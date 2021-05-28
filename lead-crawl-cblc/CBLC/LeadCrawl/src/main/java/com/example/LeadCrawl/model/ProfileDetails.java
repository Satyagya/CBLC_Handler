package com.example.LeadCrawl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;


import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileDetails implements Serializable {
  private String id;
  private String avatar;
  private String fullName;
  private String companyName;
  private String profileUrl;
  private String firstName;
  private String lastName;
  private String designation;
  private String function;
  private String size;
  private List<String> email;
  private String companyDomain;
  private List<String> emailPattern;
  private String country;
  private String leadProfile;
}
