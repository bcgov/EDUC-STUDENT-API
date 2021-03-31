package ca.bc.gov.educ.api.student.messaging.stan;

import ca.bc.gov.educ.api.student.service.v1.STANEventHandlerService;
import ca.bc.gov.educ.api.student.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.student.struct.v1.Event;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import io.nats.streaming.Message;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.SubscriptionOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_EVENTS_TOPIC;

/**
 * The type Subscriber.
 */
@Component
@Slf4j
public class StanSubscriber {
  private final StreamingConnection connection;
  private final STANEventHandlerService stanEventHandlerService;

  @Autowired
  public StanSubscriber(StanConnection stanConnection, STANEventHandlerService stanEventHandlerService) {
    this.stanEventHandlerService = stanEventHandlerService;
    this.connection = stanConnection.getConnection();
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
    this.connection.subscribe(STUDENT_EVENTS_TOPIC.toString(), "student-api-student-event",
        this::onStudentEventsTopicMessage, options);
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


}
