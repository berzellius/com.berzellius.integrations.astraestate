import com.berzellius.integrations.amocrmru.dto.api.amocrm.*;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.response.AmoCRMCreatedEntityResponse;
import com.berzellius.integrations.amocrmru.service.AmoCRMService;
import com.berzellius.integrations.astraestate.TestApplication;
import com.berzellius.integrations.astraestate.businesslogic.processes.IncomingCallBusinessProcess;
import com.berzellius.integrations.astraestate.businesslogic.processes.LeadsFromSiteService;
import com.berzellius.integrations.astraestate.businesslogic.processes.LeadsService;
import com.berzellius.integrations.astraestate.dmodel.CallRecord;
import com.berzellius.integrations.astraestate.dmodel.Site;
import com.berzellius.integrations.astraestate.dmodel.TrackedCall;
import com.berzellius.integrations.astraestate.dto.site.LeadAddDTO;
import com.berzellius.integrations.astraestate.dto.site.LeadsAddingRequest;
import com.berzellius.integrations.astraestate.scheduling.SchedulingService;
import com.berzellius.integrations.astraestate.service.SiteService;
import com.berzellius.integrations.basic.exception.APIAuthException;
import com.berzellius.integrations.calltrackingru.dto.api.calltracking.Call;
import com.berzellius.integrations.calltrackingru.dto.api.calltracking.CallTrackingSourceCondition;
import com.berzellius.integrations.service.CallTrackingAPIService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by berz on 06.03.2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestApplication.class, TestBeans.class})
public class TestAmocrm {
    @Autowired
    private AmoCRMService amoCRMService;

    @Autowired
    private LeadsFromSiteService leadsFromSiteService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private CallTrackingAPIService callTrackingAPIService;
    
    @Autowired
    LeadsService leadsService;

    @Autowired
    SchedulingService schedulingService;

    @Autowired
    IncomingCallBusinessProcess incomingCallBusinessProcess;

    //@Test
    public void simpleTest() throws APIAuthException {
        List<AmoCRMContact> crmContacts = amoCRMService.getContactsByQuery("");

        System.out.println(crmContacts);

        List<AmoCRMLead> crmLeads = amoCRMService.getLeadsByQuery("");

        System.out.println(crmLeads);
    }

    @Test
    public void testAddedLeadFromSite(){
        long[] ids = {10260855l};

        LeadsAddingRequest leadsAddingRequest = new LeadsAddingRequest();
        List<LeadAddDTO> leadAddDTOs = new ArrayList<>();
        for(long id : ids){
            System.out.println("work with id#" + id);
            LeadAddDTO leadAddDTO = new LeadAddDTO();
            leadAddDTO.setLeadId(id);
            leadAddDTOs.add(leadAddDTO);
        }
        leadsAddingRequest.setAddedLeads(leadAddDTOs);

        leadsService.newLeadsAddedInCRM(leadsAddingRequest);
    }

    @Test
    public void testProcessCreatedLead() throws APIAuthException {
        //long[] ids = {8022305l, 8022313l, 8022375l};//{5238589l, 8006723l, 8006615l, 7928305l};
        long[] ids = {10259737l};

        for(long id : ids){
            System.out.println("work with id#" + id);
            //leadsFromSiteService.processCreatedLead(id, null);
        }
    }

    //@Test
    public void testSiteService(){
        List<String> strings = new ArrayList<>();
        strings.add("http://ofis.sale");
        strings.add("http://arendbiz.ru");

        List<Site> sites = siteService.getSitesByUrl("http://bc-office.ru/"); //siteRepository.findByUrl(strings);
        //List<Site> sites = siteRepository.findByUrlOrUrl("http://arendbiz.ru", "http://ofis.sale");
        if(sites != null) {
            System.out.println("sites found");
            for (Site site : sites) {
                System.out.println(site.getUrl());
            }
        }
        else{
            System.out.println("sites NOT found");
        }
    }

