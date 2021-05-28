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
public class PartsOfInputFiles {

    @Id
    @Column(name = "Parts_Of_Input_Files_ID2")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@OneToOne(mappedBy = "Parts_Of_Input_Files", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Integer id2;


    @Column(name = "INPUT_FILENAME_WITH_PART", unique = true)
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

    @Column(name = "USER_PREFERENCE")
    private int user_preference;

    @Column(name = "EMAILID")
    private String emailID;

    @Column(name = "STATUS")
    private String status;

}
