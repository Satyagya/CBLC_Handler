package com.example.CBLC_Handler.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Domain_Files")
public class DomainFiles {

    @Id
    @Column(name = "ID3")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id3;



    @Column(name = "DOMAINNF_FILENAME_WITH_PART", unique = true)
    private String domainNF_FileName_With_Part;

    @Column(name = "DOMAIN_GEN_STATUS")
    private String dg_Status;

}
