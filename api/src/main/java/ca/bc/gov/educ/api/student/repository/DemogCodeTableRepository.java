package ca.bc.gov.educ.api.student.repository;

import ca.bc.gov.educ.api.student.model.DemogCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Gender Code Table Repository
 *
 * @author Marco Villeneuve
 * 
 */
@Repository
public interface DemogCodeTableRepository extends CrudRepository<DemogCodeEntity, Long> {
    List<DemogCodeEntity> findAll();
}
