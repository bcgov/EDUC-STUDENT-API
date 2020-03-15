package ca.bc.gov.educ.api.student.messaging;

import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import ca.bc.gov.educ.api.student.service.EventHandlerService;
import ca.bc.gov.educ.api.student.struct.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.streaming.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_TOPIC;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class MessagePubSubImpl implements Closeable {
  private StreamingConnection connection;
  private Subscription subscription;
  private StreamingConnectionFactory connectionFactory;

  @Getter(PRIVATE)
  private final EventHandlerService eventHandlerService;
  @Autowired
  public MessagePubSubImpl(final ApplicationProperties applicationProperties, final EventHandlerService eventHandlerService) throws IOException, InterruptedException {
    this.eventHandlerService = eventHandlerService;
    Options options = new Options.Builder().maxPingsOut(100)
            .natsUrl(applicationProperties.getNatsUrl())
            .clusterId(applicationProperties.getNatsClusterId())
            .connectionLostHandler(this::connectionLostHandler)
            .clientId(UUID.randomUUID().toString()).build();
    connectionFactory = new StreamingConnectionFactory(options);
    connection = connectionFactory.createConnection();
  }

  public void dispatchMessage(String subject, byte[] message) throws InterruptedException, TimeoutException, IOException {
    AckHandler ackHandler = (guid, err) -> {
      if (err != null) {
        System.err.printf("Error publishing msg id %s: %s\n", guid, err.getMessage());
      } else {
        System.out.printf("Received ack for msg id %s\n", guid);
      }
    };
    connection.publish(subject, message, ackHandler);
  }

  @PostConstruct
  public void subscribe() throws InterruptedException, TimeoutException, IOException {
    SubscriptionOptions options = new SubscriptionOptions.Builder().durableName("student-consumer").build();
    subscription = connection.subscribe(STUDENT_TOPIC.toString(), "student", this::onStudentTopicMessage, options);
  }

  private void onStudentTopicMessage(Message message) {
    if (message != null) {
      try {
        String eventString = new String(message.getData());
        Event event = new ObjectMapper().readValue(eventString, Event.class);
        getEventHandlerService().handleEvent(event);
      }catch (final  Exception ex){
        log.error("Exception ", ex);
      }
    }
  }


  @Override
  public void close() {
    log.info("nats unsubscribe.");
    if (subscription != null) {
      try {
        subscription.close();
      } catch (final Exception e) {
        log.warn("Ignoring unsubscribe exception", e);
      }
    }
    log.info("nats unsubscribe, completed.");
  }

  private void connectionLostHandler(StreamingConnection streamingConnection, Exception e) {
    try {
      if (e instanceof IllegalStateException && e.getMessage().equalsIgnoreCase("stan: connection closed")) {
        connection = connectionFactory.createConnection();
        this.subscribe();
      }
    } catch (final Exception ex) {
      log.error("not able to reconnect, pod needs to be restarted.");
    }
  }

}
