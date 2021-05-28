
var linkedin = require('./getProfileUrl.js');
var companywebsites = require('./getCompanyWebsitesFromDb.js');
var domainfinder = require('./integratedDomainFinder.js');
const express = require('express');
const app = express();
var bodyParser = require("body-parser");
const env = require('dotenv').config();
var s3 = require('./S3Operations.js');


app.use(bodyParser.json());

var router = express.Router();

function error(status, msg) {
  var err = new Error(msg);
  err.status = status;
  return err;
}

app.post("/leads", (req, res) => {
    var data = req.body;
    data = JSON.stringify(data);
    var filename = JSON.parse(data).file_name;    
    var csvFilePath = process.env.BASE_PATH + filename;
    s3.getFileFromS3(filename, csvFilePath);
    linkedin.getProfileUrls(JSON.parse(data).file_name);
    res.send(true);
});

app.post("/companywebsites", (req, res) => {
  var data = req.body;
  data = JSON.stringify(data);
  var filename = JSON.parse(data).file_name;    
  var csvFilePath = process.env.DOMAIN_CHECKER_FILE_PATH + filename;
  companywebsites.addWebsiteInCsv(csvFilePath, filename);
  res.send(true);
});

app.post("/domainfinder", (req, res) => {
  var data = req.body;
  data = JSON.stringify(data);
  var filename = JSON.parse(data).file_name;    
  console.log(filename+'-------------------------------------------file for domain finder');
  var csvFilePath = process.env.DOMAIN_FILE_PATH+ filename;
  domainfinder.domainFinder(csvFilePath, filename);
  res.send(true);
});

app.use(function (req, res) {
    res.setHeader("Content-Type", "text/plain");
    res.end(JSON.stringify(req.body, null, 2));
  });

app.listen(process.env.PORT, () => {
  console.log(
    `Data Exporter Service started at http://localhost:${process.env.PORT}`
  );
});

