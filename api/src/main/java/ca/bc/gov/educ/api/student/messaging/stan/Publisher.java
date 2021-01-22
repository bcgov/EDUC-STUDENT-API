package ca.bc.gov.educ.api.student.messaging.stan;

import ca.bc.gov.educ.api.student.messaging.NatsConnection;
import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The type Publisher.
 */
@Component
@Slf4j
public class Publisher implements Closeable {
  private final StreamingConnectionFactory connectionFactory;
  private StreamingConnection connection;

  /**
   * Instantiates a new Publisher.
   *
   * @param applicationProperties the application properties
   * @param natsConnection        the nats connection
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   */
  @Autowired
  public Publisher(ApplicationProperties applicationProperties, NatsConnection natsConnection) throws IOException, InterruptedException {
    Options options = new Options.Builder()
        .natsUrl(applicationProperties.getStanUrl())
        .clusterId(applicationProperties.getStanCluster())
        .connectionLostHandler(this::connectionLostHandler)
        .natsConn(natsConnection.getNatsCon())
        .maxPingsOut(30)
        .pingInterval(Duration.ofSeconds(2))
        .clientId("student-api-publisher" + UUID.randomUUID().toString()).build();
    connectionFactory = new StreamingConnectionFactory(options);
    connection = connectionFactory.createConnection();
  }

  /**
   * Dispatch message.
   *
   * @param topic   the topic
   * @param message the message
   */
  public void dispatchMessage(String topic, byte[] message) {
    try {
      connection.publish(topic, message);
    } catch (IOException | InterruptedException | TimeoutException e) {
      Thread.currentThread().interrupt();
      log.error("exception while broadcasting message to STAN", e);
    }
  }

  /**
   * This method will keep retrying for a connection.
   */
  private void connectionLostHandler(StreamingConnection streamingConnection, Exception e) {
    if (e != null) {
      int numOfRetries = 1;
      while (true) {
        try {
          log.trace("retrying connection as connection was lost :: retrying ::" + numOfRetries++);
          connection = connectionFactory.createConnection();
          log.info("successfully reconnected after {} attempts", numOfRetries);
          break;
        } catch (IOException ex) {
          backOff(numOfRetries, ex);
        } catch (InterruptedException interruptedException) {
          Thread.currentThread().interrupt();
          backOff(numOfRetries, interruptedException);
        }
      }
    }
  }

  private void backOff(int numOfRetries, Exception ex) {
    log.error("exception occurred", ex);
    try {
      double sleepTime = (2 * numOfRetries);
      TimeUnit.SECONDS.sleep((long) sleepTime);
    } catch (InterruptedException exc) {
      log.error("exception occurred", exc);
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Closes this stream and releases any system resources associated
   * with it. If the stream is already closed then invoking this
   * method has no effect.
   *
   * <p> As noted in {@link AutoCloseable#close()}, cases where the
   * close may fail require careful attention. It is strongly advised
   * to relinquish the underlying resources and to internally
   * <em>mark</em> the {@code Closeable} as closed, prior to throwing
   * the {@code IOException}.
   */
  @Override
  public void close() {
    if (connection != null) {
      log.info("closing stan connection...");
      try {
        connection.close();
      } catch (IOException | TimeoutException | InterruptedException e) {
        log.error("error while closing stan connection...", e);
        Thread.currentThread().interrupt();
      }
      log.info("stan connection closed...");
    }
  }
}
