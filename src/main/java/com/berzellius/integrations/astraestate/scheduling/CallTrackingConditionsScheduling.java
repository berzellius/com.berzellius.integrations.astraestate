package com.berzellius.integrations.astraestate.scheduling;

import com.berzellius.integrations.basic.exception.APIAuthException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by berz on 01.10.2017.
 */
@Service
public class CallTrackingConditionsScheduling {

    @Autowired
    SchedulingService schedulingService;

    /**
     * Периодическое обновление источников calltracking (utm -> РК)
     * @throws JobParametersInvalidException
     * @throws JobExecutionAlreadyRunningException
     * @throws JobRestartException
     * @throws JobInstanceAlreadyCompleteException
     */
    @Scheduled(fixedDelay = 3600000)
    public void runLeadsFromSiteBatch() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        try {
            schedulingService.updateCallTrackingConditions();
        } catch (APIAuthException e) {
            e.printStackTrace();
        }
    }
}
