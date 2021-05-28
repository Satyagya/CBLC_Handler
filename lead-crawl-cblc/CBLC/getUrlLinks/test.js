
// const fs = require('fs');
// const path = require('path');

// const directory = '/Users/mohitchhabra/Desktop/getUrlLinks/Domain_Found_Files/';

// function deleteFile()
// {
// fs.readdir(directory, (err, files) => {
//   if (err) throw err;

//   for (const file of files) {
//     fs.unlink(path.join(directory, file), err => {
//       if (err) throw err;
//     });
//   }
// });
// }
// deleteFile()
var words = ['Alaska','South Africa','Zambia']
var autocorrect = require('autocorrect')({words: words})
function test()
{

    var x = autocorrect('South Africa')
    console.log(x)
}
test()