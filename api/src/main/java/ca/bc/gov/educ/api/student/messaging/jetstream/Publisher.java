package ca.bc.gov.educ.api.student.messaging.jetstream;

import ca.bc.gov.educ.api.student.constant.EventOutcome;
import ca.bc.gov.educ.api.student.constant.EventType;
import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import ca.bc.gov.educ.api.student.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.StreamConfiguration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_EVENTS_TOPIC;

/**
 * The type Publisher.
 */
@Component("publisher")
@Slf4j
public class Publisher {
  private final JetStream jetStream;

  /**
   * Instantiates a new Publisher.
   *
   * @param natsConnection the nats connection
   * @throws IOException           the io exception
   * @throws JetStreamApiException the jet stream api exception
   */
  @Autowired
  public Publisher(final Connection natsConnection) throws IOException, JetStreamApiException {
    this.jetStream = natsConnection.jetStream();
    this.createOrUpdateStudentEventStream(natsConnection);
  }

  /**
   * here only name and replicas and max messages are set, rest all are library default.
   *
   * @param natsConnection the nats connection
   * @throws IOException           the io exception
   * @throws JetStreamApiException the jet stream api exception
   */
  private void createOrUpdateStudentEventStream(final Connection natsConnection) throws IOException, JetStreamApiException {
    val streamConfiguration = StreamConfiguration.builder().name(ApplicationProperties.STREAM_NAME).replicas(1).maxMessages(1000000).addSubjects(STUDENT_EVENTS_TOPIC.toString()).build();
    try {
      natsConnection.jetStreamManagement().updateStream(streamConfiguration);
    } catch (final JetStreamApiException exception) {
      if (exception.getErrorCode() == 404) { // the stream does not exist , lets create it.
        natsConnection.jetStreamManagement().addStream(streamConfiguration);
      } else {
        log.info("exception", exception);
      }
    }

  }


  /**
   * Dispatch choreography event.
   *
   * @param event the event
   */
  public void dispatchChoreographyEvent(final StudentEvent event) {
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
        val pub = this.jetStream.publishAsync(STUDENT_EVENTS_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(choreographedEvent));
        pub.thenAcceptAsync(result -> log.info("Event ID :: {} Published to JetStream :: {}", event.getEventId(), result.getSeqno()));
      } catch (IOException e) {
        log.error("exception while broadcasting message to JetStream", e);
      }
    }
  }
}
