package com.example.CBLC_Handler.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Input_Files")
public class Input_Files {

    @Id
    @Column(name = "id1")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id1;

    @OneToMany
    @JoinColumn(name = "id1", referencedColumnName = "id1")
    private List<Parts_Of_Input_Files> parts;

    @Column(name = "INPUT_FILENAME", unique = true)
    private String input_FileName;

    @Column(name = "NO_OF_PARTS")
    private Integer no_Of_Parts;

    @Column(name = "STAGE")
    private  Integer stage;

    @Column(name = "STAGE_REQUIRED")
    private Integer stage_Required;


    @Column(name = "UPLOAD_DATE")
    private String upload_Date;

    @Column(name = "FINISH_DATE")
    private String finish_Date;

}
