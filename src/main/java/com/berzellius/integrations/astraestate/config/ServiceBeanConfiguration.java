package com.berzellius.integrations.astraestate.config;


import com.berzellius.integrations.amocrmru.dto.ErrorHandlers.AmoCRMAPIRequestErrorHandler;
import com.berzellius.integrations.amocrmru.service.AmoCRMService;
import com.berzellius.integrations.amocrmru.service.AmoCRMServiceImpl;
import com.berzellius.integrations.astraestate.businesslogic.processes.*;
import com.berzellius.integrations.astraestate.businesslogic.rules.transformer.FieldsTransformer;
import com.berzellius.integrations.astraestate.businesslogic.rules.transformer.FieldsTransformerImpl;
import com.berzellius.integrations.astraestate.businesslogic.rules.validator.BusinessRulesValidator;
import com.berzellius.integrations.astraestate.businesslogic.rules.validator.BusinessRulesValidatorImpl;
import com.berzellius.integrations.astraestate.scheduling.SchedulingService;
import com.berzellius.integrations.astraestate.scheduling.SchedulingServiceImpl;
import com.berzellius.integrations.astraestate.service.*;
import com.berzellius.integrations.astraestate.settings.APISettings;
import com.berzellius.integrations.calltrackingru.dto.api.errorhandlers.CalltrackingAPIRequestErrorHandler;
import com.berzellius.integrations.service.CallTrackingAPIService;
import com.berzellius.integrations.service.CallTrackingAPIServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by berz on 20.10.14.
 */
@Configuration
public class ServiceBeanConfiguration {

    @Bean
    CallTrackingSourceConditionService callTrackingSourceConditionService(){
        CallTrackingSourceConditionService callTrackingSourceConditionService = new CallTrackingSourceConditionServiceImpl();
        return  callTrackingSourceConditionService;
    }

    @Bean
    CallTrackingAPIService callTrackingAPIService(){
        CallTrackingAPIService callTrackingAPIService = new CallTrackingAPIServiceImpl();
        /*callTrackingAPIService.setApiMethod(HttpMethod.POST);
        callTrackingAPIService.setLoginMethod(HttpMethod.POST);
        callTrackingAPIService.setLogin(APISettings.CallTrackingLogin);
        callTrackingAPIService.setPassword(APISettings.CallTrackingPassword);
        callTrackingAPIService.setProjects(APISettings.CallTrackingAPIProjects);
        callTrackingAPIService.setWebSiteLoginUrl(APISettings.CallTrackingWebLogin);
        callTrackingAPIService.setWebSitePassword(APISettings.CallTrackingWebPassword);
        callTrackingAPIService.setLoginURL(APISettings.CallTrackingAPILoginUrl);
        callTrackingAPIService.setWebSiteLoginUrl(APISettings.CallTrackingLoginUrl);
        callTrackingAPIService.setApiURL(APISettings.CallTrackingAPIUrl);*/

        callTrackingAPIService.setLogin(APISettings.CallTrackingLogin);
        callTrackingAPIService.setLoginURL(APISettings.CallTrackingAPILoginUrl);
        callTrackingAPIService.setPassword(APISettings.CallTrackingPassword);
        callTrackingAPIService.setLoginMethod(HttpMethod.POST);
        callTrackingAPIService.setApiURL(APISettings.CallTrackingAPIUrl);
        callTrackingAPIService.setApiMethod(HttpMethod.POST);
        callTrackingAPIService.setWebSiteLoginUrl(APISettings.CallTrackingLoginUrl);
        callTrackingAPIService.setWebSiteLogin(APISettings.CallTrackingWebLogin);
        callTrackingAPIService.setWebSitePassword(APISettings.CallTrackingWebPassword);
        callTrackingAPIService.setProjects(APISettings.CallTrackingAPIProjects);

        CalltrackingAPIRequestErrorHandler errorHandler = new CalltrackingAPIRequestErrorHandler();
        callTrackingAPIService.setErrorHandler(errorHandler);

        return callTrackingAPIService;
    }

    @Bean
    AmoCRMService amoCRMService(){
        AmoCRMService amoCRMService = new AmoCRMServiceImpl();
        amoCRMService.setApiBaseUrl(APISettings.AmoCRMApiBaseUrl);
        amoCRMService.setLoginUrl(APISettings.AmoCRMLoginUrl);
        amoCRMService.setUserHash(APISettings.AmoCRMHash);
        amoCRMService.setUserLogin(APISettings.AmoCRMUser);


        ArrayList<Long> leadClosedStatusesIds = new ArrayList<>(Arrays.asList(APISettings.AmoCRMLeadClosedStatuses));
        amoCRMService.setLeadClosedStatusesIDs(leadClosedStatusesIds);

        amoCRMService.setMaxRelogins(APISettings.AmoCRMMaxRelogins);


        AmoCRMAPIRequestErrorHandler errorHandler = new AmoCRMAPIRequestErrorHandler();
        amoCRMService.setErrorHandler(errorHandler);

        return amoCRMService;
    }

    @Bean
    BusinessRulesValidator businessRulesValidator(){
        return new BusinessRulesValidatorImpl();
    }

