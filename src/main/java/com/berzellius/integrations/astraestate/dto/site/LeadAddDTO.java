package com.berzellius.integrations.astraestate.dto.site;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by berz on 25.06.2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadAddDTO {
    protected Long leadId;

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public LeadAddDTO() {
    }
}
