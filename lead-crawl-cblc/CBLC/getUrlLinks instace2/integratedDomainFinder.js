var fs = require('fs');
var parse = require('csv-parse/lib/sync');
const { link } = require('fs/promises');
const puppeteerExtra = require('puppeteer-extra');
const IntegratedDomainFinderConfig = require('./IntegratedDomainFinderConfig');
const env = require('dotenv').config();
const config = require('./IntegratedDomainFinderConfig')
const axios = require('axios');
var AWS = require('aws-sdk');
var s3 = new AWS.S3({region: 'ap-south-1'});
var s3op = require('./S3Operations.js');
var codes = require('./country_codes_constant.js')
const path = require('path');
const directory = process.env.DOMAIN_FILE_PATH;
const autoCorrect = require('./autocorrect.js')
var removeSpecialCharacters = require('./removeSpecialCharacter.js');
var outputFilename = '';


var country = "";
var country_code  = "";

var searchEngines = [
    {
    searchEngine: "bing",
    isAvailable: true
    },
    {
    searchEngine: "aol",
    isAvailable: true
    },
    {
    searchEngine: "lycos",
    isAvailable: true
    }
    
]

async function sleep(ms) {
    return new Promise((resolve) => {
        setTimeout(resolve, ms);
    });
}

function getRndInteger(min, max) {
    return Math.floor(Math.random() * (max - min) ) + min;
  }

// async function countString(str, letter) {

//     if(str == '') {
//         return 0;
//     }

//     const re = new RegExp(letter, 'g');
//     const count = str.match(re).length;
//     return count;
// }

async function extractLinksAndWriteToFile(column, page, resultXpath, company) {
    var finalLinks = '';
    const links = await page.$x(resultXpath);
    if (links.length != 0) {
        for (var j = 0; j < links.length; j++) {
            var link1 = await page.evaluate(n1 => n1.textContent, links[j]);
            if ((link1.includes('.' + country_code) || link1.includes('.com') || link1.includes('/' + country_code) || link1.includes('.net')) && (!link1.includes('facebook') && !link1.includes('linkedin') && !link1.includes('instagram') && !link1.includes('news')&& !link1.includes('feed'))) {
                link1 = link1.replace('https://','');
                link1 = link1.replace('www.','');
                link1 = link1.split('/')[0];
                console.log('link1------------------------------------------------------------->',link1)
                if(link1!='')
                {
                    finalLinks +=  link1;
                    break;
                }
                await sleep(1000);
            }
        }
    }

   
    if(finalLinks== '') {
        finalLinks +=  'None';
    }
    await writeToFile(column, company, finalLinks);
    await sleep(1000);
}

async function writeToFile(column, companyName, domains) {
    var data = '';
    for(var i=0; i<column.length-1; i++) {
        data += '"' + column[i] + '"' + ',';
    }

    data += '"'+domains+'"';

    fs.appendFileSync(outputFilename, data + '\n')
}


async function bing(column, company, page)
{
    try{
    
    await page.goto((config.bingSearchQuery + company + ' AND ' + country), {
        waitUntil: 'load',
        timeout: 0
    })

    await extractLinksAndWriteToFile(column, page, config.bingResultsXpath, company);

    const client = await page.target().createCDPSession(); ///clearing cookies
    await client.send("Network.clearBrowserCookies");
    await client.send("Network.clearBrowserCache");
    await sleep(1000);
   }
    catch(e) {
        console.log(e);
    }
}   

async function aol(column, company, page)
{
    try{
    
        await page.goto((config.aolSearchQuery + company + ' AND ' + country), {
            waitUntil: 'load',
            timeout: 0
        })

        await extractLinksAndWriteToFile(column, page, config.aolSearchResultsXpath, company);
    
        const client = await page.target().createCDPSession(); ///clearing cookies
        await client.send("Network.clearBrowserCookies");
        await client.send("Network.clearBrowserCache");
        await sleep(1000);
       }
        catch(e) {
            console.log(e);
        }

}
async function lycos(column, company, page)
{
    try{
    
        await page.goto((config.lycosSearchQuery + company + ' AND ' + country), {
        waitUntil: 'load',
        timeout: 0
        })

        await extractLinksAndWriteToFile(column, page, config.lycosResultsXpath, company);
    
        const client = await page.target().createCDPSession(); ///clearing cookies
        await client.send("Network.clearBrowserCookies");
        await client.send("Network.clearBrowserCache");
        await sleep(1000);
       }
        catch(e) {
            console.log(e);
        }

}   

async function uploadDataToS3(filename){

    var uploadParams = { Bucket: process.env.BUCKET_NAME, Key: process.env.BUCKET_FOLDER_OUTPUT_FOR_DOMAIN + filename, Body: "" };
    var fileStream = fs.createReadStream(outputFilename);
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
   function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
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

async function domainFinder(csvFilePath, filename)
{
    await s3op.getFileFromS3ForDomain(filename, csvFilePath);
    await sleep(10000)

    outputFilename =process.env.DOMAIN_FILE_PATH+ filename.split(".")[0] + "_domain_output.csv";

    var data = "id,Full name,Profile url,First name,Last name,Avatar,Title,Company,Position,Function,Size,Country,Linkedin URL1,Domain1";

    fs.appendFileSync(outputFilename, data + '\n');

    const browser = await puppeteerExtra.launch({
        headless: false,
        ignoreDefaultArgs: ["--enable-automation"],
    });
    const page = await browser.newPage();
    var data=true
    try
    {   
        var rows = await readFromCsv(csvFilePath);

        for (var i = 1; i < rows.length; i++) 
        {
            var random = getRndInteger(0, 3);
            var column = rows[i];
            var company =  removeSpecialCharacters.removeSpecialCharaterForDomain(column[7]);
            if(column[11]!=null|| column[11]!=undefined || column[11]!='')
            {
                country = column[11];
                console.log("col---->", country);
                // country = codes.toTitleCase(country1);
                
                country_code= codes.codes[country];
            }
            else
            {
                country='';
                country_code='';
            }
            if(searchEngines[0].isAvailable||searchEngines[1].isAvailable||searchEngines[2].isAvailable)
            {
                while(!searchEngines[random].isAvailable) {
                    console.log('-------------------------------->', searchEngines[random].isAvailable)
                    random = getRndInteger(0, 3);
                    }
                    
                    if(searchEngines[random].searchEngine == "bing") {
                    await bing(column, company, page);
                    }
                    
                    else if(searchEngines[random].searchEngine == "aol") {
                    await aol(column, company, page);
                    }
                    
                    else if(searchEngines[random].searchEngine == "lycos") {
                    await lycos(column, company, page);
                    }
            }
            else
            {
                await sleep(3600000); 
            }
        }
        await page.close();
        await browser.close();
    }
    catch(e)
    {
        data = false
        await page.close()
        await browser.close()
        console.log(e);
    }

    await uploadDataToS3(filename);

    try{
        console.log(filename+"  *************************************************");
        await axios.post('http://localhost:9080/CBLC/updateTableAfterDG/'+filename);
    }
    catch(e){
        console.log(e);
    }
    await deleteFile();
}

module.exports = {
    domainFinder: domainFinder
}



