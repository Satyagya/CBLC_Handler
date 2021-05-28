package com.example.LeadCrawl.repository;

import com.example.LeadCrawl.entity.CompanyLeadsConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyLeadsConfigRepository extends JpaRepository<CompanyLeadsConfig, Long> {

    CompanyLeadsConfig findByProductName(String productName);

}
