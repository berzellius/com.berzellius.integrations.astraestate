package com.berzellius.integrations.astraestate.businesslogic.processes;


import com.berzellius.integrations.astraestate.dmodel.CallRecord;
import com.berzellius.integrations.astraestate.dmodel.TrackedCall;
import com.berzellius.integrations.basic.exception.APIAuthException;
import org.springframework.stereotype.Service;

/**
 * Created by berz on 10.10.2015.
 */
@Service
public interface IncomingCallBusinessProcess {
    void newIncomingCall(TrackedCall call);

    Long getPhoneNumberCustomField();

    Long getDefaultUserId();

    Long getPhoneNumberCustomFieldLeads();

    Long getMarketingChannelContactsCustomField();

    Long getMarketingChannelLeadsCustomField();

    Long getSourceContactsCustomField();

    Long getSourceLeadsCustomField();

    Long getEmailContactCustomField();

    String getEmailContactEnum();

    void setEmailContactEnum(String emailContactEnum);

    void setPhoneNumberContactStockField(Long phoneNumberContactStockField);

    void setPhoneNumberStockFieldContactEnumWork(String phoneNumberStockFieldContactEnumWork);

    void addCallRecordToCRM(CallRecord callRecord) throws APIAuthException;
}
