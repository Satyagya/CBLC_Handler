package com.example.LeadCrawl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProtocolDetails {
    private String smtpHost;
    private String smtpPort;
    private String pop3Host;
    private String pop3Port;
    private String imapHost;
    private String imapPort;
    private String spamFolderName;
    private String inboxFolderName;
}
