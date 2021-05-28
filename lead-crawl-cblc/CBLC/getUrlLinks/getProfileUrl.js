var fs = require('fs'); 
var parse = require('csv-parse/lib/sync');
const puppeteerExtra = require('puppeteer-extra');
const pluginStealth = require('puppeteer-extra-plugin-stealth');
//const { outputFile } = require('./IntegratedDomainFinderConfig');
//var removeSpecialCharacters = require('removeSpecialCharacter.js')
var AWS = require('aws-sdk');
const env = require('dotenv').config();
const axios = require('axios');
const path = require('path');
var csvFilePath = '';
var outputFile = '';
const directory = process.env.BASE_PATH;
var s3 = new AWS.S3({region: 'us-east-1'});
var removeSpecialCharacters = require('./removeSpecialCharacter.js')

async function sleep(ms) {
    return new Promise((resolve) => {
        setTimeout(resolve, ms);
    });
}

function addUrlData(eachRow, hrefs){
    data = ''
    for (var i =0; i<eachRow.length; i++){
        data += '"' + eachRow[i] + '"' + ',';
    }

    for(var i=0; i<hrefs.length; i++)
    {
        if(!hrefs[i].includes('linkedin.com'))
        {
            hrefs.splice(i,1);
        }
    }

    for(var i=0; i<hrefs.length; i++)
    {
        if(hrefs[i].includes('/pub/') || hrefs[i].includes('/jobs/') || hrefs[i].includes('/company/'))
        {
            hrefs.splice(i, 1);
            i=i-1;
        }
    }
    
    data = ''
    for (var i =0; i<eachRow.length; i++){
        data += '"' + eachRow[i] + '"' + ',';
    }
    var totalUrls = hrefs.length;
    if (totalUrls > 2){
        for (var i = 0; i < 1; i++) {
            data += '"' + hrefs[i] + '"' + ',';
        }
    }
    else if (totalUrls > 1){
        for (var i = 0; i < 1; i++) {
            data += '"' + hrefs[i] + '"' +  ',';
        }
    }
    else if (totalUrls > 0) {
        for (var i = 0; i < 1; i++) {
            data += '"' + hrefs[i] + '"' +  ',';
        }
    }
    else{
            data += ('"None"' + ',');
    }
    
    data = data.substring(0, data.length-1);
    writeInFile(outputFile ,data)
}


function writeInFile(filename, data){
    
    fs.appendFileSync(filename, data + "\n");
}


async function uploadDataToS3(localFilePath, filename){

    var uploadParams = { Bucket: process.env.BUCKET_NAME, Key: process.env.BUCKET_FOLDER_OUTPUT + filename, Body: "" };
    var fileStream = fs.createReadStream(localFilePath);
    fileStream.on("error", function (err) {
      console.log("File Error", err);
    });
    uploadParams.Body = fileStream;

    s3.upload(uploadParams, function (err, data) {
      if (err) {
        console.log("Error", err);
      }

    });
}

async function readFromCsv(filename) {

 var dataFile = fs.readFileSync(filename);
 var records = parse(dataFile, {columns: false});

 return records;

}
async function deleteFile()
{
    fs.readdir(directory, (err, files) => {
        if (err) throw err;
    
        for (const file of files) {
        fs.unlink(path.join(directory, file), err => {
            if (err) throw err;
        });
        }
    });
}

async function getProfileUrls(filename)
{
    const browser = await puppeteerExtra.launch({
        headless: false,
        ignoreDefaultArgs: ["--enable-automation"],
    });
    const page = await browser.newPage();
    try
    {  
        csvFilePath = process.env.BASE_PATH + filename; 
        outputFile = process.env.BASE_PATH + filename.split(".")[0] + "_Lead_output.csv";
        
        var data = "id,Full name,Profile url,First name,Last name,Avatar,Title,Company,Position,Function,Size,Country,Linkedin URL1";
        fs.appendFileSync(outputFile, data + '\n');

        var rows = await readFromCsv(csvFilePath);
        console.log("=====> starting crawling");
        var query = '';
        for (var i = 1; i < rows.length; i++) 
        {
            var column = rows[i];
            var name =  column[1];
            var company = column[7];
            var position = column[8];
            query = removeSpecialCharacters.removeSpecialCharaterForLead(name,company,position);


            await page.goto('https://bing.com');
			await page.type('input.sb_form_q', query);
			page.keyboard.press('Enter');
            await sleep(5000);
            var currentPage = await page.url();
            var hrefs = await Promise.all((await page.$x('//*[@class  ="b_algo"]/h2/a')).map(async item => await (await item.getProperty('href')).jsonValue()));
            await sleep(2000);
            addUrlData(column, hrefs, outputFile);

            const client = await page.target().createCDPSession(); ///clearing cookies
            await client.send("Network.clearBrowserCookies");
            await client.send("Network.clearBrowserCache");
            await sleep(1000 * (Math.floor((Math.random() * 10) + 1)));
        }
        await page.close();
        await browser.close();
    }
    catch(e)
    {
        await page.close();
        await browser.close();
        console.log(e);
    }

    await uploadDataToS3(outputFile, filename);

    try {
        await axios.post('http://localhost:9080/CBLC/updateTableAfterLC/'+filename);
    }
    catch(e) {
        console.log(e);
    }
    //await deleteFile();
}

module.exports = {
    getProfileUrls: getProfileUrls
}

