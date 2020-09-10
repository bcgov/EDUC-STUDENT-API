package ca.bc.gov.educ.api.student.support;

import ca.bc.gov.educ.api.student.messaging.MessagePublisher;
import ca.bc.gov.educ.api.student.messaging.MessageSubscriber;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test-message-sub")
@Configuration
public class MessageSubscriberMockConfiguration {
  @Bean
  @Primary
  public MessagePublisher messagePublisher() {
    return Mockito.mock(MessagePublisher.class);
  }
}