    @Bean
    FieldsTransformer fieldsTransformer(){
        return new FieldsTransformerImpl();
    }

    @Bean
    LeadsFromSiteService leadsFromSiteService(){
        LeadsFromSiteService leadsFromSiteService = new LeadFromSiteServiceImpl();
        leadsFromSiteService.setDefaultUserID(APISettings.AmoCRMDefaultUserID);
        leadsFromSiteService.setMarketingChannelContactsCustomField(APISettings.AmoCRMMarketingChannelContactsCustomField);
        leadsFromSiteService.setMarketingChannelLeadsCustomField(APISettings.AmoCRMMarketingChannelLeadsCustomField);
        leadsFromSiteService.setPhoneNumberCustomField(APISettings.AmoCRMPhoneNumberCustomField);
        leadsFromSiteService.setEmailContactCustomField(APISettings.AmoCRMEmailContactCustomField);
        leadsFromSiteService.setSourceContactsCustomField(APISettings.AmoCRMSourceContactsCustomField);
        leadsFromSiteService.setSourceLeadsCustomField(APISettings.AmoCRMSourceLeadsCustomField);
        leadsFromSiteService.setEmailContactEnum(APISettings.AmoCRMEmailContactEnum);
        leadsFromSiteService.setPhoneNumberContactStockField(APISettings.AmoCRMPhoneNumberStockFieldContact);
        leadsFromSiteService.setPhoneNumberStockFieldContactEnumWork(APISettings.AmoCRMPhoneNumberStockFieldContactEnumWork);
        leadsFromSiteService.setCommentCustomField(APISettings.AMOCRMLeadCommentField);
        leadsFromSiteService.setLeadFromSiteTagId(APISettings.AMOCRMLeadFromSiteTagId);
        leadsFromSiteService.setLeadPageFromSiteUrl(APISettings.AMOCRMLeadSitePageUrl);
        leadsFromSiteService.setRefererCustomField(APISettings.AMOCRMLeadRefererField);
        leadsFromSiteService.setRefererCustomFieldSecond(APISettings.AMOCRMLeadRefererFieldSecond);
        leadsFromSiteService.setRefererCustomFieldThird(APISettings.AMOCRMLeadRefererFieldThird);
        leadsFromSiteService.setLeadProcessedTagId(APISettings.AmoCRMLeadProcessedTagId);
        leadsFromSiteService.setLeadProcessedTagName(APISettings.AmoCRMLeadProcessedTagName);
        leadsFromSiteService.setRoistatVisitLeadCustomField(APISettings.AMOCRMLeadRoistatVisit);
        leadsFromSiteService.setAddtFieldsJoin(APISettings.AMOCRMAddtFieldsJoin());
        return leadsFromSiteService;
    }

    @Bean
    CallsService callsService(){
        CallsService callsService = new CallsServiceImpl();
        return callsService;
    }

    @Bean
    SchedulingService schedulingService(){
        SchedulingService schedulingService = new SchedulingServiceImpl();
        return  schedulingService;
    }

    @Bean
    IncomingCallBusinessProcess incomingCallBusinessProcess(){
        IncomingCallBusinessProcessImpl incomingCallBusinessProcess = new IncomingCallBusinessProcessImpl();
        incomingCallBusinessProcess.setDefaultUserId(APISettings.AmoCRMDefaultUserID);
        incomingCallBusinessProcess.setPhoneNumberCustomField(APISettings.AmoCRMPhoneNumberCustomField);
        incomingCallBusinessProcess.setMarketingChannelContactsCustomField(APISettings.AmoCRMMarketingChannelContactsCustomField);
        incomingCallBusinessProcess.setMarketingChannelLeadsCustomField(APISettings.AmoCRMMarketingChannelLeadsCustomField);
        incomingCallBusinessProcess.setSourceContactsCustomField(APISettings.AmoCRMSourceContactsCustomField);
        incomingCallBusinessProcess.setEmailContactCustomField(APISettings.AmoCRMEmailContactCustomField);
        incomingCallBusinessProcess.setEmailContactEnum(APISettings.AmoCRMEmailContactEnum);
        incomingCallBusinessProcess.setPhoneNumberContactStockField(APISettings.AmoCRMPhoneNumberStockFieldContact);
        incomingCallBusinessProcess.setPhoneNumberStockFieldContactEnumWork(APISettings.AmoCRMPhoneNumberStockFieldContactEnumWork);
        incomingCallBusinessProcess.setSourceLeadsCustomField(APISettings.AmoCRMSourceLeadsCustomField);
        incomingCallBusinessProcess.setSourceContactPhoneMobilePBX(APISettings.AmoCRMContactPhoneMobliePBX);

        return incomingCallBusinessProcess;
    }

    @Bean
    LeadsService leadsService(){
        return new LeadServiceImpl();
    }

    @Bean
    SiteService siteService(){
        return new SiteServiceImpl();
    }

    @Bean
    AmoCRMAccountInfoService amoCRMAccountInfoService() {
        return new AmoCRMAccountInfoServiceImpl();
    }
}
