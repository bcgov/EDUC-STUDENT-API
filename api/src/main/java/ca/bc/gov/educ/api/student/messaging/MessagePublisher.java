package ca.bc.gov.educ.api.student.messaging;

import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import io.nats.streaming.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
@SuppressWarnings("java:S2142")
public class MessagePublisher implements Closeable {
  private final ExecutorService executorService = Executors.newFixedThreadPool(2);
  private StreamingConnection connection;
  @Setter
  private StreamingConnectionFactory connectionFactory;

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
    if(connect) {
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
          executorService.execute(() -> {
              retryPublish(subject, data);  // NOSONAR
          });
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

  /**
   * This method will keep retrying for a connection.
   */
  public void connectionLostHandler(StreamingConnection streamingConnection, Exception e) {
    if (e != null) {
      int numOfRetries = 1;
      while (true) {
        try {
          log.trace("retrying connection as connection was lost :: retrying ::" + numOfRetries++);
          connection = connectionFactory.createConnection();
          log.info("successfully reconnected after {} attempts", numOfRetries);
          break;
        } catch (IOException | InterruptedException ex) {
          log.error("exception occurred", ex);
          try {
            double sleepTime = (2 * numOfRetries);
            TimeUnit.SECONDS.sleep((long) sleepTime);
          } catch (InterruptedException exc) {      // NOSONAR
            log.error("exception occurred", exc);   // NOSONAR
          }

        }
      }
    }
  }


  @Override
  public void close() {
    if (!executorService.isShutdown()) {
      executorService.shutdown();
    }
    if(connection != null){
      log.info("closing nats connection in the publisher...");
      try {
        connection.close();
      } catch (IOException | TimeoutException | InterruptedException e) {
        log.error("error while closing nats connection in the publisher...", e);
      }
      log.info("nats connection closed in the publisher...");
    }
  }
}
