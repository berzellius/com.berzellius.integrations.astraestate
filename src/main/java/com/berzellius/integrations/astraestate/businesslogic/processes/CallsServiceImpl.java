package com.berzellius.integrations.astraestate.businesslogic.processes;


import com.berzellius.integrations.astraestate.businesslogic.rules.transformer.FieldsTransformer;
import com.berzellius.integrations.astraestate.dmodel.CallRecord;
import com.berzellius.integrations.astraestate.dmodel.CallTrackingSourceCondition;
import com.berzellius.integrations.astraestate.dmodel.TrackedCall;
import com.berzellius.integrations.astraestate.dto.site.*;
import com.berzellius.integrations.astraestate.repository.CallRecordRepository;
import com.berzellius.integrations.astraestate.repository.LeadAddedRepository;
import com.berzellius.integrations.astraestate.repository.TrackedCallRepository;
import com.berzellius.integrations.astraestate.scheduling.SchedulingService;
import com.berzellius.integrations.astraestate.service.CallTrackingSourceConditionService;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by berz on 14.03.2017.
 */
@Service
public class  CallsServiceImpl implements CallsService {
    @Autowired
    SchedulingService schedulingService;

    @Autowired
    TrackedCallRepository trackedCallRepository;

    @Autowired
    LeadAddedRepository leadAddedRepository;

    @Autowired
    FieldsTransformer fieldsTransformer;

    @Autowired
    CallRecordRepository callRecordRepository;

    @Autowired
    CallTrackingSourceConditionService callTrackingSourceConditionService;

    private String transformPhone(String phone){
        String res = fieldsTransformer.transform(phone, FieldsTransformer.Transformation.CALL_NUMBER_COMMON);
        //res = fieldsTransformer.transform(res, FieldsTransformer.Transformation.CALL_NUMBER_LEADING_7);
        return res;
    }

    @Override
    public Result newCallFromWebhook(CallRequest callRequest) {
        List<CallDTO> calls = callRequest.getCalls();

        if(calls != null && calls.size() > 0){
            processCalls(calls);

            try {
                schedulingService.runImportCallsToCRM();
            } catch (JobParametersInvalidException e) {
                e.printStackTrace();
            } catch (JobExecutionAlreadyRunningException e) {
                e.printStackTrace();
            } catch (JobRestartException e) {
                e.printStackTrace();
            } catch (JobInstanceAlreadyCompleteException e) {
                e.printStackTrace();
            }
        }

        return new Result("success");
    }

    @Override
    public Result newCallRecords(CallRecordRequest callRecordRequest) {
        List<CallRecord> callRecords = new ArrayList<>();

        for (CallRecordDTO callRecordDTO : callRecordRequest.getCall_records()) {
        /*    Matcher m = Pattern.compile("\\&\\#34;(\\S)+\\&\\#34;").matcher(callRecordDTO.getRecordlink());
            int i = 0;
            while (m.find()) {*/
                String calling_phone = callRecordDTO.getCaller();
                String called_phone = callRecordDTO.getVirtual_number();
                Date dt = callRecordDTO.getDatetime();

                CallRecord callRecord = new CallRecord();
                callRecord.setState(CallRecord.State.NEW);
                callRecord.setCalled_phone(called_phone);
                callRecord.setCalling_phone(calling_phone);
                callRecord.setDt(dt);
                callRecord.setLink(callRecordDTO.getRecordlink());

                callRecords.add(callRecord);
          /*  }*/
        }
        if(callRecords.size() > 0){
            callRecordRepository.save(callRecords);
        }

        return new Result("success");
    }

    private void processCalls(List<CallDTO> calls) {
        for(CallDTO callDTO : calls){
            if(callDTO.getCaller() != null) {
                TrackedCall trackedCall = new TrackedCall();
                trackedCall.setState(TrackedCall.State.NEW);
                trackedCall.setSiteId(callDTO.getProject_id() != null ? callDTO.getProject_id() : 0);
                trackedCall.setDt(callDTO.getDatetime());
                trackedCall.setDtmCreate(new Date());
                String phone = transformPhone(callDTO.getCaller());
                trackedCall.setNumber(phone);

                // если задан id источника звонка, пытаемся определить наименование
                if(callDTO.getSource_id() != null) {
                    List<CallTrackingSourceCondition> conditions = callTrackingSourceConditionService.getCallTrackingSourceConditionsBySourceId(callDTO.getSource_id());
                    if(conditions.size() > 0){
                        trackedCall.setSource(conditions.get(0).getSourceName());
                    }
                }

                //trackedCall.setSource(callDTO.);
                trackedCall.setVirtual_number(callDTO.getVirtual_number());
                //trackedCall.setSearch_engine(callDTO.getSearch_engine());
                //trackedCall.setSearch_query(callDTO.getSearch_query());
                trackedCall.setCampaign_id(trackedCall.getCampaign_id());

                trackedCallRepository.save(trackedCall);
            }
        }
    }
}
