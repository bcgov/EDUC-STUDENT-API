package ca.bc.gov.educ.api.student.messaging.jetstream;

import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import ca.bc.gov.educ.api.student.service.v1.JetStreamEventHandlerService;
import ca.bc.gov.educ.api.student.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.student.struct.v1.Event;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_EVENTS_TOPIC;

/**
 * The type Subscriber.
 */
@Component
@DependsOn("publisher")
@Slf4j
public class Subscriber {
  private final JetStreamEventHandlerService jetStreamEventHandlerService;
  private final Connection natsConnection;

  /**
   * Instantiates a new Subscriber.
   *
   * @param natsConnection          the nats connection
   * @param jetStreamEventHandlerService the stan event handler service
   */
  @Autowired
  public Subscriber(final Connection natsConnection, final JetStreamEventHandlerService jetStreamEventHandlerService) {
    this.jetStreamEventHandlerService = jetStreamEventHandlerService;
    this.natsConnection = natsConnection;
  }


  /**
   * This subscription will makes sure the messages are required to acknowledge manually to Jet Stream.
   * Subscribe.
   *
   * @throws IOException the io exception
   */
  @PostConstruct
  public void subscribe() throws IOException, JetStreamApiException {
    val qName = "STUDENT-EVENTS-TOPIC-STUDENT-API";
    val autoAck = false;
    PushSubscribeOptions options = PushSubscribeOptions.builder().stream(ApplicationProperties.STREAM_NAME)
        .durable("STUDENT-API-STUDENT-EVENTS-TOPIC-DURABLE")
        .configuration(ConsumerConfiguration.builder().deliverPolicy(DeliverPolicy.New).build()).build();
    this.natsConnection.jetStream().subscribe(STUDENT_EVENTS_TOPIC.toString(), qName, this.natsConnection.createDispatcher(), this::onStudentEventsTopicMessage,
        autoAck, options);
  }

  /**
   * This method will process the event message pushed into the student_events_topic.
   * this will get the message and update the event status to mark that the event reached the message broker.
   * On message message handler.
   *
   * @param message the string representation of {@link Event} if it not type of event then it will throw exception and will be ignored.
   */
  public void onStudentEventsTopicMessage(final Message message) {
    log.debug("Received message :: {}", message);
    try {
      String eventString = new String(message.getData());
      ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
      jetStreamEventHandlerService.updateEventStatus(event);
      log.info("received event :: {} ", event);
      message.ack();
    } catch (final Exception ex) {
      log.error("Exception ", ex);
    }
  }

}
