package repository;

import dmodel.LeadFromSite;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by berz on 15.06.2016.
 */
@Transactional(readOnly = true)
public interface LeadFromSiteRepository extends CrudRepository<LeadFromSite, Long>, JpaSpecificationExecutor {
}
