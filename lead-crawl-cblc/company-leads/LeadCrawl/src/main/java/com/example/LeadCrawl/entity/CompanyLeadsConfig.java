package com.example.LeadCrawl.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "COMPANY_LEADS_CONFIG")
public class CompanyLeadsConfig {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "PRODUCT_NAME")
    private String productName;


    @Column(name = "DESIGNATIONS")
    private String designations;


    @Column(name = "EMAIL_IDS")
    private String emailIds;
}
