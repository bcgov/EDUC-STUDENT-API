package ca.bc.gov.educ.api.student.repository.v1;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.student.model.v1.GenderCodeEntity;

/**
 * Gender Code Table Repository
 *
 * @author Marco Villeneuve
 * 
 */
@Repository
public interface GenderCodeTableRepository extends CrudRepository<GenderCodeEntity, Long> {
    List<GenderCodeEntity> findAll();
}
