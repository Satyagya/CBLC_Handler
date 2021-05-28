package com.example.LeadCrawl.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.Date;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "EMAIL_ACCOUNT_INFORMATION")
public class EmailAccountInformation {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "USERNAME")
    private String userName;

    @NotNull
    @Column(name = "PASSWORD")
    private String password;

    @NotNull
    @Column(name = "STATUS")
    private String status;

    @NotNull
    @Column(name = "JOB_STATUS")
    private String jobStatus;

    @NotNull
    @Column(name = "LAST_UPDATED")
    private Date lastUpdated;

    @NotNull
    @Column(name = "COUNT")
    private int count;
}

