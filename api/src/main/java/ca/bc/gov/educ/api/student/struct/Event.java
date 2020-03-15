package ca.bc.gov.educ.api.student.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class Event {
  private EventType eventType;
  private String replyTo;
  private String eventPayload;
  private String sagaId;
  public enum EventType{
    STUDENT_CREATED_OR_UPDATED,
    PEN_REQUEST_COMPLETE_SAGA,
    STUDENT_EVENT_OUTBOX_PROCESSED
  }
}
