package businesslogic.processes;

import com.berzellius.integrations.basic.exception.APIAuthException;
import dmodel.LeadFromSite;
import dto.site.Lead;
import dto.site.Result;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by berz on 09.03.2017.
 */
@Service
public interface LeadsFromSiteService {
    LeadFromSite processLeadFromSite(LeadFromSite leadFromSite) throws APIAuthException;

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

    Result newLeadFromSite(List<Lead> leads, String origin, String password);
}
