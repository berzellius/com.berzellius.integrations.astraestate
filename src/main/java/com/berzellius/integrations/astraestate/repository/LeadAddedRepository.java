package com.berzellius.integrations.astraestate.repository;

import com.berzellius.integrations.astraestate.dmodel.LeadAdded;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by berz on 25.06.2017.
 */
@Transactional(readOnly = true)
public interface LeadAddedRepository extends CrudRepository<LeadAdded, Long> {
}
