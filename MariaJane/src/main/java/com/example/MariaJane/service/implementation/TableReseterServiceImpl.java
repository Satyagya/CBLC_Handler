package com.example.MariaJane.service.implementation;

import com.example.MariaJane.entity.EmailAccountInformation;
import com.example.MariaJane.repository.EmailAccountInformationRepository;
import com.example.MariaJane.service.TableReseterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class TableReseterServiceImpl implements TableReseterService {

    @Autowired
    private EmailAccountInformationRepository emailAccountInformationRepository;

    @Override
    @Scheduled(fixedDelayString = "${poll.frequency.millis}")
    public void scheduledReseter()
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cal.add(Calendar.DATE, -1);
        List<EmailAccountInformation> accountsToUpdate = emailAccountInformationRepository.accountsToUpdate(formatter.format(cal.getTime()),"UNAVAILABLE");
        if(accountsToUpdate.isEmpty()) {
            log.info("NO ACCOUNTS AVAILABLE TO UPDATE-------------------------------------------------------------------");
        }
        else {
            log.info("UPDATING ACCOUNTS BEFORE DATE : " + formatter.format(cal.getTime()));
            log.info("TOTAL ACCOUNTS LEFT TO MODIFY : " + accountsToUpdate.size());
            Date date = new Date();
            for(EmailAccountInformation ATU : accountsToUpdate)
            {
//              (@Param("id") Long id, @Param("jobstatus") String jobstatus, @Param("count") int count, @Param("date") String date)
                emailAccountInformationRepository.resetCount(Long.valueOf(ATU.getId()),"AVAILABLE", 0, formatter.format(date));
                log.info("UPDATED ID: " + ATU.getId());
            }
        }

    }

}
