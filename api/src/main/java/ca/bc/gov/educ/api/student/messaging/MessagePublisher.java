package ca.bc.gov.educ.api.student.messaging;

import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This publisher will publish messages to NATS, for publishing directly to STAN,
 * please follow {@link ca.bc.gov.educ.api.student.messaging.stan.StanPublisher}.
 */
@Component
@Slf4j
public class MessagePublisher {


  private final Connection connection;

  /**
   * Instantiates a new Message publisher.
   *
   * @param connection the nats connection
   */
  @Autowired
  public MessagePublisher(final Connection connection) {
    this.connection = connection;
  }

  /**
   * Dispatch message.
   *
   * @param subject the subject
   * @param message the message
   */
  public void dispatchMessage(String subject, byte[] message) {
    connection.publish(subject, message);
  }
}
