package com.berzellius.integrations.astraestate.dto.site;

import java.util.List;

/**
 * Created by berz on 25.06.2017.
 */
public class LeadsAddingRequest {

    protected List<LeadAddDTO> addedLeads;

    public LeadsAddingRequest() {
    }

    public List<LeadAddDTO> getAddedLeads() {
        return addedLeads;
    }

    public void setAddedLeads(List<LeadAddDTO> addedLeads) {
        this.addedLeads = addedLeads;
    }
}
