package com.berzellius.integrations.astraestate.businesslogic.processes;

import com.berzellius.integrations.amocrmru.dto.api.amocrm.*;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.response.AmoCRMCreatedLeadsResponse;
import com.berzellius.integrations.amocrmru.service.AmoCRMService;
import com.berzellius.integrations.amocrmru.service.AmoCRMServiceImpl;
import com.berzellius.integrations.astraestate.businesslogic.rules.transformer.FieldsTransformer;
import com.berzellius.integrations.astraestate.businesslogic.rules.validator.BusinessRulesValidator;
import com.berzellius.integrations.astraestate.dmodel.CallTrackingSourceCondition;
import com.berzellius.integrations.astraestate.dmodel.LeadAdded;
import com.berzellius.integrations.astraestate.dmodel.LeadFromSite;
import com.berzellius.integrations.astraestate.dmodel.Site;
import com.berzellius.integrations.astraestate.dto.site.CleanLead;
import com.berzellius.integrations.astraestate.dto.site.Lead;
import com.berzellius.integrations.astraestate.dto.site.Result;
import com.berzellius.integrations.astraestate.repository.LeadAddedRepository;
import com.berzellius.integrations.astraestate.repository.LeadFromSiteRepository;
import com.berzellius.integrations.astraestate.scheduling.SchedulingService;
import com.berzellius.integrations.astraestate.service.CallTrackingSourceConditionService;
import com.berzellius.integrations.astraestate.service.SiteService;
import com.berzellius.integrations.basic.exception.APIAuthException;
import com.berzellius.integrations.service.CallTrackingAPIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by berz on 09.03.2017.
 */
@Service
@Transactional
public class LeadFromSiteServiceImpl implements LeadsFromSiteService {

    private static final Integer RETRIES_DEFAULT = 3;

    @Autowired
    CallTrackingSourceConditionService callTrackingSourceConditionService;

    @Autowired
    BusinessRulesValidator businessRulesValidator;

    @Autowired
    FieldsTransformer fieldsTransformer;

    @Autowired
    LeadFromSiteRepository leadFromSiteRepository;

    @Autowired
    AmoCRMService amoCRMService;

    @Autowired
    SiteService siteService;

    @Autowired
    CallTrackingAPIService callTrackingAPIService;

    @Autowired
    LeadAddedRepository leadAddedRepository;

    @Autowired
    SchedulingService schedulingService;

    private Long phoneNumberCustomFieldLeads;
    private Long phoneNumberCustomField;
    private Long commentCustomField;
    private Long defaultUserID;
    private Long marketingChannelContactsCustomField;
    private Long marketingChannelLeadsCustomField;
    private Long emailContactCustomField;
    private String emailContactEnum;
    private Long phoneNumberContactStockField;
    private String phoneNumberStockFieldContactEnumWork;

    private Long leadFromSiteTagId;
    private Long leadPageFromSiteUrl;

    private Long sourceLeadsCustomField;
    private Long sourceContactsCustomField;

    private HashMap<Integer, Long> siteIdToLeadsSource;

    private HashMap<String, Long> addtFieldsJoin;

    private Long refererCustomField;
    private Long refererCustomFieldSecond;
    private Long refererCustomFieldThird;

    private Long leadProcessedTagId;
    private String leadProcessedTagName;

    private Long roistatVisitLeadCustomField;

    private static final Logger log = LoggerFactory.getLogger(LeadsFromSiteService.class);

    private String transformPhone(String phone){
        String res = fieldsTransformer.transform(phone, FieldsTransformer.Transformation.CALL_NUMBER_COMMON);
        res = fieldsTransformer.transform(res, FieldsTransformer.Transformation.CALL_NUMBER_LEADING_7);
        return res;
    }

    private static final Pattern p = Pattern.compile("\\b(([\\w-]+://?|www[.])[^\\s()<>]+(?:\\([\\w\\d]+\\)|([^[:punct:]\\s]|/)))", Pattern.CASE_INSENSITIVE);
    //private static final Pattern p = Pattern.compile("http");

