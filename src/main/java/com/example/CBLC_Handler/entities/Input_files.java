package com.example.CBLC_Handler.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Input_files")
public class Input_files {

    @Id
    @Column(name = "Input_files_ID1")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @OneToMany(mappedBy = "Input_files", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Integer id1;

    @Column(name = "INPUT_FILENAME")
    private String input_FileName;

    @Column(name = "NO_OF_PARTS")
    private String no_Of_Parts;

    @Column(name = "STAGE")
    private  Integer stage;

    @Column(name = "STAGE_REQUIRED")
    private Integer stage_Required;


    @Column(name = "UPLOAD_DATE")
    private Date upload_Date;

    @Column(name = "FINISH_DATE")
    private Date finish_Date;

}
