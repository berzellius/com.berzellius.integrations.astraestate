package com.berzellius.integrations.astraestate.scheduling;

import com.berzellius.integrations.basic.exception.APIAuthException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

/**
 * Created by berz on 14.03.2017.
 */
@Service
public interface SchedulingService {

    void runImportCallsToCRM() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException;

    void runProcessingAddedLeads() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException;

    void runProcessingCallRecords() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException;

    void updateCallTrackingConditions() throws APIAuthException;

    void processLeadsFromSite() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException;
}
