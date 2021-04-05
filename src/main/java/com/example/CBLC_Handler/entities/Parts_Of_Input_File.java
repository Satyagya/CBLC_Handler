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
@Table(name = "Parts_Of_Input_Files")
public class Parts_Of_Input_File {

    @Id
    @Column(name = "Parts_Of_Input_Files_ID2")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @OneToOne(mappedBy = "Parts_Of_Input_Files", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Integer id2;


    @ManyToOne
    @JoinColumn(name = "Input_files_ID1")
    private Integer id1;

    @Column(name = "INPUT_FILENAME_WITH_PART")
    private String input_FileName_With_part;

    @Column(name = "LEAD_CRAWL_STATUS")
    private String lc_Status;

    @Column(name = "DOMAIN_CHECK_STATUS")
    private String dc_Status;

    @Column(name = "EMAIL_GEN_STATUS_FOR_FOUND_DOMAINS")
    private String eg_Found_Status;

    @Column(name = "EMAIL_GEN_STATUS_FOR_NOTFOUND_DOMAINS")
    private String eg_NotFound_Status;

    @Column(name = "OUTPUT_FILENAME_WITH_PART")
    private String output_FileName_With_Part;

    public Integer getId2() {
        return id2;
    }

    public void setId2(Integer id2) {
        this.id2 = id2;
    }

    public Integer getId1() {
        return id1;
    }

    public void setId1(Integer id1) {
        this.id1 = id1;
    }

    public String getInput_FileName_With_part() {
        return input_FileName_With_part;
    }

    public void setInput_FileName_With_part(String input_FileName_With_part) {
        this.input_FileName_With_part = input_FileName_With_part;
    }

    public String getLc_Status() {
        return lc_Status;
    }

    public void setLc_Status(String lc_Status) {
        this.lc_Status = lc_Status;
    }

    public String getDc_Status() {
        return dc_Status;
    }

    public void setDc_Status(String dc_Status) {
        this.dc_Status = dc_Status;
    }

    public String getEg_Found_Status() {
        return eg_Found_Status;
    }

    public void setEg_Found_Status(String eg_Found_Status) {
        this.eg_Found_Status = eg_Found_Status;
    }

    public String getEg_NotFound_Status() {
        return eg_NotFound_Status;
    }

    public void setEg_NotFound_Status(String eg_NotFound_Status) {
        this.eg_NotFound_Status = eg_NotFound_Status;
    }

    public String getOutput_FileName_With_Part() {
        return output_FileName_With_Part;
    }

    public void setOutput_FileName_With_Part(String output_FileName_With_Part) {
        this.output_FileName_With_Part = output_FileName_With_Part;
    }
}
