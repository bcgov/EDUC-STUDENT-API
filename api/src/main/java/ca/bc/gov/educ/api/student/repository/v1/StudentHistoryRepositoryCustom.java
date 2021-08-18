package ca.bc.gov.educ.api.student.repository.v1;

import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import ca.bc.gov.educ.api.student.struct.v1.Search;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface StudentHistoryRepositoryCustom {

  Page<StudentEntity> findDistinctStudentsByStudentHistoryCriteria(Map<String, String> sortMap, List<Search> searches, int pageNumber, int pageSize);
}
