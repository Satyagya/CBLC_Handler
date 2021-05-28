package com.example.LeadCrawl.services;

import java.util.Set;

public interface EmailGenerationService {

    String generateEmailForCsv(String csvFilePath);

    Set<String> setSendingReductionMails(Set<String> unValidatedEmails);



}
