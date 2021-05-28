package com.example.CBLC_Handler.repositories;

import com.example.CBLC_Handler.entities.PartsOfInputFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartsOfInputFilesRepository extends JpaRepository<PartsOfInputFiles, Integer> {

    @Query(value = "select count(*) from Parts_Of_Input_Files where LEAD_CRAWL_STATUS = 'SCHEDULED' ",nativeQuery = true)
    int SC_LC_FileCount();

    @Query(value = "select * from Parts_Of_Input_Files where LEAD_CRAWL_STATUS = 'SCHEDULED' limit 1",nativeQuery = true)
    List<PartsOfInputFiles> get_Next_Part_File_ForLC();

    @Query(value = "select count(*) from Parts_Of_Input_Files where LEAD_CRAWL_STATUS = 'IN_PROGRESS'",nativeQuery = true)
    int IP_LC_FileCount();

    @Query(value = "select count(*) from Parts_Of_Input_Files where LEAD_CRAWL_STATUS = 'FAILED'",nativeQuery = true)
    int FA_LC_FileCount();

    @Query(value = "select * from Parts_Of_Input_Files where LEAD_CRAWL_STATUS = 'FAILED' limit 1",nativeQuery = true)
    List<PartsOfInputFiles> get_Next_Part_File_ForLC_FA();


    @Query(value = "select * from Parts_Of_Input_Files where INPUT_FILENAME_WITH_PART = :fileName ",nativeQuery = true)
    List<PartsOfInputFiles> get_Part_WithName(@Param("fileName") String fileName);


    @Query(value = "select count(*) from Parts_Of_Input_Files where DOMAIN_CHECK_STATUS = 'SCHEDULED' ",nativeQuery = true)
    int SC_DC_FileCount();

    @Query(value = "select * from Parts_Of_Input_Files where DOMAIN_CHECK_STATUS = 'SCHEDULED' limit 1",nativeQuery = true)
    List<PartsOfInputFiles> get_Next_Part_File_ForDC();

    @Query(value = "select count(*) from Parts_Of_Input_Files where DOMAIN_CHECK_STATUS = 'SCHEDULED' ",nativeQuery = true)
    int FA_DC_FileCount();

    @Query(value = "select * from Parts_Of_Input_Files where DOMAIN_CHECK_STATUS = 'SCHEDULED' limit 1",nativeQuery = true)
    List<PartsOfInputFiles> get_Next_Part_File_ForDC_FA();


    @Query(value = "select count(*) from Parts_Of_Input_Files where DOMAIN_CHECK_STATUS = 'IN_PROGRESS'",nativeQuery = true)
    int IP_DC_FileCount();

    @Query(value = "select count(*) from Parts_Of_Input_Files where EMAIL_GEN_STATUS_FOR_FOUND_DOMAINS = 'SCHEDULED'",nativeQuery = true)
    int SC_EG_FileCount();

    @Query(value = "select * from Parts_Of_Input_Files where EMAIL_GEN_STATUS_FOR_FOUND_DOMAINS = 'SCHEDULED' limit 1",nativeQuery = true)
    List<PartsOfInputFiles> get_Next_Part_File_ForEG();

    @Query(value = "select count(*) from Parts_Of_Input_Files where EMAIL_GEN_STATUS_FOR_FOUND_DOMAINS = 'IN_PROGRESS'",nativeQuery = true)
    int IP_EG_FileCount();

    @Query(value = "select count(*) from Parts_Of_Input_Files where EMAIL_GEN_STATUS_FOR_NOTFOUND_DOMAINS = 'IN_PROGRESS'",nativeQuery = true)
    int IP_EG_NF_FileCount();


    @Query(value = "select count(*) from Parts_Of_Input_Files where EMAIL_GEN_STATUS_FOR_NOTFOUND_DOMAINS = 'SCHEDULED' ",nativeQuery = true)
    int SC_EG_FileCount_ForFNF();

    @Query(value = "select * from Parts_Of_Input_Files where EMAIL_GEN_STATUS_FOR_NOTFOUND_DOMAINS = 'SCHEDULED' limit 1",nativeQuery = true)
    List<PartsOfInputFiles> get_Next_Part_File_ForEG_ForFNF();

    @Query(value = "select count(*) from Parts_Of_Input_Files where EMAIL_GEN_STATUS_FOR_NOTFOUND_DOMAINS = 'COMPLETED' AND EMAIL_GEN_STATUS_FOR_FOUND_DOMAINS = 'COMPLETED' AND STATUS = 'NOT_COMPLETED' ",nativeQuery = true)
    int count_FNF_merger();

    @Query(value = "select * from Parts_Of_Input_Files where EMAIL_GEN_STATUS_FOR_NOTFOUND_DOMAINS = 'COMPLETED' AND EMAIL_GEN_STATUS_FOR_FOUND_DOMAINS = 'COMPLETED' AND STATUS = 'NOT_COMPLETED' limit 1",nativeQuery = true)
    List<PartsOfInputFiles> get_Next_FNF_Part();

    @Query(value = "select count(*) from Parts_Of_Input_Files where INPUT_FILENAME_WITH_PART = :fileName limit 1",nativeQuery = true)
    int file_present(@Param("fileName") String fileName);

    @Query(value = "select count(*) from Parts_Of_Input_Files where INPUT_FILENAME_WITH_PART = :fileName AND LEAD_CRAWL_STATUS = 'COMPLETED' limit 1",nativeQuery = true)
    int file_completed_LC(@Param("fileName")String fileName);

    @Query(value = "select * from Parts_Of_Input_Files where INPUT_FILENAME_WITH_PART = :fileName AND LEAD_CRAWL_STATUS = 'COMPLETED' limit 1",nativeQuery = true)
    List<PartsOfInputFiles> get_leadcrawl_file(@Param("fileName")String fileName);


    @Query(value = "select count(*) from Parts_Of_Input_Files where STATUS = 'COMPLETED'",nativeQuery = true)
    int count_Final_merger();

    @Query(value = "select * from Parts_Of_Input_Files where STATUS = 'COMPLETED' limit 1",nativeQuery = true)
    List<PartsOfInputFiles> get_Next_Final_Part();


    @Query(value = "select count(*) from Parts_Of_Input_Files where INPUT_FILENAME_WITH_PART = :fileName AND  STATUS = 'COMPLETED' limit 1",nativeQuery = true)
    int file_completed_EG(@Param("fileName")String fileName);





}
