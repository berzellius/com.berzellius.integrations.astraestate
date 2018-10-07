package com.berzellius.integrations.astraestate.service;


import com.berzellius.integrations.astraestate.dmodel.CallTrackingSourceCondition;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by berz on 01.03.2016.
 */
@Service
public interface CallTrackingSourceConditionService {
    void updateSources(List<CallTrackingSourceCondition> allMarketingChannelsFromCalltracking);

    Iterable<CallTrackingSourceCondition> getAllSources();


    /*
        * Выбрать рекламный канал по Utm и Id проекта
         */
    CallTrackingSourceCondition getCallTrackingSourceConditionByUtmAndProjectId(String utmSource, String utmMedium, String utmCampaign, Integer projectId);

    List<CallTrackingSourceCondition> getCallTrackingSourceConditionsBySourceId(Integer sourceId);
}
