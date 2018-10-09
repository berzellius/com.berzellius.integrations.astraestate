package com.berzellius.integrations.astraestate.businesslogic.processes;


import com.berzellius.integrations.amocrmru.dto.api.amocrm.*;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.response.AmoCRMCreatedContactsResponse;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.response.AmoCRMCreatedEntityResponse;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.response.AmoCRMCreatedLeadsResponse;
import com.berzellius.integrations.amocrmru.service.AmoCRMService;
import com.berzellius.integrations.astraestate.businesslogic.rules.transformer.FieldsTransformer;
import com.berzellius.integrations.astraestate.businesslogic.rules.validator.BusinessRulesValidator;
import com.berzellius.integrations.astraestate.businesslogic.rules.validator.SimpleFieldsValidationUtil;
import com.berzellius.integrations.astraestate.dmodel.CallRecord;
import com.berzellius.integrations.astraestate.dmodel.Site;
import com.berzellius.integrations.astraestate.dmodel.TrackedCall;
import com.berzellius.integrations.astraestate.repository.CallRecordRepository;
import com.berzellius.integrations.astraestate.repository.SiteRepository;
import com.berzellius.integrations.astraestate.repository.TrackedCallRepository;
import com.berzellius.integrations.basic.exception.APIAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.util.*;

/**
 * Created by berz on 10.10.2015.
 */
@Service
@Transactional
public class IncomingCallBusinessProcessImpl implements IncomingCallBusinessProcess {

    @Autowired
    TrackedCallRepository trackedCallRepository;

    @Autowired
    CallRecordRepository callRecordRepository;

    @Autowired
    AmoCRMService amoCRMService;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    BusinessRulesValidator businessRulesValidator;

    @Autowired
    FieldsTransformer fieldsTransformer;

    @Autowired
    SimpleFieldsValidationUtil simpleFieldsValidationUtil;

    private static final boolean CREATE_TASK_FOR_EACH_CALL = false;
    private static final boolean CREATE_LEAD_IF_ABSENT = true;

    private static final Logger log = LoggerFactory.getLogger(IncomingCallBusinessProcessImpl.class);

    /*
     * Id пользователя по умолчанию, которому назначаются все сделки
     */
    private Long defaultUserId;

    /*
     * Id кастомных полей "номер телефона (API)" для контактов и сделок
     */
    private Long phoneNumberCustomField;
    private Long phoneNumberCustomFieldLeads;

    /*
    *
    * ID поля "телефон" и enum "Рабочий"
     */
    private Long phoneNumberContactStockField;

    private String phoneNumberStockFieldContactEnumWork;

    /**
     * Id поля "email" для контактов и сделок
     */
    private Long emailContactCustomField;
    private String emailContactEnum;

    /*
     * Id кастомных полей "Рекламный канал" для контактов и сделок
     */
    private Long marketingChannelContactsCustomField;
    private Long marketingChannelLeadsCustomField;

    /*
     * Id кастомных полей "Источник" для контактов и сделок
     */
    private Long sourceContactsCustomField;
    private Long sourceLeadsCustomField;

    /*
     * Привязки {id проекта calltracking}=>{enum значение поля "Источник"}
     */
    private HashMap<Integer, Long> siteIdToContactsSource;
    private HashMap<Integer, Long> siteIdToLeadsSource;

    /**
     * Поле "мобильный телефон менеджера" при обработке контактов, созданных
     * при звонках на мобильные номера менеджеров
     */
    private Long sourceContactPhoneMobilePBX;

    private String transformPhone(String phone){
        String res = fieldsTransformer.transform(phone, FieldsTransformer.Transformation.CALL_NUMBER_COMMON);
        //res = fieldsTransformer.transform(res, FieldsTransformer.Transformation.CALL_NUMBER_LEADING_7);
        return res;
    }

