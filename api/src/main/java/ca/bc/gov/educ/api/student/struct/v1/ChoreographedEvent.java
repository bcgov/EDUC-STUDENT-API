package ca.bc.gov.educ.api.student.struct.v1;

import ca.bc.gov.educ.api.student.constant.EventOutcome;
import ca.bc.gov.educ.api.student.constant.EventType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * The type Choreographed event.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChoreographedEvent {
  /**
   * The Event id.
   */
  String eventID; // the primary key of student event table.
  /**
   * The Event type.
   */
  EventType eventType;
  /**
   * The Event outcome.
   */
  EventOutcome eventOutcome;
  /**
   * The Event payload.
   */
  String eventPayload;
  /**
   * The Create user.
   */
  String createUser;
  /**
   * The Update user.
   */
  String updateUser;
}
