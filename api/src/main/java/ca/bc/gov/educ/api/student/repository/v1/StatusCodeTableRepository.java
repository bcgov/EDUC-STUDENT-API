package ca.bc.gov.educ.api.student.repository.v1;

import ca.bc.gov.educ.api.student.model.v1.StatusCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Gender Code Table Repository
 *
 * @author Marco Villeneuve
 */
@Repository
public interface StatusCodeTableRepository extends CrudRepository<StatusCodeEntity, Long> {
    List<StatusCodeEntity> findAll();
}
