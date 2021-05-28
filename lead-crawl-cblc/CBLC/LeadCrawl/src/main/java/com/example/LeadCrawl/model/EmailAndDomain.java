package com.example.LeadCrawl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailAndDomain {

    private String identity;

    private String domain;

    public EmailAndDomain(String emailAddress) {
        String[] emailAddressParts = emailAddress.split("@");
        identity = emailAddressParts[0];
        domain = emailAddressParts[1].toLowerCase();
    }
}
