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

    @Column(name = "START_STAGE")
    private  Integer stage_start;

    @Column(name = "END_STAGE")
    private  Integer stage_end;

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

    public Integer getStage_start() {
        return stage_start;
    }

    public void setStage_start(Integer stage_start) {
        this.stage_start = stage_start;
    }

    public Integer getStage_end() {
        return stage_end;
    }

    public void setStage_end(Integer stage_end) {
        this.stage_end = stage_end;
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