    @Override
    public void processCreatedLead(LeadAdded leadAdded, CleanLead cleanLead) throws APIAuthException {
        Long leadId = leadAdded.getLeadId();
        AmoCRMLead lead = amoCRMService.getLeadById(leadId);
        if(lead == null){
            log.error("Lead# " + leadId + " is not exists!");
            leadAdded.setState(LeadAdded.State.DONE);
            leadAddedRepository.save(leadAdded);
            return;
        }

        log.info("current checking tag id is " + this.getLeadFromSiteTagId());
        log.info("lead#" + leadId);
        List<AmoCRMContactsLeadsLink> assocContactsList = amoCRMService.getContactsLeadsLinksByLead(lead);

        if(
                lead.hasTagById(this.getLeadFromSiteTagId()) &&
                        !lead.hasTagById(this.getLeadProcessedTagId())
                ) {
                    // сделка тегирована как поступившая с сайта заявка.
                    log.info("This is lead from site");
                    log.info(lead.getName());

                    if (lead.getName() != null) {
                        // автоматически создаваемые сделки содержат в названии url. Его мы перенесем в отдельное поле, а сделке дадим более удобное имя
                        Matcher m = p.matcher(lead.getName());

                        if (m.find()) {
                            log.info("Looks like lead name contains url. Add it to specific field");
                            log.info(m.group(0));

                            if (m.group(0) != null) {
                                String[] url = {m.group(0)};
                                lead.addStringValuesToCustomField(this.getLeadPageFromSiteUrl(), url);
                            }

                            if (assocContactsList.size() > 0) {
                                log.info("Found associated contacts; gotta change name of lead");
                                AmoCRMContactsLeadsLink contactsLeadsLink = assocContactsList.get(0);
                                log.info("Contact id#" + contactsLeadsLink.getContact_id());
                                AmoCRMContact crmContact = amoCRMService.getContactById(contactsLeadsLink.getContact_id());

                                if (crmContact != null) {
                                    lead.setName("Сделка по заявке с сайта (" + crmContact.getName() + ")");
                                }
                            } else {
                                log.info("Associated contacts not found");
                            }
                        } else {
                            log.info("Lead name is just plain text");
                        }
                    }

                    // в this.getAddtFieldsJoin() лежат пары "код кастомного поля" -> "id другого поля, куда копируем значения из исходного"
                    log.info("Checking fields to copy data");
                    ArrayList<AmoCRMCustomField> crmCustomFields = (ArrayList<AmoCRMCustomField>) lead.getCustom_fields().clone();
                    HashMap<Long, ArrayList<String>> joiningFieldsValues = new LinkedHashMap<>();


                    for (AmoCRMCustomField crmCustomField : crmCustomFields) {
                        log.info("check field with name " + crmCustomField.getName() + ", id #" + crmCustomField.getId().toString());
                        if (this.getAddtFieldsJoin().containsKey(crmCustomField.getName())) {
                            Long fieldId = this.getAddtFieldsJoin().get(crmCustomField.getName());
                            // текст с учетом множественных реализаций
                            String text = String.join(" ", crmCustomField.allStringValues());

                            if(fieldId.equals(this.getCommentCustomField())) {
                                // Поле "комментарий"
                                AmoCRMNote note = new AmoCRMNote();
                                AmoCRMNoteSimpleText amoCRMNoteText = new AmoCRMNoteSimpleText();
                                log.info("adding notes with text " + text);
                                amoCRMNoteText.setText(text);
                                note.setText(amoCRMNoteText);
                                note.setNote_type(4);
                                amoCRMService.addNoteToLead(note, lead);
                            }
                            else{
                                // Копируем набор значений этого поля в объединяющее поле fieldId.
                                /**
                                 *  todo это случай, когда нужно сгруппировать поля в пределах сделки
                                 *  если хотим группировать значения поля в сделке и копировать их наборы в контакты, то
                                 *  нужно:
                                 *  1) вести списки id полей контактов и сделок (подгружать по требованию 1 раз)
                                 *  2) если id поля попал в список полей сделок, обновляем сделку
                                 *  3) если id поля попал в список контактов, обновляем все привязанные контакты
                                 */
                                AmoCRMServiceImpl.AmoCRMFieldDescription description = amoCRMService.getFieldDescription(fieldId);
                                if(description != null && description.getType() != null) {
                                    switch (description.getType()) {
                                        case CONTACT:
                                            // Значения объединяются в контакт
                                            if(assocContactsList != null){
                                                for(AmoCRMContactsLeadsLink leadsLink : assocContactsList){
                                                    log.info("updating field#" + fieldId + " of contact#" + leadsLink.getContact_id());
                                                    AmoCRMContact crmContact = amoCRMService.getContactById(leadsLink.getContact_id());
                                                    if(crmContact != null){
                                                        crmContact.addStringValuesToCustomField(fieldId, crmCustomField.allStringValues());
                                                    }
                                                }
                                            }
                                            break;
                                        case LEAD:
                                            // Значения объединяются в сделку
                                            log.info("updating field#" + fieldId + " of lead#" + leadId);
                                            lead.addStringValuesToCustomField(fieldId, crmCustomField.allStringValues());
                                            break;
                                    }
                                }
                                else{
                                    log.error("Description of field#" + fieldId + " not detected");
                                }
                            }
                        }
                    }

                    // определяем сайт-источник
                    AmoCRMCustomField refererCustomField = lead.customFieldByFieldId(this.getRefererCustomField());
                    String referer = this.getRefererFromLead(lead);

                    if (referer != null) {
                        log.info("referer: " + referer + ", looking for site..");
                        Site site = siteService.getSiteByUrl(referer);
                        if (site != null) {
                            log.info("Site url: " + site.getUrl());
                            log.info("Field: #" + this.getSourceLeadsCustomField());
                            log.info("Enum: " + site.getCrmLeadSourceId());
                            lead.addStringValuesToCustomField(this.getSourceLeadsCustomField(), new String[]{this.getDomainFromUrl(site.getUrl())}, site.getCrmLeadSourceId());
                        } else {
                            log.warn("Site undefined!");
                        }
                    } else {
                        log.info("Referer is EMPTY");
                    }

                    if(cleanLead != null) {
                        String marketingChannel = this.getMarketingChannelByCleanLead(cleanLead);
                        log.info("Marketing channel == " + marketingChannel);
                        lead.addStringValuesToCustomField(this.getMarketingChannelLeadsCustomField(), new String[]{marketingChannel});
                    }

                    // ставим тег "обработано"
                    lead.tag(this.getLeadProcessedTagId(), this.getLeadProcessedTagName());
                    AmoCRMCreatedLeadsResponse result = amoCRMService.saveByUpdate(lead);


                }

        log.info("it is not lead from site");

        leadAdded.setState(LeadAdded.State.DONE);
        leadAddedRepository.save(leadAdded);
    }

