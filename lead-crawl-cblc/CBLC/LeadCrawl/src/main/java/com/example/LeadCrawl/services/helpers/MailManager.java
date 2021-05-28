package com.example.LeadCrawl.services.helpers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.LeadCrawl.config.S3Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;

@Component("com.example.LeadCrawl.services.helpers.MailManager")
@Slf4j
public class MailManager {
  @Autowired
  private S3Config s3Config;
  @Value("${mail.sender.username}")
  private String userName;
  @Value("${mail.sender.password}")
  private String password;
  @Value("${download.base.url}")
  private String baseUrl;
  @Value("${aws.bucket.name.final.output}")
  private String emailOutputBucket;
  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.Notifier")
  private Notifier notifier;


  public void sendMail(String filePath, String sender, String subject, String product,
      String typeOfMail) {

    File file = new File(filePath);
    Properties props = new Properties();
    props.put("mail.smtp.auth", true);
    props.put("mail.smtp.starttls.enable", true);
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");
    Session session = Session.getInstance(props, new javax.mail.Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, password);
      }
    });
    try {
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(userName));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sender));
      message.setSubject(subject);
      message.setText(
          "The CSV Output file for the input companies List for Product" + product + "\n" + baseUrl
              + file.getName());
      MimeBodyPart messageBodyPart = new MimeBodyPart();
      Transport.send(message);
      log.info("Mail Sent to " + sender);
    } catch (MessagingException e) {
      String errorMeasage = "Error occurred while sending "+typeOfMail.toLowerCase()+" data to the mail id "+sender+" due to "+e;
      notifier.notifySlack(errorMeasage);
      e.printStackTrace();
    }
    if (typeOfMail.equalsIgnoreCase("MAIL")) {
      try {
        AmazonS3 amazonS3 = s3Config.getS3Client();
        PutObjectResult putObjectResult =
            amazonS3.putObject(emailOutputBucket, file.getName(), file);
        System.out.println(putObjectResult);
      } catch (Exception e) {
        log.error("Output email file could not be uploaded to s3 ", e);
        notifier.notifySlack("Output email file could not be uploaded to S3. Reason: "+ e.toString());
      }
    }
  }
}

