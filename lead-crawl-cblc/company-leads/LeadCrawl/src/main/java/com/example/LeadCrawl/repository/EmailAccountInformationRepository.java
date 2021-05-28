package com.example.LeadCrawl.repository;

import com.example.LeadCrawl.entity.EmailAccountInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository("com.example.LeadCrawl.repository.EmailAccountInformationRepository")
public interface EmailAccountInformationRepository extends JpaRepository<EmailAccountInformation, Long> {

    @Query(value = "select * from EMAIL_ACCOUNT_INFORMATION e where e.STATUS=:status and e.JOB_STATUS=:jobstatus",nativeQuery = true)
    List<EmailAccountInformation> selectAccount(@Param("status") String status, @Param("jobstatus") String jobStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "UPDATE EMAIL_ACCOUNT_INFORMATION ei SET ei.COUNT=:count, ei.LAST_UPDATED=:date WHERE ei.USERNAME=:username", nativeQuery = true)
    void updateCount(@Param("username") String userName, @Param("count") int count, @Param("date") String date);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "UPDATE EMAIL_ACCOUNT_INFORMATION ei SET ei.JOB_STATUS=:jobstatus, ei.LAST_UPDATED=:date WHERE ei.USERNAME=:username",nativeQuery = true)
    void updateJobStatus(@Param("username") String username, @Param("jobstatus") String jobstatus, @Param("date") String date);

}