    protected void checkContactAndLeadEqualResponsibleUser(AmoCRMContact crmContact, AmoCRMLead crmLead) throws APIAuthException {
        Assert.notNull(crmContact);
        Assert.notNull(crmLead);

        log.info("Checking that contact#".concat(crmContact.getId().toString())
                .concat(" and")
                .concat(" lead#").concat(crmLead.getId().toString())
                .concat(" has equal responsible user")
        );

        if(
                crmContact.getResponsible_user_id() != null &&
                        !crmContact.getResponsible_user_id().equals(crmLead.getResponsible_user_id()))
        {
            log.info("updating responsibe user for lead#".concat(crmLead.getId().toString()));
            crmLead.setResponsible_user_id(crmContact.getResponsible_user_id());
            amoCRMService.saveByUpdate(crmLead);
        }
    }

    protected String getManagerMobilePhoneByAmoCRMContact(AmoCRMContact crmContact) {
        Assert.notNull(crmContact);

        List<AmoCRMCustomField> crmCustomFields = crmContact.getCustom_fields();
        if(crmCustomFields == null)
            return null;

        for(AmoCRMCustomField crmCustomField : crmCustomFields){
            if(
                    crmCustomField.getId().equals(this.getSourceContactPhoneMobilePBX()) &&
                            crmCustomField.getValues() != null &&
                            crmCustomField.getValues().size() > 0){
                return crmCustomField.getValues().get(0).getValue();
            }
        }

        return null;
    }

    @Override
    public void newIncomingCall(TrackedCall call) {
        Assert.notNull(call.getSiteId());
        Assert.notNull(call.getNumber());
        Assert.notNull(call.getState());

        log.info("Work with new call from number: " + call.getNumber());

        try {
            if(businessRulesValidator.validate(call)) {
                log.info("Call was succesfully validated!");
                processCall(call);
            }
            else{
                log.error("Call object has not pass validation!");
            }
            call.setState(TrackedCall.State.DONE);
            trackedCallRepository.save(call);
        } catch (APIAuthException e) {
            System.out.println("amoCRM auth error!!");
            e.printStackTrace();
        }
    }

    private void processCall(TrackedCall call) throws APIAuthException {
        AmoCRMContact contact = null;

        String number = call.getNumber();
        List<AmoCRMContact> amoCRMContacts = amoCRMService.getContactsByQuery(number);

        if (amoCRMContacts.size() == 0) {
            log.info("Not found contacts for number " + number + "; creating new");
            contact = createContact(call);
        } else {
            log.info("Contacts found for number " + number + ": " + amoCRMContacts.size());

            for (AmoCRMContact amoCRMContact : amoCRMContacts) {
                if (amoCRMContact.getCustom_fields() == null)
                    continue;
                ArrayList<AmoCRMCustomField> crmCustomFields = amoCRMContact.getCustom_fields();

                for (AmoCRMCustomField amoCRMCustomField : crmCustomFields) {
                    if (
                            amoCRMCustomField.getId().equals(this.getPhoneNumberCustomField()) ||
                                    (amoCRMCustomField.getCode() != null && amoCRMCustomField.getCode().equals("PHONE"))
                            ) {

                        for (AmoCRMCustomFieldValue amoCRMCustomFieldValue : amoCRMCustomField.getValues()) {
                            String value = amoCRMCustomFieldValue.getValue();
                            value = transformPhone(value);
                            log.info("phone value: " + value);
                            if (value.length() == 11) {
                                value = value.substring(1);
                            }

                            if (value.equals(number)) {
                                contact = amoCRMContact;
                            }
                        }
                    }
                }
            }
        }

        if (contact == null) {
            log.info("All found contacts for number " + number + " is wrong");
            contact = createContact(call);
            this.workWithContact(contact, call);
        } else {
            log.info("Got contact #" + contact.getId().toString() + " for number " + number + "!");
            this.workWithContact(contact, call);
        }
    }

