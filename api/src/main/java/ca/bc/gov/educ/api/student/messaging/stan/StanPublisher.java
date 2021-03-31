package ca.bc.gov.educ.api.student.messaging.stan;

import ca.bc.gov.educ.api.student.constant.EventOutcome;
import ca.bc.gov.educ.api.student.constant.EventType;
import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import ca.bc.gov.educ.api.student.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import io.nats.streaming.StreamingConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_EVENTS_TOPIC;

/**
 * The type Publisher.
 */
@Component
@Slf4j
public class StanPublisher {

  private final StreamingConnection streamingConnection;
  @Autowired
  public StanPublisher(StanConnection connection) {
   this.streamingConnection = connection.getConnection();
  }


  /**
   * Dispatch choreography event.
   *
   * @param event the event
   */
  public void dispatchChoreographyEvent(StudentEvent event) {
    if (event != null && event.getEventId() != null) {
      ChoreographedEvent choreographedEvent = new ChoreographedEvent();
      choreographedEvent.setEventType(EventType.valueOf(event.getEventType()));
      choreographedEvent.setEventOutcome(EventOutcome.valueOf(event.getEventOutcome()));
      choreographedEvent.setEventPayload(event.getEventPayload());
      choreographedEvent.setEventID(event.getEventId().toString());
      choreographedEvent.setCreateUser(event.getCreateUser());
      choreographedEvent.setUpdateUser(event.getUpdateUser());
      try {
        log.info("Broadcasting event :: {}", choreographedEvent);
        this.streamingConnection.publish(STUDENT_EVENTS_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(choreographedEvent));
      } catch (IOException | TimeoutException e) {
        log.error("exception while broadcasting message to STAN", e);
      } catch (InterruptedException e) {
        log.error("exception while broadcasting message to STAN", e);
        Thread.currentThread().interrupt();
      }
    }
  }
}
