package com.berzellius.integrations.astraestate.repository;

import com.berzellius.integrations.astraestate.dmodel.CallRecord;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by berz on 28.03.2017.
 */
public interface CallRecordRepository extends CrudRepository<CallRecord, Long> {
}