    private void workWithContact(AmoCRMContact contact, TrackedCall call) throws APIAuthException {
        String number = call.getNumber();
        ArrayList<Long> leadIds = contact.getLinked_leads_id();

        log.info("Work with contact #" + contact.getId());
        checkExistingContactCustomFields(contact, call);

        Integer foundOpenedLeads = 0;
        if (leadIds != null && leadIds.size() != 0) {
            log.info("Leads found. Checking statuses");
            for (Long leadId : leadIds) {
                log.info("Lead #" + leadId);
                AmoCRMLead amoCRMLead = amoCRMService.getLeadById(leadId);

                if (amoCRMLead != null) {
                    if (amoCRMService.getLeadClosedStatusesIDs().contains(amoCRMLead.getStatus_id())) {
                        log.info("Lead is closed");
                    } else {
                        log.info("Lead is open!");
                        //amoCRMLeadToWorkWith = amoCRMLead;
                        foundOpenedLeads++;
                        checkExistingLeadCustomFields(amoCRMLead, call);
                    }
                }
            }
        }

        // Не найдено сделок
        if (foundOpenedLeads == 0 && CREATE_LEAD_IF_ABSENT) {
            log.info("We need to create lead for contact #" + contact.getId());
            AmoCRMLead amoCRMLeadToWorkWith = this.createLeadForContact(contact, call);

            /**
             * НЕ создаем сделку, ждем следующей обработки
             */
        }
        else{
            log.info("Found leads for contact #" + contact.getId() + ". We need to create task for new call");
            this.createTasksForCall(contact, call);
        }
    }

    private void createTasksForCall(AmoCRMContact contact, TrackedCall call) throws APIAuthException {
        if(CREATE_TASK_FOR_EACH_CALL) {
            AmoCRMTask amoCRMTask = new AmoCRMTask();
            amoCRMTask.setResponsible_user_id(getDefaultUserId());
            amoCRMTask.setContact(contact);

            // Связаться с клиентом
            amoCRMTask.setTask_type(1l);
            amoCRMTask.setText("Повторный звонок от клиента!");
            amoCRMTask.setComplete_till(new Date());

            amoCRMService.addTask(amoCRMTask);
        }
    }

    private void checkExistingContactCustomFields(AmoCRMContact contact, TrackedCall call) throws APIAuthException {
        log.info("checking custom fields for contact #".concat(contact.getId().toString()));

        ArrayList<AmoCRMCustomField> crmCustomFields = contact.getCustom_fields();
        Boolean updated = false;

        if(crmCustomFields == null){
            crmCustomFields = new ArrayList<>();
        }

        AmoCRMCustomField marketingChannelCustomField = null;
        AmoCRMCustomField sourceCustomField = null;

        for (AmoCRMCustomField amoCRMCustomField : crmCustomFields) {

            if (amoCRMCustomField.getId().equals(this.getMarketingChannelContactsCustomField())) {
                // Кастомное поле "Рекламный канал"
                marketingChannelCustomField = amoCRMCustomField;
            }

            if (amoCRMCustomField.getId().equals(this.getSourceContactsCustomField())) {
                // Кастомное поле "Источник"
                sourceCustomField = amoCRMCustomField;
            }
        }

        if (
                marketingChannelCustomField == null ||
                        marketingChannelCustomField.getValues() == null ||
                        marketingChannelCustomField.getValues().size() == 0
                ) {
            log.info("'Marketing Channel' is absent or empty");
            if(call.getSource() != null) {
                updated = true;

                String[] sourceField = {call.getSource()};
                contact.addStringValuesToCustomField(this.getMarketingChannelContactsCustomField(), sourceField);
            }
            else{
                log.info("to be honest, sourceField is empty in Call entity too. This is the life");
            }
        }

        if (
                sourceCustomField == null ||
                        sourceCustomField.getValues() == null ||
                        sourceCustomField.getValues().size() == 0
                ) {
            log.info("'Source' is absent or empty");

            if(call.getSiteId() != null && call.getSiteId() != 0 && this.getSiteIdToContactsSource().get(call.getSiteId()) != null) {
                updated = true;
                String[] siteField = {this.getSiteIdToContactsSource().get(call.getSiteId()).toString()};
                contact.addStringValuesToCustomField(this.getSourceContactsCustomField(), siteField);
            }
            else{
                log.info("to be honest, projectID is empty in Call entity too or this project not properly registered. This is the life");
            }
        }

        if (updated) {
            log.info("Contact #".concat(contact.getId().toString()).concat(" has been updated"));

            AmoCRMEntities amoCRMEntities = new AmoCRMEntities();
            ArrayList<AmoCRMContact> amoCRMContacts = new ArrayList<>();
            amoCRMContacts.add(contact);
            amoCRMEntities.setUpdate(amoCRMContacts);
            AmoCRMCreatedContactsResponse response = amoCRMService.editContacts(amoCRMEntities);
            if (response == null) {
                throw new IllegalStateException("No response, but we have not any error message from AmoCRM API!");
            }

            log.info("updating contact response: " + response.getResponse().toString());
        }

    }


