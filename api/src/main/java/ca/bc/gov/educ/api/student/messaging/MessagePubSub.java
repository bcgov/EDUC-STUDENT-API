package ca.bc.gov.educ.api.student.messaging;

import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@SuppressWarnings("java:S2142")
public abstract class MessagePubSub implements Closeable {
  protected StreamingConnection connection;
  @Setter
  protected StreamingConnectionFactory connectionFactory;
  @Setter
  protected ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Override
  public void close() {
    if (!executorService.isShutdown()) {
      executorService.shutdown();
    }
    if(Optional.ofNullable(connection).isPresent()){
      log.info("closing nats connection ...");
      try {
        connection.close();
      } catch (IOException | TimeoutException | InterruptedException e) {
        log.error("error while closing nats connection ...", e);
      }
      log.info("nats connection closed...");
    }
  }
  /**
   * This method will keep retrying for a connection.
   */
  protected int connectionLostHandler(StreamingConnection streamingConnection, Exception e) {
    log.trace("streaming connection is :: {}",streamingConnection);
    int numOfRetries = 1;
    if (e != null) {
      boolean successfullyReconnected = false;
      while (!successfullyReconnected) {
        try {
          log.trace("retrying connection as connection was lost :: retrying ::" + numOfRetries++);
          connection = connectionFactory.createConnection();
          log.info("successfully reconnected after {} attempts", numOfRetries);
          successfullyReconnected = true;
        } catch (IOException | InterruptedException ex) {
          log.error("exception occurred", ex);
          try {
            double sleepTime = (2 * numOfRetries);
            TimeUnit.SECONDS.sleep((long) sleepTime);
          } catch (InterruptedException exc) {
            log.error("exception occurred", exc);
          }

        }
      }
    }
    return numOfRetries;
  }
}
