package ca.bc.gov.educ.api.student.messaging;

import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Nats;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;

/**
 * The type Nats connection.
 */
@Component
@Slf4j
public class NatsConnection implements Closeable {
  @Getter
  private final Connection natsCon;

  /**
   * Instantiates a new Nats connection.
   *
   * @param applicationProperties the application properties
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   */
  @Autowired
  public NatsConnection(final ApplicationProperties applicationProperties) throws IOException, InterruptedException {
    io.nats.client.Options natsOptions = new io.nats.client.Options.Builder()
        .connectionListener(this::connectionListener)
        .maxPingsOut(30)
        .pingInterval(Duration.ofSeconds(2))
        .connectionName("STUDENT-API")
        .connectionTimeout(Duration.ofSeconds(20))
        .executor(new EnhancedQueueExecutor.Builder()
            .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("core-nats-%d").build())
            .setCorePoolSize(2).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build())
        .maxReconnects(-1)
        .reconnectWait(Duration.ofSeconds(2))
        .servers(new String[]{applicationProperties.getStanUrl()})
        .build();
    natsCon = Nats.connect(natsOptions);

  }

  private void connectionListener(Connection connection, ConnectionListener.Events events) {
    switch (events) {
      case DISCOVERED_SERVERS:
        log.info("NATS -> Discovered servers");
        break;
      case RESUBSCRIBED:
        log.info("NATS -> Resubscribed");
        break;
      case DISCONNECTED:
        log.info("NATS -> Disconnected");
        break;
      case RECONNECTED:
        log.info("NATS -> Reconnected");
        break;
      case CLOSED:
        log.info("NATS -> Closed");
        break;
      case CONNECTED:
        log.info("NATS -> Connected");
        break;
      case LAME_DUCK:
        log.info("NATS -> Lame Duck");
        break;
      default:
        break;
    }
  }


  @Override
  public void close() {
    if (natsCon != null) {
      log.info("closing nats connection...");
      try {
        natsCon.close();
      } catch (InterruptedException e) {
        log.error("error while closing nats connection...", e);
        Thread.currentThread().interrupt();
      }
      log.info("nats connection closed...");
    }
  }
}
