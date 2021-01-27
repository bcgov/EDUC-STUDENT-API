package ca.bc.gov.educ.api.student.repository.v1;

import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Student event repository.
 */
public interface StudentEventRepository extends JpaRepository<StudentEvent, UUID> {
  /**
   * Find by saga id optional.
   *
   * @param sagaId the saga id
   * @return the optional
   */
  Optional<StudentEvent> findBySagaId(UUID sagaId);

  /**
   * Find by saga id and event type optional.
   *
   * @param sagaId    the saga id
   * @param eventType the event type
   * @return the optional
   */
  Optional<StudentEvent> findBySagaIdAndEventType(UUID sagaId, String eventType);

  /**
   * Find by event status list.
   *
   * @param eventStatus the event status
   * @return the list
   */
  List<StudentEvent> findByEventStatus(String eventStatus);
}
