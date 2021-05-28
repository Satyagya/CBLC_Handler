package com.example.LeadCrawl.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "EMAIL_MESSAGE_INFORMATION")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class EmailMessageInformation implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "MESSAGE_BODY")
    private String messageBody;

    @Column(name = "MIME_TYPE")
    private String mimeType;

    @Column(name = "STATUS")
    private String status;

}

