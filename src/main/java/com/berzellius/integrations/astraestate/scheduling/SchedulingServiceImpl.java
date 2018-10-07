package com.berzellius.integrations.astraestate.scheduling;

import com.berzellius.integrations.astraestate.service.CallTrackingSourceConditionService;
import com.berzellius.integrations.basic.exception.APIAuthException;
import com.berzellius.integrations.calltrackingru.dto.api.calltracking.CallTrackingSourceCondition;
import com.berzellius.integrations.service.CallTrackingAPIService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by berz on 14.03.2017.
 */
@Service
public class SchedulingServiceImpl implements SchedulingService {

    @Autowired
    Job newCallsToCRMJob;

    @Autowired
    Job newLeadAddedProcessJob;

    @Autowired
    Job newCallRecordsToCRMJob;

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job leadsFromSiteJob;

    @Autowired
    CallTrackingAPIService callTrackingAPIService;

    @Autowired
    CallTrackingSourceConditionService callTrackingSourceConditionService;


    @Override
    public void runImportCallsToCRM() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addDate("start", new Date());

        System.out.println("START import calls to CRM!");
        jobLauncher.run(newCallsToCRMJob, jobParametersBuilder.toJobParameters());
    }

    @Override
    public void runProcessingAddedLeads() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addDate("start", new Date());

        System.out.println("START process Added Leads to CRM!");
        jobLauncher.run(newLeadAddedProcessJob, jobParametersBuilder.toJobParameters());
    }

    @Override
    public void runProcessingCallRecords() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addDate("start", new Date());

        System.out.println("START process Calls Records to CRM!");
        jobLauncher.run(newCallRecordsToCRMJob, jobParametersBuilder.toJobParameters());
    }

    @Override
    public void updateCallTrackingConditions() throws APIAuthException {
        List<CallTrackingSourceCondition> callTrackingSourceConditions = callTrackingAPIService.getAllMarketingChannelsFromCalltracking();
        List<com.berzellius.integrations.astraestate.dmodel.CallTrackingSourceCondition> callTrackingSourceConditions1 = new ArrayList<>();

        for(CallTrackingSourceCondition callTrackingSourceCondition : callTrackingSourceConditions){
            com.berzellius.integrations.astraestate.dmodel.CallTrackingSourceCondition condition = new com.berzellius.integrations.astraestate.dmodel.CallTrackingSourceCondition();
            condition.setPhonesCount(callTrackingSourceCondition.getPhonesCount());
            condition.setProjectId(callTrackingSourceCondition.getProjectId());
            condition.setSourceName(callTrackingSourceCondition.getSourceName());
            condition.setTruth(callTrackingSourceCondition.getTruth());
            condition.setUtmCampaign(callTrackingSourceCondition.getUtmCampaign());
            condition.setUtmMedium(callTrackingSourceCondition.getUtmMedium());
            condition.setUtmSource(callTrackingSourceCondition.getUtmSource());
            condition.setSourceId(callTrackingSourceCondition.getSourceId());

            callTrackingSourceConditions1.add(condition);
        }

        callTrackingSourceConditionService.updateSources(callTrackingSourceConditions1);
    }

    @Override
    public void processLeadsFromSite() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addDate("start", new Date());

        System.out.println("START process LeadsFromSite!");
        jobLauncher.run(leadsFromSiteJob, jobParametersBuilder.toJobParameters());
    }
}
