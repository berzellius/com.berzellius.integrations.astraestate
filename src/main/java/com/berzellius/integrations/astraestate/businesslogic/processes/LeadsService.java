package com.berzellius.integrations.astraestate.businesslogic.processes;

import com.berzellius.integrations.astraestate.dto.site.LeadsAddingRequest;
import com.berzellius.integrations.astraestate.dto.site.Result;
import org.springframework.stereotype.Service;

/**
 * Created by berz on 03.12.2017.
 */
@Service
public interface LeadsService {
    Result newLeadsAddedInCRM(LeadsAddingRequest leadsAddingRequest);
}
