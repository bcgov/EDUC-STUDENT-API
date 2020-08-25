package ca.bc.gov.educ.api.student.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.student.model.StudentMergeSourceCodeEntity;

@Repository
public interface StudentMergeSourceCodeTableRepository extends CrudRepository<StudentMergeSourceCodeEntity, Long> {
    List<StudentMergeSourceCodeEntity> findAll();
}
