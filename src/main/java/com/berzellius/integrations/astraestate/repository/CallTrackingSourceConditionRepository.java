package com.berzellius.integrations.astraestate.repository;

import com.berzellius.integrations.astraestate.dmodel.CallTrackingSourceCondition;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Created by berz on 01.03.2016.
 */
@Transactional(readOnly = true)
public interface CallTrackingSourceConditionRepository extends CrudRepository<CallTrackingSourceCondition, Long>, JpaSpecificationExecutor<CallTrackingSourceCondition> {
    public List<CallTrackingSourceCondition> findBySourceId(Integer sourceId);
}
