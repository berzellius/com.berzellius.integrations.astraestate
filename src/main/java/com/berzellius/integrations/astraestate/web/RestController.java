package com.berzellius.integrations.astraestate.web;

import com.berzellius.integrations.astraestate.businesslogic.processes.CallsService;
import com.berzellius.integrations.astraestate.businesslogic.processes.LeadsFromSiteService;
import com.berzellius.integrations.astraestate.businesslogic.processes.LeadsService;
import com.berzellius.integrations.astraestate.dto.site.*;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by berz on 15.06.2016.
 */
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/rest/")
public class RestController extends BaseController {

    @Autowired
    LeadsFromSiteService leadsFromSiteService;

    @Autowired
    LeadsService leadsService;

    @Autowired
    CallsService callsService;


    @RequestMapping(
            value = "call_webhook",
            method = RequestMethod.POST,
            consumes="application/json",
            produces="application/json"
    )
    @ResponseBody
    public Result newCallWebhook(
            @RequestBody
            CallRequest callRequest
    ) throws NotFoundException {
        //System.out.println("webhook! " + callRequest.toString());
        //throw new NotFoundException("out of service!");
        return callsService.newCallFromWebhook(callRequest);
    }

    @RequestMapping(
            value = "call_record",
            method = RequestMethod.POST,
            consumes="application/json",
            produces="application/json"
    )
    @ResponseBody
    public Result newCallRecords(
            @RequestBody
            CallRecordRequest callRecordRequest
    ){
        return callsService.newCallRecords(callRecordRequest);
    }

    @RequestMapping(
            value = "leads_add",
            method = RequestMethod.POST,
            consumes="application/json",
            produces="application/json"
    )
    @ResponseBody
    public Result newLeadAdd(
            @RequestBody
            LeadsAddingRequest leadsAddingRequest
    )
    {
        return  leadsService.newLeadsAddedInCRM(leadsAddingRequest);
    }

    @RequestMapping(
            value = "lead_from_site",
            method = RequestMethod.POST,
            consumes="application/json",
            produces="application/json"
    )
    @ResponseBody
    public Result newLeadFromSite(
            @RequestBody
            LeadRequest leadRequest
    ){
        return leadsFromSiteService.newLeadFromSite(leadRequest.getLeads(), leadRequest.getOrigin(), leadRequest.getPassword());
    }
}

