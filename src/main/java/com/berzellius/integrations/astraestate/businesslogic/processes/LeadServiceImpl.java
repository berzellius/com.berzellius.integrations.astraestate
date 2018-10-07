package com.berzellius.integrations.astraestate.businesslogic.processes;

import com.berzellius.integrations.astraestate.dmodel.LeadAdded;
import com.berzellius.integrations.astraestate.dto.site.LeadAddDTO;
import com.berzellius.integrations.astraestate.dto.site.LeadsAddingRequest;
import com.berzellius.integrations.astraestate.dto.site.Result;
import com.berzellius.integrations.astraestate.repository.LeadAddedRepository;
import com.berzellius.integrations.astraestate.scheduling.SchedulingService;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by berz on 03.12.2017.
 */
@Service
public class LeadServiceImpl implements LeadsService {
    @Autowired
    LeadAddedRepository leadAddedRepository;

    @Autowired
    SchedulingService schedulingService;

    @Override
    public Result newLeadsAddedInCRM(LeadsAddingRequest leadsAddingRequest) {
        if(leadsAddingRequest.getAddedLeads().size() > 0){
            for(LeadAddDTO leadAddDTO : leadsAddingRequest.getAddedLeads()){
                LeadAdded leadAdded = new LeadAdded();
                leadAdded.setState(LeadAdded.State.NEW);
                leadAdded.setLeadId(leadAddDTO.getLeadId());

                leadAddedRepository.save(leadAdded);
            }

            try {
                // todo подвесить nowait блокировку!!
                schedulingService.runProcessingAddedLeads();
            } catch (JobParametersInvalidException e) {
                e.printStackTrace();
            } catch (JobExecutionAlreadyRunningException e) {
                e.printStackTrace();
            } catch (JobRestartException e) {
                e.printStackTrace();
            } catch (JobInstanceAlreadyCompleteException e) {
                e.printStackTrace();
            }
        }
        return new Result("success");
    }
}
