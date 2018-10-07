package com.berzellius.integrations.astraestate.businesslogic.processes;


import com.berzellius.integrations.astraestate.dto.site.CallRecordRequest;
import com.berzellius.integrations.astraestate.dto.site.CallRequest;
import com.berzellius.integrations.astraestate.dto.site.Result;
import org.springframework.stereotype.Service;

/**
 * Created by berz on 14.03.2017.
 */
@Service
public interface CallsService {
    Result newCallFromWebhook(CallRequest callRequest);

    Result newCallRecords(CallRecordRequest callRecordRequest);

}
