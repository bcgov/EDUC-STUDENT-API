package ca.bc.gov.educ.api.student.repository.v1;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.student.model.v1.SexCodeEntity;

/**
 * Sex Code Table Repository
 *
 * @author Marco Villeneuve
 * 
 */
@Repository
public interface SexCodeTableRepository extends CrudRepository<SexCodeEntity, Long> {
    List<SexCodeEntity> findAll();
}