    @Override
    public void leadFromSiteDataProcessing(LeadFromSite leadFromSite) throws APIAuthException {
        UUID uuid = UUID.randomUUID();
        log.info("START Processing data from site about lead " + uuid.toString());

        if(leadFromSite.getLead() != null) {
            Lead lead = leadFromSite.getLead();
            CleanLead cleanLead = lead.toCleanLead();
            String marketingChannel = this.getMarketingChannelByCleanLead(cleanLead);
            Long roistatVisit = lead.getRoistat_visit();


            String search = "";
            if(cleanLead.getPhone() != null){
                search = cleanLead.getPhone();
            }

            if(cleanLead.getEmail() != null){
                search = (search.equals("") ? "" : " ").concat(cleanLead.getEmail());
            }

            if(!search.equals("")) {
                log.info("searching by " + search + " " + uuid.toString());

                List<AmoCRMLead> crmLeads = amoCRMService.getLeadsByQuery(search);
                if (crmLeads.size() > 0) {
                    for(AmoCRMLead crmLead : crmLeads){
                        log.info("lead#" + crmLead.getId() + " " + uuid.toString());

                        if(marketingChannel != null){
                            // определили рекламный канал. нужно распространить на сделки, у которых не определен РК..

                            log.info("marketing channel = " + marketingChannel + " " + uuid.toString());

                            if(crmLead.customFieldIsEmpty(this.getMarketingChannelLeadsCustomField())){
                                log.info("update marketing channel for lead#" + crmLead.getId().toString() + " " + uuid.toString());
                                String[] values = {marketingChannel};
                                crmLead.addStringValuesToCustomField(this.getMarketingChannelLeadsCustomField(), values);
                            }
                            else{
                                log.info("marketing channel is set for lead#" + crmLead.getId().toString() + " " + uuid.toString());
                            }
                        }
                        else{
                            log.warn("marketing channel NOT detected!");
                        }

                        if (roistatVisit != null) {
                            // работаем с roistat visit

                            log.info("roistat visit = " + marketingChannel + " " + uuid.toString());

                            if(crmLead.customFieldIsEmpty(this.getRoistatVisitLeadCustomField())){
                                log.info("update roistat visit for lead#" + crmLead.getId().toString() + " " + uuid.toString());
                                crmLead.addStringValueToCustomField(this.getRoistatVisitLeadCustomField(), roistatVisit.toString());
                            }
                        }
                        else{
                            log.warn("roistat visit NOT detected");
                        }

                        amoCRMService.saveByUpdate(crmLead);
                    }
                }
            }
        }

        log.info("END Processing data from site about lead " + uuid.toString());

        if(leadFromSite.getRetryCount() > 0){
            leadFromSite.setRetryCount(leadFromSite.getRetryCount() - 1);
        }
        else{
            leadFromSite.setState(LeadFromSite.State.DONE);
        }
        leadFromSiteRepository.save(leadFromSite);
    }

