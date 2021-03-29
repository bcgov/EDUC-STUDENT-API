package ca.bc.gov.educ.api.student.messaging.stan;

import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import ca.bc.gov.educ.api.student.service.v1.STANEventHandlerService;
import ca.bc.gov.educ.api.student.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.student.struct.v1.Event;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.streaming.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_EVENTS_TOPIC;

/**
 * The type Subscriber.
 */
@Component
@Slf4j
public class Subscriber extends PubSub implements Closeable {
  private final StreamingConnectionFactory connectionFactory;
  private final STANEventHandlerService stanEventHandlerService;
  private StreamingConnection connection;

  /**
   * Instantiates a new Subscriber.
   *
   * @param applicationProperties   the application properties
   * @param natsConnection          the nats connection
   * @param stanEventHandlerService the stan event handler service
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   */
  @Autowired
  public Subscriber(ApplicationProperties applicationProperties, Connection natsConnection, STANEventHandlerService stanEventHandlerService) throws IOException, InterruptedException {
    this.stanEventHandlerService = stanEventHandlerService;
    Options options = new Options.Builder()
        .clusterId(applicationProperties.getStanCluster())
        .connectionLostHandler(this::connectionLostHandler)
        .natsConn(natsConnection)
        .traceConnection()
        .maxPingsOut(30)
        .pingInterval(Duration.ofSeconds(2))
        .clientId("student-api-subscriber" + UUID.randomUUID().toString()).build();
    connectionFactory = new StreamingConnectionFactory(options);
    connection = connectionFactory.createConnection();
  }


  /**
   * This subscription will makes sure the messages are required to acknowledge manually to STAN.
   * Subscribe.
   *
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   * @throws IOException          the io exception
   */
  @PostConstruct
  public void subscribe() throws InterruptedException, TimeoutException, IOException {
    SubscriptionOptions options = new SubscriptionOptions.Builder().durableName("student-api-student-event-consumer").build();
    connection.subscribe(STUDENT_EVENTS_TOPIC.toString(), "student-api-student-event", this::onStudentEventsTopicMessage, options);
  }

  /**
   * This method will process the event message pushed into the student_events_topic.
   * this will get the message and update the event status to mark that the event reached the message broker.
   * On message message handler.
   *
   * @param message the string representation of {@link Event} if it not type of event then it will throw exception and will be ignored.
   */
  public void onStudentEventsTopicMessage(Message message) {
    if (message != null) {
      try {
        String eventString = new String(message.getData());
        ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
        stanEventHandlerService.updateEventStatus(event);
        log.info("received event :: {} ", event);
      } catch (final Exception ex) {
        log.error("Exception ", ex);
      }
    }
  }


  /**
   * This method will keep retrying for a connection.
   */
  private void connectionLostHandler(StreamingConnection streamingConnection, Exception e) {
    this.connection = super.connectionLostHandler(this.connectionFactory);
    this.retrySubscription();
  }
  private void retrySubscription() {
    int numOfRetries = 0;
    while (true) {
      try {
        log.trace("retrying subscription as connection was lost :: retrying ::" + numOfRetries++);
        this.subscribe();
        log.info("successfully resubscribed after {} attempts", numOfRetries);
        break;
      } catch (InterruptedException | TimeoutException | IOException exception) {
        log.error("exception occurred while retrying subscription", exception);
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public void close() {
    super.close(this.connection);
  }
}