    /*private Long getInitialStatusForPipeline(Long pipelineId){

        return null;
    }*/

    private void checkExistingLeadCustomFields(AmoCRMLead amoCRMLead, TrackedCall call) throws APIAuthException {
        Boolean updated = true;
        Long pipelineId = amoCRMLead.getPipeline_id();


        log.info("checking custom fields for lead #".concat(amoCRMLead.getId().toString()));
        ArrayList<AmoCRMCustomField> amoCRMCustomFields = amoCRMLead.getCustom_fields();

        if (amoCRMCustomFields == null) {
            amoCRMCustomFields = new ArrayList<AmoCRMCustomField>();
        }

        AmoCRMCustomField marketingChannelCustomField = null;
        AmoCRMCustomField sourceCustomField = null;

        for (AmoCRMCustomField amoCRMCustomField : amoCRMCustomFields) {

            if (amoCRMCustomField.getId().equals(this.getMarketingChannelLeadsCustomField())) {
                // Кастомное поле "Рекламный канал"
                marketingChannelCustomField = amoCRMCustomField;
            }

            if (amoCRMCustomField.getId().equals(this.getSourceLeadsCustomField())) {
                // Кастомное поле "Источник"
                sourceCustomField = amoCRMCustomField;
            }
        }

        if (
                marketingChannelCustomField == null ||
                        marketingChannelCustomField.getValues() == null ||
                        marketingChannelCustomField.getValues().size() == 0
                ) {
            log.info("'Marketing Channel' is absent or empty");
            if(call.getSource() != null) {
                log.info("call source is " + call.getSource());
                updated = true;
                String[] sourceField = {call.getSource()};
                amoCRMLead.addStringValuesToCustomField(this.getMarketingChannelLeadsCustomField(), sourceField);
            }
            else{
                log.info("to be honest, sourceField is empty in Call entity too. This is the life");
            }
        }

        if (
                sourceCustomField == null ||
                        sourceCustomField.getValues() == null ||
                        sourceCustomField.getValues().size() == 0
                ) {
            log.info("'Source' is absent or empty");

            if(call.getSiteId() != null && this.getSiteIdToLeadsSource().get(call.getSiteId()) != null) {
                updated = true;
                String[] projectField = {this.getSiteIdToLeadsSource().get(call.getSiteId()).toString()};
                amoCRMLead.addStringValuesToCustomField(this.getSourceLeadsCustomField(), projectField);
            }
            else{
                log.info("to be honest, projectID is empty in Call entity too or this project not properly registered. This is the life");
            }
        }

        if (updated) {
            log.info("Lead #".concat(amoCRMLead.getId().toString()).concat(" has been updated"));

            AmoCRMEntities amoCRMEntities = new AmoCRMEntities();
            ArrayList<AmoCRMLead> amoCRMLeads = new ArrayList<>();
            amoCRMLeads.add(amoCRMLead);
            amoCRMEntities.setUpdate(amoCRMLeads);
            AmoCRMCreatedLeadsResponse response = amoCRMService.editLeads(amoCRMEntities);
            if (response == null) {
                throw new IllegalStateException("No response, but we have not any error message from AmoCRM API!");
            }

            log.info(response.getResponse().toString());
        }
    }