    protected String getMarketingChannelByCleanLead(CleanLead cleanLead) throws APIAuthException {
        log.info("getting marketing channel");
        String utmSource = (cleanLead.getUtm_source() != null)? cleanLead.getUtm_source() : "";
        String utmMedium = (cleanLead.getUtm_medium() != null)? cleanLead.getUtm_medium() : "";
        String utmCampaign = (cleanLead.getUtm_campaign() != null)? cleanLead.getUtm_campaign() : "";

        //Integer projectId = callTrackingAPIService.getProjectIdBySite(cleanLead.getOrigin());
        Site site = siteService.getSiteByUrl(cleanLead.getOrigin());
        if (site == null){
            log.error("cant determine site...");
            return null;
        }
        Integer projectId = site.getCallTrackingSiteId();

        log.info("utm = {" + utmSource + ", " + utmMedium + ", " + utmCampaign + "}, project = " + projectId); //cleanLead.getCallTrackingProjectId());

        CallTrackingSourceCondition callTrackingSourceCondition = callTrackingSourceConditionService.getCallTrackingSourceConditionByUtmAndProjectId(utmSource, utmMedium, utmCampaign, projectId);
        return (callTrackingSourceCondition != null)? callTrackingSourceCondition.getSourceName() : null;
    }

    protected String getRefererFromLead(AmoCRMLead lead){
        /*AmoCRMCustomField refererCustomField = lead.customFieldByFieldId(this.getRefererCustomField());
        AmoCRMCustomField refererCustomFieldSecond = lead.customFieldByFieldId(this.getRefererCustomFieldSecond());
        AmoCRMCustomField refererCustomFieldThird = lead.customFieldByFieldId(this.getRefererCustomFieldThird());*/

        AmoCRMCustomField refererStock = lead.customFieldByFieldId(this.getLeadPageFromSiteUrl());
        return  /*(
                    refererCustomField != null &&
                    refererCustomField.allStringValues() != null &&
                    refererCustomField.allStringValues().length > 0
                )?
                    refererCustomField.allStringValues()[0] :
                (
                    refererCustomFieldSecond != null &&
                    refererCustomFieldSecond.allStringValues() != null &&
                    refererCustomFieldSecond.allStringValues().length > 0
                )? refererCustomFieldSecond.allStringValues()[0] :
                (
                        refererCustomFieldThird != null &&
                        refererCustomFieldThird.allStringValues() != null &&
                        refererCustomFieldThird.allStringValues().length > 0
                )? refererCustomFieldThird.allStringValues()[0] :*/
                (
                    refererStock != null &&
                    refererStock.allStringValues() != null &&
                    refererStock.allStringValues().length > 0
                )? refererStock.allStringValues()[0] : null;
    }

