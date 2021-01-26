package ca.bc.gov.educ.api.student.constant;

/**
 * The enum Event type.
 */
public enum EventType {
  /**
   * Get student event type.
   */
  GET_STUDENT,
  /**
   * Create student event type.
   */
  CREATE_STUDENT,
  /**
   * Update student event type.
   */
  UPDATE_STUDENT,
  /**
   * Get student event type.
   */
  GET_STUDENT_HISTORY,
  /**
   * Create student event type.
   */
  CREATE_STUDENT_HISTORY,
  /**
   * Student event outbox processed event type.
   */
  STUDENT_EVENT_OUTBOX_PROCESSED,
  /**
   * Get paginated student by criteria event type.
   */
  GET_PAGINATED_STUDENT_BY_CRITERIA
}
