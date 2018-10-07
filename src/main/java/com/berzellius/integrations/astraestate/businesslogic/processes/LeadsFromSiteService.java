package com.berzellius.integrations.astraestate.businesslogic.processes;

import com.berzellius.integrations.astraestate.dmodel.LeadAdded;
import com.berzellius.integrations.astraestate.dmodel.LeadFromSite;
import com.berzellius.integrations.astraestate.dto.site.CleanLead;
import com.berzellius.integrations.astraestate.dto.site.Lead;
import com.berzellius.integrations.astraestate.dto.site.Result;
import com.berzellius.integrations.basic.exception.APIAuthException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * Created by berz on 09.03.2017.
 */
@Service
public interface LeadsFromSiteService {
    void processCreatedLead(LeadAdded leadId, CleanLead cleanLead) throws APIAuthException;

    void leadFromSiteDataProcessing(LeadFromSite leadFromSite) throws APIAuthException;

    void setPhoneNumberCustomFieldLeads(Long phoneNumberCustomFieldLeads);

    void setPhoneNumberCustomField(Long phoneNumberCustomField);

    void setCommentCustomField(Long commentCustomField);

    void setDefaultUserID(Long defaultUserID);

    void setMarketingChannelContactsCustomField(Long marketingChannelContactsCustomField);

    void setMarketingChannelLeadsCustomField(Long marketingChannelLeadsCustomField);

    void setEmailContactCustomField(Long emailContactCustomField);

    void setEmailContactEnum(String emailContactEnum);

    void setPhoneNumberContactStockField(Long phoneNumberContactStockField);

    void setPhoneNumberStockFieldContactEnumWork(String phoneNumberStockFieldContactEnumWork);

    void setLeadFromSiteTagId(Long leadFromSiteTagId);

    void setSourceLeadsCustomField(Long sourceLeadsCustomField);

    void setSourceContactsCustomField(Long sourceContactsCustomField);

    void setLeadPageFromSiteUrl(Long leadPageFromSiteUrl);

    void setAddtFieldsJoin(HashMap<String, Long> addtFieldsJoin);

    void setRefererCustomField(Long refererCustomField);

    void setRefererCustomFieldSecond(Long refererCustomFieldSecond);

    void setRefererCustomFieldThird(Long refererCustomFieldThird);

    Result newLeadFromSite(List<Lead> leads, String origin, String password);

    void setLeadProcessedTagId(Long leadProcessedTagId);

    void setLeadProcessedTagName(String leadProcessedTagName);
}
