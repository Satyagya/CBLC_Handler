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
public class Input_File {

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

    public Integer getId1() {
        return id1;
    }

    public void setId1(Integer id1) {
        this.id1 = id1;
    }

    public String getInput_FileName() {
        return input_FileName;
    }

    public void setInput_FileName(String input_FileName) {
        this.input_FileName = input_FileName;
    }

    public String getNo_Of_Parts() {
        return no_Of_Parts;
    }

    public void setNo_Of_Parts(String no_Of_Parts) {
        this.no_Of_Parts = no_Of_Parts;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public Integer getStage_Required() {
        return stage_Required;
    }

    public void setStage_Required(Integer stage_Required) {
        this.stage_Required = stage_Required;
    }

    public Date getUpload_Date() {
        return upload_Date;
    }

    public void setUpload_Date(Date upload_Date) {
        this.upload_Date = upload_Date;
    }

    public Date getFinish_Date() {
        return finish_Date;
    }

    public void setFinish_Date(Date finish_Date) {
        this.finish_Date = finish_Date;
    }
}
