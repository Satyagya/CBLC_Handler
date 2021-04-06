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

    public Integer getId1() {
        return id1;
    }

    public void setId1(Integer id1) {
        this.id1 = id1;
    }

    public List<Parts_Of_Input_Files> getParts() {
        return parts;
    }

    public void setParts(List<Parts_Of_Input_Files> parts) {
        this.parts = parts;
    }

    public String getInput_FileName() {
        return input_FileName;
    }

    public void setInput_FileName(String input_FileName) {
        this.input_FileName = input_FileName;
    }

    public Integer getNo_Of_Parts() {
        return no_Of_Parts;
    }

    public void setNo_Of_Parts(Integer no_Of_Parts) {
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

    public String getUpload_Date() {
        return upload_Date;
    }

    public void setUpload_Date(String upload_Date) {
        this.upload_Date = upload_Date;
    }

    public String getFinish_Date() {
        return finish_Date;
    }

    public void setFinish_Date(String finish_Date) {
        this.finish_Date = finish_Date;
    }
}
