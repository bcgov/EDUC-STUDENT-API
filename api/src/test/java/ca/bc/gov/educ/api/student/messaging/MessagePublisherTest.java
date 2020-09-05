package ca.bc.gov.educ.api.student.messaging;

import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import ca.bc.gov.educ.api.student.service.EventHandlerService;
import io.nats.streaming.AckHandler;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringRunner.class)
@ActiveProfiles("test-event")
@SpringBootTest
public class MessagePublisherTest {
  public static final String STUDENT_API_TOPIC = "STUDENT_API_TOPIC";

  @Mock
  private StreamingConnection connection;
  @Mock
  private StreamingConnectionFactory connectionFactory;
  @Autowired
  private ApplicationProperties applicationProperties;
  @Autowired
  private EventHandlerService eventHandlerService;
  private MessagePublisher messagePublisher;

  @Before
  public void setUp() throws IOException, InterruptedException {
    initMocks(this);
    messagePublisher = new MessagePublisher(applicationProperties, eventHandlerService);
    messagePublisher.setConnection(connection);
    messagePublisher.setConnectionFactory(connectionFactory);
  }

  @Test
  public void testDispatchMessage_givenMessage_shouldPublish() throws Exception{
    final ArgumentCaptor<AckHandler> captor = ArgumentCaptor.forClass(AckHandler.class);
    messagePublisher.dispatchMessage(STUDENT_API_TOPIC, "Test".getBytes());
    verify(connection, atMostOnce()).publish(eq(STUDENT_API_TOPIC), aryEq("Test".getBytes()), captor.capture());
  }

  @Test
  public void testRetryPublish_givenMessage_shouldPublish() throws Exception{
    final ArgumentCaptor<AckHandler> captor = ArgumentCaptor.forClass(AckHandler.class);
    messagePublisher.retryPublish(STUDENT_API_TOPIC, "Test".getBytes());
    verify(connection, atMostOnce()).publish(eq(STUDENT_API_TOPIC), aryEq("Test".getBytes()), captor.capture());
  }

  @Test
  public void testClose_shouldClose() throws Exception{
    messagePublisher.close();
    verify(connection, atMostOnce()).close();
  }

}
