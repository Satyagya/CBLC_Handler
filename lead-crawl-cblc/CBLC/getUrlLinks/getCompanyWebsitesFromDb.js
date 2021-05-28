var fs = require('fs'); 
var parse = require('csv-parse/lib/sync');
//const mariadb = require('mariasql');
const path = require('path');

var None = null;
const mariadb = require('mariadb/');
var AWS = require('aws-sdk');
var s3op = require('./S3Operations.js');
var s3 = new AWS.S3({region: 'us-east-1'});
const env = require('dotenv').config();
var pool = mariadb.createPool({
    host: "localhost",
    user: "automi_crawler_admin",
    password: "automi_crawler_admin",
    database: "CRAWL_DB"
  });
const axios = require('axios')
const directory = process.env.DOMAIN_NOT_FOUND_FILE_PATH;
const directory1 = process.env.DOMAIN_FOUND_FILE_PATH;
const directory2 = process.env.DOMAIN_CHECKER_FILE_PATH;
// function getResultsFromDB(table_name, company_name) {
//     var cur;
//     pool.getConnection()
//     .then(conn => {
    
//       conn.query("SELECT WEBSITE FROM ? WHERE COMPANY_NAME=?", [table_name, company_name])
//         .then((rows) => {
//           console.log(rows); 
//           cur = rows;
//           console.log(cur);
//         })
//     }).catch(err => {
//     })

//     return cur;
// }


async function getResultsFromDB(table_name, company_name) {
    let conn;
    var cur;
    try
    {
       console.log(company_name)
        conn = await pool.getConnection();
	    // var rows = await conn.query("SELECT 1 as val");
	    // console.log(rows); //[ {val: 1}, meta: ... ]
	    var res = await conn.query("SELECT WEBSITE FROM " + table_name + " WHERE COMPANY_NAME = ? ", [company_name]);
        let result = res.map(a => a.WEBSITE);
        cur = result;
	    console.log(cur);
    }
    catch(e)
    {
        console.log(e)
    }
    finally
    {
        conn.end();
    }
    return cur;
}


async function addUrlFromLinkedInToList(table_name, row) {
    try
    {
    var count = row.length;
    var website = None;
    

    var company_name = row[7];

            console.log("comapny_name------->", company_name);
            var cur = await getResultsFromDB(table_name ,company_name);
            console.log(cur+"---------------------");
            for( website of cur) {
                var web = website[0];
                if(web== ''){
                    continue;
                }
                else if ( web != None && web!= ''){
                    return website;
                }
                else{
                    continue;
                }
            }
            if( website == None || website==''){
                return '-';
            }
    }
    catch(e)
    {
        console.log(e)
    }
    
}

async function checkWebsiteInTable(table_name, company_name) {
    
    try{
        var website = None;
        var cur = await getResultsFromDB(table_name, company_name);
        for( website of cur ) 
        {
            var web =website[0]
            if(web== ''){
                continue;
            }
            else if( web != None && web != '' ) {
                return website;
            }
            else {
                continue;
            }
        }
    }
    catch(e)
    {
        console.log(e);
    }

    return '-';
}

async function readFromCsv(filename) {
 
    var dataFile = fs.readFileSync(filename);
    var records = parse(dataFile, {columns: false});
    console.log(records)
    return records;
   }

   async function uploadDataToS3(localFilePath, filename, BUCKET_FOLDER_DOMAIN_CHECKER_PARA){
    var uploadParams = { Bucket: process.env.BUCKET_NAME, Key: BUCKET_FOLDER_DOMAIN_CHECKER_PARA + filename, Body: "" };
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
function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

function deleteFile(fileDirectory)
{
    fs.readdir(fileDirectory, (err, files) => {
        if (err) throw err;
    
        for (const file of files) {
        fs.unlink(path.join(fileDirectory, file), err => {
            if (err) throw err;
        });
        }
    });
}
async function addWebsiteInCsv(csvfilePath, filename) {
        await s3op.getFileFromS3ForDomainChecker(filename, csvfilePath)
        console.log('filename---------------------------------------->',filename);
        await sleep(10000);
        var csvreader =  await readFromCsv(csvfilePath);
        var count = 1;
        var row;
        var data1='';
        data1= "id,Full name,Profile url,First name,Last name,Avatar,Title,Company,Position,Function,Size,Country,Linkedin URL1,Domain1";
        
        
        fs.appendFileSync(process.env.DOMAIN_NOT_FOUND_FILE_PATH+ filename.split(".")[0] + "_NF_output.csv", data1 + '\n');
        fs.appendFileSync(process.env.DOMAIN_FOUND_FILE_PATH + filename.split(".")[0] + "_output.csv", data1 + '\n');
        for( count= 1;count<csvreader.length;count++) {
            row=csvreader[count];
            try
            {
            var eachRowList = await addUrlFromLinkedInToList('COMPANY_DL',row);
            console.log('after linkedIn and Glassdoor: ', row[7], eachRowList);
            if(eachRowList == '-') {
                var company = row[7];
                var website = await checkWebsiteInTable('pb_all', company);
                console.log(website+'This is webiste '+'---------------------------------------------------------->');
                if(website!='-' && website!=None) {
                    console.log('after powrbot: ',company, website);
                    eachRowList = website
                    console.log('----> eachrowlist[8] in pb_all--->', eachRowList);
            }
        }
            var data=  '';
            for(var i=0; i<row.length;i++) {
                data += '"'+row[i]+'"'+ ',';
            }
            
            data += '"' + eachRowList + '"';
            
            
            if(eachRowList == '-') {
                fs.appendFileSync(process.env.DOMAIN_NOT_FOUND_FILE_PATH+ filename.split(".")[0] + "_NF_output.csv", data+ "\n");
            }
            else {
                fs.appendFileSync(process.env.DOMAIN_FOUND_FILE_PATH + filename.split(".")[0]+"_output.csv", data + "\n");
            }
        }catch(e)
        {
            console.log(e)
        }
    
    }

    await uploadDataToS3(process.env.DOMAIN_NOT_FOUND_FILE_PATH+ filename.split(".")[0]+"_NF_output.csv", filename.split(".")[0]+"_NF_output.csv", process.env.BUCKET_FOLDER_INPUT_FOR_DOMAIN);
    await sleep(10000)
    await uploadDataToS3(process.env.DOMAIN_FOUND_FILE_PATH + filename.split(".")[0]+"_output.csv", filename.split(".")[0]+"_output.csv", process.env.BUCKET_FOLDER_OUTPUT_FOR_DOMAIN);
    await sleep(10000)
    try {
        await axios.post('http://localhost:9080/CBLC/updateTableAfterDC/'+filename);
    }
    catch(e) {
        console.log(e);
    }
    deleteFile(directory);
    deleteFile(directory1);
    deleteFile(directory2);
}
//addWebsiteInCsv(process.env.DOMAIN_CHECKER_FILE_PATH + 'p1_Nigeria30DEmailIdlist.csv','p1_Nigeria30DEmailIdlist.csv')
    module.exports = {
        addWebsiteInCsv: addWebsiteInCsv
    }