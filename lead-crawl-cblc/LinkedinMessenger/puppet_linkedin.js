const puppeteerExtra = require('puppeteer-extra');
const pluginStealth = require('puppeteer-extra-plugin-stealth');
var fs = require('fs');
const C = require('./linkedin_constants');

async function sleep(ms) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

function randon_int(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function random_sleep(min, max) {
  return Math.floor(Math.random() * (max - min + 1000)) + min;
}

function random_delay(min, max) {
  return Math.floor(Math.random() * (max - min + 100)) + min;
}


async function start_fun(num) {
  puppeteerExtra.use(pluginStealth());
  const browser = await puppeteerExtra.launch(
    {
      headless: false,
      ignoreDefaultArgs: ['--enable-automation'],
    });

  const page = await browser.newPage();
    await page.goto(C.LinkedinURL);
    await sleep(5000 + random_sleep(1000, 10000));
    await page.click(C.USERNAME_SELECTOR2);
    await page.keyboard.type(C.username[num], { delay: 500 + random_delay(100, 500) });              ////
    await page.click(C.PASSWORD_SELECTOR2);
    await page.keyboard.type(C.password[num], { delay: 500 + random_delay(100, 500) });             /////
    await sleep(8000 + random_sleep(1000, 5000));
    await page.click(C.LOGIN_BUTTON, { delay: 500 + random_delay(100, 500) });
    await sleep(8000 + random_sleep(1000, 5000));
    var num = randon_int(0, 3);
    await goto_home_page(page,num);
}


async function goto_home_page(page,num){
try{
    await sleep(5000);
    if (num == 0) {
      const network = await page.$x(C.NETWORK_BUTTON_XPATH);
      network[0].click();
      await sleep(4100 + random_sleep(1000, 5000));
    }
    if (num == 1) {
      const jobs = await page.$x(C.JOBS_BUTTON_XPATH);
      jobs[0].click();
      await sleep(4200 + random_sleep(1000, 5000));
    }
    if (num == 2) {
      const messages = await page.$x(C.MESSAGES_BUTTON_XPATH);
      messages[0].click();
      await sleep(4300 + random_sleep(1000, 5000));
    }
    if (num == 3) {
      const notifications = await page.$x(C.NOTIFICATION_BUTTON_XPATH);
      notifications[0].click();
      await sleep(4500 + random_sleep(1000, 5000));
    }
    await page.goBack();
  
   readInputFile(page);
  }
catch (e) {
    console.log("Error in loading profile");
    fs.appendFileSync('Response_File.txt',('Cannot send message\t'+profile_url+'\tError in loading profile'+'\n'))
    console.log('Error:', e.stack);
  }
}


  async function readInputFile(page)
  {
    var url = fs.readFileSync('linked_input.txt', 'utf8');
    var url_arr = url.split("\n");

    for (var i = 0; i < url_arr.length; i++) {
      var msg = url_arr[i].split('\t');
      var profile_url =msg[0];
      console.log(msg[0]+"\n"+msg[1])
      await sleep(3000 + random_sleep(1000, 10000));
      await page.goto(profile_url, {
        waitUntil: 'load',
        timeout: 0
      });
      await sleep(10000);
      console.log("enteres url" + i);
      await getNameAndDegreeOfPerson(page,profile_url,msg[1])
    }
  }

    async function getNameAndDegreeOfPerson(page,profile_url,msg){
      var n = randon_int(0, 2);
      const name = await page.$x(C.PERSON_NAME_XPATH);
      const name1 = await name[0].getProperty('textContent');
      const name_val = name1._remoteObject.value;
      var person_name = name_val.trim();

      const degree = await page.$x(C.CONNECTION_DEGREE_XPATH);
      const degree1 = await degree[0].getProperty('textContent');
      const degree2 = degree1._remoteObject.value;
      const degree_val = degree2.trim();
      try{
      await check_button(page,profile_url,person_name,n,C,msg);
      }
      catch(e){
        console.log("Did'nt found the connect button")
        console.log(e.stack);
      }
    }


    async function check_button(page,profile_url,person_name,n,C,msg){
      try{
      const pending = await page.$x('//div[@class="mt1 inline-flex align-items-center ember-view"]/div');
      console.log(pending.length);
      const req_pending = await pending[0].getProperty('textContent');
      const text_on_button = req_pending._remoteObject.value;
      if (text_on_button.trim() == "Pending") {
        console.log("Request already sent to " + person_name + ".");
        var out = "Request already sent " +'\t'+profile_url+'\n'; 
        fs.appendFileSync('Response_File.txt',out);
      if (text_on_button.trim() == "Message"){
        await pending[0].click();
        console.log("Message clicked"); 
        await sleep(2000 + random_sleep(1000, 10000));
        await page.keyboard.type(C.HELLO + person_name+',' +'\n'+ msg, { delay: 300 + random_delay(100, 500) });
        await sleep(2000 + random_sleep(1000, 10000));

        const send_button = await page.$x(C.SEND_BUTTON_XPATH);
        await send_button[0].click();
        console.log("message sent to url" + i + " " + person_name);
        fs.appendFileSync('Response_File.txt',("Message sent\t"+profile_url+'\n'));
        await sleep(4000);
        const cross = await page.$x(C.CROSS_BUTTON_XPATH);
        await cross[0].click();
      }

      if(text_on_button.trim()=="Connect"){
          await pending[0].click();
          console.log("Connect clicked");
        await sleep(5000);
        const add_note = await page.$x(C.ADD_NOTE_XPATH);
        await add_note[0].click();
        console.log("add note clicked");
        await sleep(random_sleep(1000, 10000));
        await page.keyboard.type(C.HELLO + person_name+','+'\n' + C.MESSAGES[n], { delay: 300 + random_delay(100, 500) });
        await sleep(6000 + random_sleep(1000, 5000));

        const send_request = await page.$x(C.SEND_INVITE_XPATH);
        await send_request[0].click();
        fs.appendFileSync('Response_File.txt',('Request sent\t'+profile_url+'\n'));
      }

      if (text_on_button.trim()=="Follow") {
        const len = pending.length;
        await pending[len-1].click();
        console.log("More clicked");
        await sleep(3000);

        const conn = await page.$x('//div[@class="pv-s-profile-actions__overflow ember-view"]/div/div/div/ul/li[3]');
        const check_pending = await conn[0].getProperty('textContent'); 
        const is_pending = check_pending._remoteObject.value;
        if(is_pending.trim()=='Pending'){
          fs.writeFileSync('Response_File.txt',('Message already sent \t'+profile_url+'\n'));
        }
        else{
        await conn[0].click();
        console.log("Connect clicked");

        await sleep(5000);
        const add_note = await page.$x(C.ADD_NOTE_XPATH);
        await add_note[0].click();
        console.log("add note clicked");
        await sleep(random_sleep(1000, 10000));
        await page.keyboard.type(C.HELLO + person_name + C.MESSAGES[n], { delay: 300 + random_delay(100, 500) });
        await sleep(6000 + random_sleep(1000, 5000));

        const send_request = await page.$x(C.SEND_INVITE_XPATH);
        await send_request[0].click();
        fs.appendFileSync('Response_File.txt',("Mssage sent"+'\t'+profile_url+'\n'));
        }
      }
    }
  catch(e){
    console.log("Error in finding button");
    fs.appendFileSync('Response_File.txt',('Cannot send message '+'\t'+profile_url+'\n'))
    console.log(e.stack);
  }
}


start_fun(1);