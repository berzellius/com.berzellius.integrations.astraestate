package com.berzellius.integrations.astraestate.dto.site;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Created by berz on 28.03.2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallRecordDTO {

    public CallRecordDTO() {
    }

   // private String _id;
    private String site;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date datetime;//notification_time;
    private String virtual_number;//called_phone;
    private String caller;//calling_phone;
    private Long call_id;
    //private String direction;
    private String recordlink;//record_link;
    //private Long employee_id;
    //private Integer file_duration;

    /**
     * Техническе поля
     */
    private String result;
    private String processed;

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Long getCall_id() {
        return call_id;
    }

    public void setCall_id(Long call_id) {
        this.call_id = call_id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getProcessed() {
        return processed;
    }

    public void setProcessed(String processed) {
        this.processed = processed;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getVirtual_number() {
        return virtual_number;
    }

    public void setVirtual_number(String virtual_number) {
        this.virtual_number = virtual_number;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getRecordlink() {
        return recordlink;
    }

    public void setRecordlink(String recordlink) {
        this.recordlink = recordlink;
    }
}
