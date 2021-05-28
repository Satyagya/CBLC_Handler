package com.example.LeadCrawl.enums;

public enum Domain {
    GMAIL("gmail.com"),
    STEERMI("steermi.com"),
    YAHOO("yahoo.com"),
    MUBILO("mubilo.com");

    private String domainName;

    Domain(String actualName) {
        this.domainName = actualName;
    }

    public String getDomainName() {
        return domainName;
    }
}
