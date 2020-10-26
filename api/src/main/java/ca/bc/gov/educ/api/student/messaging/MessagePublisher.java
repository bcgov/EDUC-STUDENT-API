package ca.bc.gov.educ.api.student.messaging;

import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import io.nats.streaming.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
@SuppressWarnings("java:S2142")
public class MessagePublisher extends MessagePubSub {
  @Autowired
  public MessagePublisher(final ApplicationProperties applicationProperties) throws IOException, InterruptedException {
    this(applicationProperties, true);
  }

  public MessagePublisher(final ApplicationProperties applicationProperties, final boolean connect) throws IOException, InterruptedException {
    Options options = new Options.Builder()
      .natsUrl(applicationProperties.getNatsUrl())
      .clusterId(applicationProperties.getNatsClusterId())
      .connectionLostHandler(this::connectionLostHandler)
      .clientId("student-api-publisher" + UUID.randomUUID().toString()).build();
    connectionFactory = new StreamingConnectionFactory(options);
    if(connect && applicationProperties.isNatsStreamingPubSubEnabled()) {
      this.connnect();
    }
  }

  public void connnect() throws IOException, InterruptedException {
    connection = connectionFactory.createConnection();
  }

  public void dispatchMessage(String subject, byte[] message) throws InterruptedException, TimeoutException, IOException {
    AckHandler ackHandler = getAckHandler();
    connection.publish(subject, message, ackHandler);
  }

  public AckHandler getAckHandler() {
    return new AckHandler() {
      @Override
      public void onAck(String guid, Exception err) {
        log.trace("already handled.");
      }

      @Override
      public void onAck(String guid, String subject, byte[] data, Exception ex) {

        if (ex != null) {
          executorService.execute(() -> retryPublish(subject, data));
        } else {
          log.trace("acknowledgement received {}", guid);
        }
      }
    };
  }

  public void retryPublish(String subject, byte[] message) {
    log.trace("retrying...");
    try {
      connection.publish(subject, message, getAckHandler());
    } catch (InterruptedException | TimeoutException | IOException e) {
      log.error("Exception", e);
    }
  }
}
