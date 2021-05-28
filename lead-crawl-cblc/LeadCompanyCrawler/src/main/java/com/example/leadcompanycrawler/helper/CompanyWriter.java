package com.example.leadcompanycrawler.helper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CompanyWriter {

  private static volatile CompanyWriter singletoneObject = null;

  private CompanyWriter() {
  }

  public static CompanyWriter singletoneMethod() {
    if (null == singletoneObject) {
      synchronized (CompanyWriter.class) {
        if (singletoneObject == null) {
          singletoneObject = new CompanyWriter();
        }
      }
    }
    return singletoneObject;
  }

  public void write(String name, String record) throws IOException {

    java.io.FileWriter fileWriter = new java.io.FileWriter(name, true);
    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
    try (PrintWriter printWriter = new PrintWriter(bufferedWriter)) {
      printWriter.println(record);
    }
  }
}
