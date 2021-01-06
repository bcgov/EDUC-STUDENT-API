package ca.bc.gov.educ.api.student.repository.v1;

import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentEventRepository extends CrudRepository<StudentEvent, UUID> {
  Optional<StudentEvent> findBySagaId(UUID sagaId);

  Optional<StudentEvent> findBySagaIdAndEventType(UUID sagaId, String eventType);

  List<StudentEvent> findByEventStatus(String eventStatus);
}
