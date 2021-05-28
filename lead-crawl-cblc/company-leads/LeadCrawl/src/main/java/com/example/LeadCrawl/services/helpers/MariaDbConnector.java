package com.example.LeadCrawl.services.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static com.example.LeadCrawl.constants.Constants.MARIADB_CONFIG;
import static com.example.LeadCrawl.constants.Constants.MARIADB_PASSWORD;
import static com.example.LeadCrawl.constants.Constants.MARIADB_USER_NAME;

@Component
@Slf4j
public class MariaDbConnector {

  @Value("${mariadb.config}")
  private String mariaDbConfig;

  @Value("${mariadb.username}")
  private String mariadbUserName;

  @Value("${mariadb.password}")
  private String mariadbPassword;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.Notifier")
  private Notifier notifier;

  private Connection conn;

  public HashMap<String,String> connectDb(String column) {
    HashMap<String,String> stringHashMap = new HashMap<>();
    Statement stmt = null;
    try {
      Class.forName("org.mariadb.jdbc.Driver");

      conn = DriverManager.getConnection(mariaDbConfig,mariadbUserName,mariadbPassword);
      String sql = "select * from COMPANY_LEADS_CONFIG";
      stmt = conn.createStatement();
      ResultSet resultSet = stmt.executeQuery(sql);
      while (resultSet.next()){
        stringHashMap.put(resultSet.getString("PRODUCT_NAME"),resultSet.getString(column));
      }
      conn.close();
    } catch (ClassNotFoundException | SQLException e) {
        log.error("Exception occurred while getting designations from Database. Reason: "+e.toString());
      notifier.notifySlack("Exception occurred while getting designations from Database. Reason: "+e.toString());
    }
    return stringHashMap;
  }
}
