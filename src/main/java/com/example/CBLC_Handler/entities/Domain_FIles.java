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
public class Domain_FIles {

    @Id
    @Column(name = "ID3")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id3;

    @OneToOne
    @JoinColumn(name = "Parts_Of_Input_Files_ID2")
    private Integer id2;

    @Column(name = "DOMAINNF_FILENAME_WITH_PART")
    private String domainNF_FileName_With_Part;

    @Column(name = "DG_STATUS")
    private String dg_Status;

}