    private AmoCRMLead createLeadForContact(AmoCRMContact contact, TrackedCall call) throws APIAuthException {
        String number = call.getNumber();
        AmoCRMLead amoCRMLead = new AmoCRMLead();
        amoCRMLead.setName("Автоматически -> " + contact.getName());

        Long responsibleUserID = contact.getResponsible_user_id() != null ? contact.getResponsible_user_id() : this.getDefaultUserId();

        String[] numberField = {number};
        amoCRMLead.addStringValuesToCustomField(this.getPhoneNumberCustomFieldLeads(), numberField);
        String[] sourceField = {call.getSource()};
        log.info("add " + call.getSource() + " to custom field#" + this.getMarketingChannelLeadsCustomField());
        amoCRMLead.addStringValuesToCustomField(this.getMarketingChannelLeadsCustomField(), sourceField);

        if(
                call.getSiteId() != 0 &&
                        this.getSiteIdToLeadsSource().get(call.getSiteId()) != null
                ) {
            String[] projectField = {this.getSiteIdToLeadsSource().get(call.getSiteId()).toString()};
            if (projectField != null) {
                amoCRMLead.addStringValuesToCustomField(this.getSourceLeadsCustomField(), projectField);
            } else {
                log.info("not found Ids for siteID = " + call.getSiteId());
            }
        }
        else{
            log.info("not found Ids for siteID = " + call.getSiteId());
        }

        log.info("Creating lead for contact #" + contact.getId());

        log.info("run CALL FOR MARKETING tranformer...");
        amoCRMLead = this.transfromLeadFromCallForMarketingDataDetecting(amoCRMLead, call);

        AmoCRMCreatedEntityResponse amoCRMCreatedEntityResponse = amoCRMService.addLead(amoCRMLead);


        if (amoCRMCreatedEntityResponse == null) {
            throw new IllegalStateException("No response, but we have not any error message from AmoCRM API!");
        }

        // Обновляем данные
        AmoCRMLead amoCRMLead1 = amoCRMService.getLeadById(amoCRMCreatedEntityResponse.getId());
        log.info("Adding contact #" + contact.getId() + " to lead #" + amoCRMLead1.getId());
        amoCRMService.addContactToLead(contact, amoCRMLead1);

        return amoCRMLead1;
    }

    private AmoCRMContact createContact(TrackedCall call) throws APIAuthException {
        String number = call.getNumber();

        AmoCRMContact amoCRMContact = new AmoCRMContact();
        amoCRMContact.setName("Звонок:[" + number + "]");
        amoCRMContact.setResponsible_user_id(this.getDefaultUserId());
        String[] fieldNumber = {number};
        String[] extFieldNumber = {"7" + number};
        amoCRMContact.addStringValuesToCustomField(this.getPhoneNumberCustomField(), fieldNumber);
        amoCRMContact.addStringValuesToCustomField(this.getPhoneNumberContactStockField(), extFieldNumber, this.getPhoneNumberStockFieldContactEnumWork());
        String[] fieldSource = {call.getSource()};
        amoCRMContact.addStringValuesToCustomField(this.getMarketingChannelContactsCustomField(), fieldSource);

        if(
                this.getSiteIdToContactsSource().get(call.getSiteId()) != null &&
                        call.getSiteId() != 0
                ) {
            String[] fieldProject = {this.getSiteIdToContactsSource().get(call.getSiteId()).toString()};
            if (fieldProject != null) {
                amoCRMContact.addStringValuesToCustomField(this.getSourceContactsCustomField(), fieldProject);
            } else {
                log.info("not found Ids for siteID = " + call.getSiteId());
            }
        }
        else {
            log.info("not found Ids for siteID = " + call.getSiteId());
        }

        log.info("run AMOCRM Contact TRANSFORM FOR MARKETING");
        amoCRMContact = this.transfromContactFromCallForMarketingDataDetecting(amoCRMContact, call);

        log.info("ready to add contact");
        AmoCRMCreatedEntityResponse resp = amoCRMService.addContact(amoCRMContact);
        if(resp.getId() == null){
            log.error("Request to create contact was sent but no `id created` returned");
            throw new IllegalStateException("Request to create contact was sent but no `id created` returned");
        }
        log.info("created contact#" + resp.getId());

        AmoCRMContact contact = amoCRMService.getContactById(resp.getId());
        return contact;
    }

