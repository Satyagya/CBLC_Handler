package com.example.MariaJane.repository;


import com.example.MariaJane.entity.EmailAccountInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository("com.example.LeadCrawl.repository.EmailAccountInformationRepository")
public interface EmailAccountInformationRepository extends JpaRepository<EmailAccountInformation, Long> {

    @Query(value = "select * from EMAIL_ACCOUNT_INFORMATION e where e.LAST_UPDATED <=:date and e.JOB_STATUS=:jobstatus",nativeQuery = true)
    List<EmailAccountInformation> accountsToUpdate(@Param("date") String date, @Param("jobstatus") String jobstatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "UPDATE EMAIL_ACCOUNT_INFORMATION ei SET ei.JOB_STATUS=:jobstatus, ei.COUNT=:count, ei.LAST_UPDATED=:date WHERE ei.ID =:id", nativeQuery = true)
    void resetCount(@Param("id") Long id, @Param("jobstatus") String jobstatus, @Param("count") int count, @Param("date") String date);

}
