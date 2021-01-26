package ca.bc.gov.educ.api.student.struct.v1;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type Choreographed event.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChoreographedEvent extends Event {
  /**
   * The Event id.
   */
  String eventID; // the primary key of student event table.
  /**
   * The Create user.
   */
  String createUser;
  /**
   * The Update user.
   */
  String updateUser;
}
