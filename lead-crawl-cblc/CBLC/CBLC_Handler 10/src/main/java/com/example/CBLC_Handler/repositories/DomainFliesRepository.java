package com.example.CBLC_Handler.repositories;

import com.example.CBLC_Handler.entities.DomainFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DomainFliesRepository extends JpaRepository<DomainFiles, Integer> {

    @Query(value = "select count(*) from Domain_Files where DOMAIN_GEN_STATUS = 'SCHEDULED' ",nativeQuery = true)
    int SC_DG_FileCount();

    @Query(value = "select * from Domain_Files where DOMAIN_GEN_STATUS = 'SCHEDULED' limit 1",nativeQuery = true)
    List<DomainFiles> get_Next_Part_File_ForDG();

    @Query(value = "select count(*) from Domain_Files where DOMAIN_GEN_STATUS = 'SCHEDULED' ",nativeQuery = true)
    int FA_DG_FileCount();

    @Query(value = "select * from Domain_Files where DOMAIN_GEN_STATUS = 'SCHEDULED' limit 1",nativeQuery = true)
    List<DomainFiles> get_Next_Part_File_ForDG_FA();

    @Query(value = "select count(*) from Domain_Files where DOMAIN_GEN_STATUS = 'IN_PROGRESS' ",nativeQuery = true)
    int IP_DG_FileCount();

    @Query(value = "select * from Domain_Files where DOMAINNF_FILENAME_WITH_PART = :fileName limit 1",nativeQuery = true)
    List<DomainFiles> get_Part_WithName(@Param("fileName") String fileName);

}