    //@Test
    public void testCutSlashInTail(){
        String url1 = "http://somesite.url/";
        String url2 = "http://somesite.url";
        String url3 = siteService.cutSlashInTail(url1);
        String url4 = siteService.cutSlashInTail(url2);

        System.out.println(url3);
        System.out.println(url4);

        Assert.isTrue(url3.equals(url2));
        Assert.isTrue((url1.length() - url3.length()) == 1);
    }

    @Test
    public void testWorkWithTags() throws APIAuthException {
        Long[] tagIds = {152251l, 236893l};

        AmoCRMLead lead = new AmoCRMLead();
        lead.setName("Тест работы с тегами");
        lead.tag(tagIds[0], "tiiiiii");

        AmoCRMCreatedEntityResponse crmCreatedEntityResponse = amoCRMService.addLead(lead);

        Long leadId = crmCreatedEntityResponse.getId();
        AmoCRMLead createdLead = amoCRMService.getLeadById(leadId);

        createdLead.tag(tagIds[1], "об");
        amoCRMService.saveByUpdate(createdLead);
        createdLead = amoCRMService.getLeadById(leadId);

        for(AmoCRMTag tag : createdLead.getTags()){
            System.out.println(tag.getId() + ": " + tag.getName());
        }
    }

    @Test
    public void testDomainCheck() throws URISyntaxException {
        String url = "http://zdanie.net/yaroslavl";

        URI uri = new URI(url);
        String domain = uri.getHost();
        domain = domain.startsWith("www.") ? domain.substring(4) : domain;

        System.out.println(domain);
    }

    @Test
    public void testFindSite(){
        String url = "http://bc-office.ru/";
        Site site = siteService.getSiteByUrl(url);
        if(site != null) {
            System.out.println(site.getUrl());
        }
        else{
            System.out.println("site not found");
        }
    }

    @Test
    public void testCallTracking() throws APIAuthException {
        List<CallTrackingSourceCondition> callTrackingSourceConditions = callTrackingAPIService.getAllMarketingChannelsFromCalltracking();
        System.out.println("conds: " + callTrackingSourceConditions.size());
    }

    @Test
    public void testGetAdditionalCallsInfo(){
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(new Date());
        calendar1.set(Calendar.HOUR, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);
        calendar1.set(Calendar.DAY_OF_MONTH, 20);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(new Date());
        calendar2.set(Calendar.HOUR, 23);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.SECOND, 59);
        calendar2.set(Calendar.MILLISECOND, 999);
        calendar2.set(Calendar.DAY_OF_MONTH, 25);


        try {
            List<Call> calls = callTrackingAPIService.getCalls(calendar1.getTime(), calendar2.getTime(), 0l, 10, 5845);
            System.out.println("We have " + calls.size() + " calls");
            for(Call call : calls){
                System.out.println(call.getNumber() + " :: " + call.getParams());
            }
        } catch (APIAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddCallRecord(){
        CallRecord callRecord = new CallRecord();
        callRecord.setCalled_phone("4995585083");
        callRecord.setCalling_phone("9163110563");
        callRecord.setLink("https://calltracking.ru/shareRecords/2018-01-15/1516020946.2195302.mp3");
        callRecord.setState(CallRecord.State.NEW);

        Calendar calendar = Calendar.getInstance();
        //2018-01-15 15:55:46
        calendar.set(2018, Calendar.JANUARY, 15);
        callRecord.setDt(calendar.getTime());

        try {
            incomingCallBusinessProcess.addCallRecordToCRM(callRecord);
        } catch (APIAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTrackedCallFromCIAN(){
        TrackedCall call = new TrackedCall();
        call.setSource("CIAN");
        call.setState(TrackedCall.State.NEW);
        call.setDtmCreate(new Date());
        call.setId(11111111111l);
        call.setDt(new Date());
        call.setVirtual_number("84951231231");
        call.setNumber("89951233211");
        call.setSiteId(1);

        incomingCallBusinessProcess.newIncomingCall(call);
    }

    /*@Test
    public void testTestTest(){
        try {
            schedulingService.updateCallTrackingConditions();
        } catch (APIAuthException e) {
            e.printStackTrace();
        }
    }*/
}
