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
public class Domain_FIle {

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

    public Integer getId3() {
        return id3;
    }

    public void setId3(Integer id3) {
        this.id3 = id3;
    }

    public Integer getId2() {
        return id2;
    }

    public void setId2(Integer id2) {
        this.id2 = id2;
    }

    public String getDomainNF_FileName_With_Part() {
        return domainNF_FileName_With_Part;
    }

    public void setDomainNF_FileName_With_Part(String domainNF_FileName_With_Part) {
        this.domainNF_FileName_With_Part = domainNF_FileName_With_Part;
    }

    public String getDg_Status() {
        return dg_Status;
    }

    public void setDg_Status(String dg_Status) {
        this.dg_Status = dg_Status;
    }
}
