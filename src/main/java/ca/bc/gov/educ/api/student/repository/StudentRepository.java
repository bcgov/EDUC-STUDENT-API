package ca.bc.gov.educ.api.student.repository;

import ca.bc.gov.educ.api.student.model.StudentEntity;
import org.springframework.data.repository.CrudRepository;

public interface StudentRepository extends CrudRepository<StudentEntity, Long> {
}
