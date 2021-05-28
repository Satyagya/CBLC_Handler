package com.example.LeadCrawl.services.helpers;

import com.example.LeadCrawl.model.ProtocolDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.example.LeadCrawl.constants.ProtocolConstants.*;

@Slf4j
@Component("com.example.LeadCrawl.services.helpers.ProtocolBuilder")
class ProtocolBuilder {

    ProtocolDetails getGmailDetails() {
        return ProtocolDetails.builder().imapHost(GMAIL_IMAP_HOST).imapPort(GMAIL_IMAP_PORT)
                .pop3Host(GMAIL_POP3_HOST).pop3Port(GMAIL_POP3_PORT).smtpHost(GMAIL_SMTP_HOST)
                .smtpPort(GMAIL_SMTP_PORT).spamFolderName(GMAIL_SPAM_FOLDER)
                .inboxFolderName(GMAIL_INBOX_FOLDER).build();
    }


    ProtocolDetails getSteermiDetails() {
        return ProtocolDetails.builder().imapHost(STEERMI_IMAP_HOST).imapPort(STEERMI_IMAP_PORT)
                .pop3Host(STEERMI_POP3_HOST).pop3Port(STEERMI_POP3_PORT).smtpHost(STEERMI_SMTP_HOST)
                .smtpPort(STEERMI_SMTP_PORT).spamFolderName(STEERMI_SPAM_FOLDER)
                //TODO need to check and test
                .inboxFolderName(STEERMI_INBOX_FOLDER).build();
    }

    ProtocolDetails getYahooDetails() {
        return ProtocolDetails.builder().imapHost(YAHOO_IMAP_HOST).imapPort(YAHOO_IMAP_PORT)
                .pop3Host(YAHOO_POP3_HOST).pop3Port(YAHOO_POP3_PORT).smtpHost(YAHOO_SMTP_HOST)
                .smtpPort(YAHOO_SMTP_PORT).spamFolderName(YAHOO_SPAM_FOLDER)
                //TODO need to check and test
                .inboxFolderName(YAHOO_INBOX_FOLDER).build();
    }

    ProtocolDetails getMubiloDetails() {
        return ProtocolDetails.builder().imapHost(MUBILO_IMAP_HOST).imapPort(MUBILO_IMAP_PORT)
                .pop3Host(MUBILO_POP3_HOST).pop3Port(MUBILO_POP3_PORT).smtpHost(MUBILO_SMTP_HOST)
                .smtpPort(MUBILO_SMTP_PORT).spamFolderName(MUBILO_SPAM_FOLDER)
                //TODO need to check and test
                .inboxFolderName(MUBILO_INBOX_FOLDER).build();
    }
}


