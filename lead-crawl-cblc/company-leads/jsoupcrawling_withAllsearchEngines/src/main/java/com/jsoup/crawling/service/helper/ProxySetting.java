package com.jsoup.crawling.service.helper;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.Authenticator;

@Service("com.jsoup.crawling.service.helper.ProxySetting")
@Slf4j
public class ProxySetting {

    @Value("${proxy.rotating.api}")
    private String proxyHost;

    @Value("${proxy.rotating.port}")
    private String proxyPort;

    @Value("${proxy.rotating.username}")
    private String proxyUsername;

    @Value("${proxy.rotating.password}")
    private String proxyPassword;

    public void setProxy(){
        Authenticator.setDefault(new ProxyAuthenticator(proxyUsername, proxyPassword));
        System.setProperty("http.proxyUser", proxyUsername);
        System.setProperty("http.proxyPassword", proxyPassword);
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
    }

}