    protected String getDomainFromUrl(String url){
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long getPhoneNumberCustomFieldLeads() {
        return phoneNumberCustomFieldLeads;
    }

    @Override
    public void setPhoneNumberCustomFieldLeads(Long phoneNumberCustomFieldLeads) {
        this.phoneNumberCustomFieldLeads = phoneNumberCustomFieldLeads;
    }

    public Long getPhoneNumberCustomField() {
        return phoneNumberCustomField;
    }

    @Override
    public void setPhoneNumberCustomField(Long phoneNumberCustomField) {
        this.phoneNumberCustomField = phoneNumberCustomField;
    }

    public Long getCommentCustomField() {
        return commentCustomField;
    }

    @Override
    public void setCommentCustomField(Long commentCustomField) {
        this.commentCustomField = commentCustomField;
    }

    public Long getDefaultUserID() {
        return defaultUserID;
    }

    @Override
    public void setDefaultUserID(Long defaultUserID) {
        this.defaultUserID = defaultUserID;
    }

    public Long getMarketingChannelContactsCustomField() {
        return marketingChannelContactsCustomField;
    }

    @Override
    public void setMarketingChannelContactsCustomField(Long marketingChannelContactsCustomField) {
        this.marketingChannelContactsCustomField = marketingChannelContactsCustomField;
    }

    public Long getMarketingChannelLeadsCustomField() {
        return marketingChannelLeadsCustomField;
    }

    @Override
    public void setMarketingChannelLeadsCustomField(Long marketingChannelLeadsCustomField) {
        this.marketingChannelLeadsCustomField = marketingChannelLeadsCustomField;
    }

    public Long getEmailContactCustomField() {
        return emailContactCustomField;
    }

    @Override
    public void setEmailContactCustomField(Long emailContactCustomField) {
        this.emailContactCustomField = emailContactCustomField;
    }

    public String getEmailContactEnum() {
        return emailContactEnum;
    }

    @Override
    public void setEmailContactEnum(String emailContactEnum) {
        this.emailContactEnum = emailContactEnum;
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

    public Long getLeadFromSiteTagId() {
        return leadFromSiteTagId;
    }

    @Override
    public void setLeadFromSiteTagId(Long leadFromSiteTagId) {
        this.leadFromSiteTagId = leadFromSiteTagId;
    }

    public Long getSourceLeadsCustomField() {
        return sourceLeadsCustomField;
    }

    @Override
    public void setSourceLeadsCustomField(Long sourceLeadsCustomField) {
        this.sourceLeadsCustomField = sourceLeadsCustomField;
    }

    public Long getSourceContactsCustomField() {
        return sourceContactsCustomField;
    }

    @Override
    public void setSourceContactsCustomField(Long sourceContactsCustomField) {
        this.sourceContactsCustomField = sourceContactsCustomField;
    }

    public void setSiteIdToLeadsSource(HashMap<Integer, Long> siteIdToLeadsSource) {
        this.siteIdToLeadsSource = siteIdToLeadsSource;
    }

    public Long getLeadPageFromSiteUrl() {
        return leadPageFromSiteUrl;
    }

    @Override
    public void setLeadPageFromSiteUrl(Long leadPageFromSiteUrl) {
        this.leadPageFromSiteUrl = leadPageFromSiteUrl;
    }

    public HashMap<String, Long> getAddtFieldsJoin() {
        return addtFieldsJoin;
    }

    @Override
    public void setAddtFieldsJoin(HashMap<String, Long> addtFieldsJoin) {
        this.addtFieldsJoin = addtFieldsJoin;
    }

    public Long getRefererCustomField() {
        return refererCustomField;
    }

    @Override
    public void setRefererCustomField(Long refererCustomField) {
        this.refererCustomField = refererCustomField;
    }

    public Long getRefererCustomFieldSecond() {
        return refererCustomFieldSecond;
    }

    @Override
    public void setRefererCustomFieldSecond(Long refererCustomFieldSecond) {
        this.refererCustomFieldSecond = refererCustomFieldSecond;
    }

    public Long getRefererCustomFieldThird() {
        return refererCustomFieldThird;
    }

    @Override
    public void setRefererCustomFieldThird(Long refererCustomFieldThird) {
        this.refererCustomFieldThird = refererCustomFieldThird;
    }

    @Override
    public Result newLeadFromSite(List<Lead> leads, String origin, String password) {
        Site site = siteService.findByUrlAndPassword(origin, password);
        if(site == null){
            return new Result("error: site not found");
        }

        List<LeadFromSite> leadFromSiteList = new ArrayList<>();
        for(Lead lead : leads){
            LeadFromSite leadFromSite = new LeadFromSite();
            leadFromSite.setDtmCreate(new Date());
            leadFromSite.setSite(site);
            leadFromSite.setLead(lead);
            leadFromSite.setState(LeadFromSite.State.NEW);
            leadFromSite.setRetryCount(RETRIES_DEFAULT);

            leadFromSiteList.add(leadFromSite);
        }

        leadFromSiteRepository.save(leadFromSiteList);
        return new Result("success");
    }

    public Long getLeadProcessedTagId() {
        return leadProcessedTagId;
    }

    @Override
    public void setLeadProcessedTagId(Long leadProcessedTagId) {
        this.leadProcessedTagId = leadProcessedTagId;
    }

    public String getLeadProcessedTagName() {
        return leadProcessedTagName;
    }

    @Override
    public void setLeadProcessedTagName(String leadProcessedTagName) {
        this.leadProcessedTagName = leadProcessedTagName;
    }

    public Long getRoistatVisitLeadCustomField() {
        return roistatVisitLeadCustomField;
    }

    @Override
    public void setRoistatVisitLeadCustomField(Long roistatVisitLeadCustomField) {
        this.roistatVisitLeadCustomField = roistatVisitLeadCustomField;
    }
}
