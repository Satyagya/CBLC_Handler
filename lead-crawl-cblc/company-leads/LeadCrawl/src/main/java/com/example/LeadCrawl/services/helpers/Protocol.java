package com.example.LeadCrawl.services.helpers;

import com.example.LeadCrawl.model.ProtocolDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.example.LeadCrawl.enums.Domain.*;

@Slf4j
@Component("com.example.LeadCrawl.services.helpers.Protocol")
class Protocol {

    private static final Map<String, ProtocolDetails> map = new HashMap<>();

    @Autowired
    @Qualifier("com.example.LeadCrawl.services.helpers.ProtocolBuilder")
    private ProtocolBuilder protocolFactory;

    @PostConstruct
    private void init() {
        map.put(GMAIL.getDomainName(), protocolFactory.getGmailDetails());
        map.put(STEERMI.getDomainName(), protocolFactory.getSteermiDetails());
        map.put(YAHOO.getDomainName(), protocolFactory.getYahooDetails());
        map.put(MUBILO.getDomainName(), protocolFactory.getMubiloDetails());
    }

    ProtocolDetails getProtocolDetails(String domain) {
        return map.get(domain);
    }
}
