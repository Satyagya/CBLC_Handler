package com.jsoup.crawling.service.helper;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

@Service("com.jsoup.crawling.service.helper.ProxyAuthenticator")
@Slf4j
public class ProxyAuthenticator extends Authenticator {
    private String userName;
    private String password;

    public ProxyAuthenticator(){

    }
    public ProxyAuthenticator(String userName, String password){
        this.userName = userName;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication(){
        return new PasswordAuthentication(userName,password.toCharArray());
    }
}