    @Override
    public void addCallRecordToCRM(CallRecord callRecord) throws APIAuthException {
        Assert.notNull(callRecord.getState());
        Assert.notNull(callRecord.getCalled_phone());
        Assert.notNull(callRecord.getCalling_phone());
        Assert.notNull(callRecord.getLink());

        log.info(
                "Work with call record#" + callRecord.getUuid() + ": " +
                       // callRecord.getDirection().toString() +
                        " from " + callRecord.getCalling_phone() +
                        " to " + callRecord.getCalled_phone());

        Integer noteType = null;
        String phone = callRecord.getCalling_phone();
        noteType = 10;

        List<AmoCRMContact> crmContacts = amoCRMService.getContactsByQuery(phone);

        if(crmContacts != null && crmContacts.size() > 0) {
            for (AmoCRMContact crmContact : crmContacts) {
                log.info("add record to contact #" + crmContact.getId().toString());

                AmoCRMNote amoCRMNote = new AmoCRMNote();
                amoCRMNote.setNote_type(noteType);

                AmoCRMNoteText amoCRMNoteText = new AmoCRMNoteText();
                amoCRMNoteText.setPhone(phone);
                amoCRMNoteText.setCall_status("4");
                amoCRMNoteText.setLink(callRecord.getLink());
                amoCRMNoteText.setDuration(callRecord.getDuration());
                amoCRMNoteText.setUniq(callRecord.getUuid());
                amoCRMNoteText.setCall_result("");
                amoCRMNoteText.setSrc("calltracking");
                amoCRMNote.setText(amoCRMNoteText);

                amoCRMService.addNoteToContact(amoCRMNote, crmContact);
            }

            callRecord.setState(CallRecord.State.DONE);
            callRecordRepository.save(callRecord);
        }
    }

    protected AmoCRMLead transfromLeadFromCallForMarketingDataDetecting(AmoCRMLead amoCRMLead, TrackedCall call){
        HashMap<String, Object> params = new LinkedHashMap<>();
        log.info("preparing to run transformer");

        params.put("virtual_number", call.getVirtual_number());
        params.put("source", call.getSource());
        params.put("sourceLeadsCustomField", this.getSourceLeadsCustomField());

        return fieldsTransformer.transform(
                amoCRMLead,
                FieldsTransformer.Transformation.AMOCRM_LEADFROMCALL_MARKETING,
                params
        );
    }

    protected AmoCRMContact transfromContactFromCallForMarketingDataDetecting(AmoCRMContact amoCRMContact, TrackedCall call){
        HashMap<String, Object> params = new LinkedHashMap<>();
        log.info("preparing to run transformer");

        params.put("virtual_number", call.getVirtual_number());
        params.put("source", call.getSource());
        params.put("sourceContactsCustomField", this.getSourceContactsCustomField());

        return fieldsTransformer.transform(
                amoCRMContact,
                FieldsTransformer.Transformation.AMOCRM_CONTACTFROMCALL_MARKETING,
                params
        );
    }




    @Override
    public Long getPhoneNumberCustomField() {
        return phoneNumberCustomField;
    }

    public void setPhoneNumberCustomField(Long phoneNumberCustomField) {
        this.phoneNumberCustomField = phoneNumberCustomField;
    }

    @Override
    public Long getDefaultUserId() {
        return defaultUserId;
    }

    public void setDefaultUserId(Long defaultUserId) {
        this.defaultUserId = defaultUserId;
    }

    @Override
    public Long getPhoneNumberCustomFieldLeads() {
        return phoneNumberCustomFieldLeads;
    }

    public void setPhoneNumberCustomFieldLeads(Long phoneNumberCustomFieldLeads) {
        this.phoneNumberCustomFieldLeads = phoneNumberCustomFieldLeads;
    }

    @Override
    public Long getMarketingChannelContactsCustomField() {
        return marketingChannelContactsCustomField;
    }

    public void setMarketingChannelContactsCustomField(Long marketingChannelContactsCustomField) {
        this.marketingChannelContactsCustomField = marketingChannelContactsCustomField;
    }

    @Override
    public Long getMarketingChannelLeadsCustomField() {
        return marketingChannelLeadsCustomField;
    }

