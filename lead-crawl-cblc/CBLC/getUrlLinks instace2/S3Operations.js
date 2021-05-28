var AWS = require('aws-sdk');
var s3 = new AWS.S3({region: 'ap-south-1'});

async function getFileFromS3(filename, csvFilePath) {
    var params = {Bucket: process.env.BUCKET_NAME, Key: process.env.BUCKET_FOLDER_INPUT + filename};
    var file = require('fs').createWriteStream(csvFilePath);
    s3.getObject(params).
    on('httpData', function(chunk) { file.write(chunk); }).
    on('httpDone', function() { file.end(); }).
    send();
    console.log("======> read file from S3");
}

async function getFileFromS3ForDomain(filename, csvFilePath) {
    var params = {Bucket: process.env.BUCKET_NAME, Key: process.env.BUCKET_FOLDER_INPUT_FOR_DOMAIN + filename};
    var file = require('fs').createWriteStream(csvFilePath);
    s3.getObject(params).
    on('httpData', function(chunk) { file.write(chunk); }).
    on('httpDone', function() { file.end(); }).
    send();
    console.log("======> read file from S3");
}
async function getFileFromS3ForDomainChecker(filename, csvFilePath) {
    var params = {Bucket: process.env.BUCKET_NAME, Key: process.env.BUCKET_FOLDER_OUTPUT + filename};
    var file = require('fs').createWriteStream(csvFilePath);
    s3.getObject(params).
    on('httpData', function(chunk) { file.write(chunk); }).
    on('httpDone', function() { file.end(); }).
    send();
    console.log("======> read file from S3");
}

module.exports = {
    getFileFromS3: getFileFromS3,
    getFileFromS3ForDomain: getFileFromS3ForDomain,
    getFileFromS3ForDomainChecker: getFileFromS3ForDomainChecker
}