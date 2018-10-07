package com.berzellius.integrations.astraestate.scheduling;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by berz on 15.01.2018.
 */
@Service
public class CallsRecordsScheduling {
    @Autowired
    SchedulingService schedulingService;

    @Scheduled(fixedDelay = 3600000)
    public void runRecordsBatch() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        schedulingService.runProcessingCallRecords();
    }
}
