package ca.bc.gov.educ.api.student.support;

import ca.bc.gov.educ.api.student.messaging.MessagePublisher;
import ca.bc.gov.educ.api.student.messaging.MessageSubscriber;
import ca.bc.gov.educ.api.student.messaging.NatsConnection;
import ca.bc.gov.educ.api.student.messaging.stan.StanPublisher;
import ca.bc.gov.educ.api.student.messaging.stan.StanSubscriber;
import ca.bc.gov.educ.api.student.messaging.stan.StanConnection;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test-event")
@Configuration
public class EventTaskSchedulerMockConfiguration {
  @Bean
  @Primary
  public MessagePublisher messagePublisher() {
    return Mockito.mock(MessagePublisher.class);
  }

  @Bean
  @Primary
  public MessageSubscriber messageSubscriber() {
    return Mockito.mock(MessageSubscriber.class);
  }

  @Bean
  @Primary
  public NatsConnection natsConnection() {
    return Mockito.mock(NatsConnection.class);
  }

  @Bean
  @Primary
  public StanPublisher publisher() {
    return Mockito.mock(StanPublisher.class);
  }

  @Bean
  @Primary
  public StanSubscriber subscriber() {
    return Mockito.mock(StanSubscriber.class);
  }
  @Bean
  @Primary
  public StanConnection stanConnection() {
    return Mockito.mock(StanConnection.class);
  }

}
