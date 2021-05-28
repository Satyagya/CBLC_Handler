package com.example.javaMail.EmailService.impl;

import com.example.javaMail.EmailService.EmailService;
import com.example.javaMail.constants.EmailConfig;
import org.springframework.stereotype.Service;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import java.util.Date;
import java.util.Properties;


@Service
public class EmailServiceImpl implements EmailService {

    public void sendSimpleMessage(){

        try
        {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new javax.mail.Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(EmailConfig.MAIL_ID, EmailConfig.PASSWORD);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(EmailConfig.MAIL_ID, false));

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("swastikswa29@gmail.com"));
            msg.setSubject("Company based crawling");
            msg.setSentDate(new Date());

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent("Email for Comapny based Crawling", "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            MimeBodyPart attachPart = new MimeBodyPart();
            attachPart.attachFile(EmailConfig.FILE_PATH + "Rita File April15 2021 - Sheet1.csv");
            multipart.addBodyPart(attachPart);
            msg.setContent(multipart);
            Transport.send(msg);
        }
        catch (Exception exe)
        {
            exe.printStackTrace();
        }

        System.out.println("---------------process completed------------");

    }
}



// using spring boot mail package

//package com.example.javaMail.EmailService.impl;
//
//import com.example.javaMail.EmailService.EmailService;
//import com.example.javaMail.constants.EmailConfig;
//import org.springframework.stereotype.Service;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//
//import javax.mail.internet.MimeMessage;
//import java.io.File;
//
//@Service
//public class EmailServiceImpl implements EmailService {
//    @Autowired
//    private JavaMailSender emailSender;
//
//    public void sendSimpleMessage() {
//
//        MimeMessage message = emailSender.createMimeMessage();
//
//        try {
//
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setFrom("kswastik29@gmail.com");
//            helper.setTo("swastikswa29@gmail.com");
//            helper.setSubject("Email testing");
//            helper.setText("Company based crawling");
//
//            FileSystemResource file = new FileSystemResource(new File(EmailConfig.FILE_PATH + "Rita File April15 2021 - Sheet1.csv"));
//            helper.addAttachment("Rita File April15 2021 - Sheet1.csv", file);
//
//            emailSender.send(message);
//        } catch (javax.mail.MessagingException e) {
//
//            e.printStackTrace();
//
//        }
//
//          System.out.println("---------------process completed------------");
//
//    }
//}