    public void setMarketingChannelLeadsCustomField(Long marketingChannelLeadsCustomField) {
        this.marketingChannelLeadsCustomField = marketingChannelLeadsCustomField;
    }

    @Override
    public Long getSourceContactsCustomField() {
        return sourceContactsCustomField;
    }

    public void setSourceContactsCustomField(Long sourceContactsCustomField) {
        this.sourceContactsCustomField = sourceContactsCustomField;
    }

    @Override
    public Long getSourceLeadsCustomField() {
        return sourceLeadsCustomField;
    }

    public void setSourceLeadsCustomField(Long sourceLeadsCustomField) {
        this.sourceLeadsCustomField = sourceLeadsCustomField;
    }

/*
    public HashMap<Integer, Long> getSiteIdToContactsSource() {
        return siteIdToContactsSource;
    }

    public void setSiteIdToContactsSource(HashMap<Integer, Long> siteIdToContactsSource) {
        this.siteIdToContactsSource = siteIdToContactsSource;
    }
*/

    public HashMap<Integer, Long> getSiteIdToLeadsSource() {
        if(siteIdToLeadsSource == null){
            this.setSiteIdToLeadsSource(new HashMap<>());
        }


        if(siteIdToLeadsSource.size() == 0){
            log.info("updating siteIdToLeadsSource in incomingBusinessProcessImpl");
            List<Site> sites = (List<Site>) siteRepository.findAll();
            for(Site site : sites){
                siteIdToLeadsSource.put(site.getCallTrackingSiteId(), Long.decode(site.getCrmLeadSourceId()));
            }
        }
        return siteIdToLeadsSource;
    }

    public HashMap<Integer, Long> getSiteIdToContactsSource() {
        if(siteIdToContactsSource == null){
            this.setSiteIdToContactsSource(new HashMap<>());
        }

        if(siteIdToContactsSource.size() == 0){
            log.info("updating siteIdToContactsSource in incomingBusinessProcessImpl");
            List<Site> sites = (List<Site>) siteRepository.findAll();
            for(Site site : sites){
                siteIdToContactsSource.put(site.getCallTrackingSiteId(), Long.decode(site.getCrmContactSourceId()));
            }
        }

        log.info("sites: " + siteIdToContactsSource.toString());

        return siteIdToContactsSource;
    }

    public void setSiteIdToLeadsSource(HashMap<Integer, Long> siteIdToLeadsSource) {
        this.siteIdToLeadsSource = siteIdToLeadsSource;
    }

    public void setEmailContactCustomField(Long emailContactCustomField) {
        this.emailContactCustomField = emailContactCustomField;
    }

    @Override
    public Long getEmailContactCustomField() {
        return emailContactCustomField;
    }

    @Override
    public String getEmailContactEnum() {
        return emailContactEnum;
    }

    @Override
    public void setEmailContactEnum(String emailContactEnum) {
        this.emailContactEnum = emailContactEnum;
    }


    public void setSiteIdToContactsSource(HashMap<Integer, Long> siteIdToContactsSource) {
        this.siteIdToContactsSource = siteIdToContactsSource;
    }

    public Long getPhoneNumberContactStockField() {
        return phoneNumberContactStockField;
    }

    @Override
    public void setPhoneNumberContactStockField(Long phoneNumberContactStockField) {
        this.phoneNumberContactStockField = phoneNumberContactStockField;
    }

    public String getPhoneNumberStockFieldContactEnumWork() {
        return phoneNumberStockFieldContactEnumWork;
    }

    @Override
    public void setPhoneNumberStockFieldContactEnumWork(String phoneNumberStockFieldContactEnumWork) {
        this.phoneNumberStockFieldContactEnumWork = phoneNumberStockFieldContactEnumWork;
    }

    public Long getSourceContactPhoneMobilePBX() {
        return sourceContactPhoneMobilePBX;
    }

    public void setSourceContactPhoneMobilePBX(Long sourceContactPhoneMobilePBX) {
        this.sourceContactPhoneMobilePBX = sourceContactPhoneMobilePBX;
    }
}
