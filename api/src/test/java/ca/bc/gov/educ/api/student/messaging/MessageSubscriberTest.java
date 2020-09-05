package ca.bc.gov.educ.api.student.messaging;

import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import ca.bc.gov.educ.api.student.service.EventHandlerService;
import io.nats.streaming.*;
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
@ActiveProfiles("test-message-sub")
@SpringBootTest
public class MessageSubscriberTest {
  public static final String STUDENT_API_TOPIC = "STUDENT_API_TOPIC";

  @Mock
  private StreamingConnection connection;
  @Mock
  private StreamingConnectionFactory connectionFactory;
  @Autowired
  private ApplicationProperties applicationProperties;
  @Autowired
  private EventHandlerService eventHandlerService;
  private MessageSubscriber messageSubscriber;

  @Before
  public void setUp() throws IOException, InterruptedException {
    initMocks(this);
    messageSubscriber = new MessageSubscriber(applicationProperties, eventHandlerService);
    messageSubscriber.setConnection(connection);
    messageSubscriber.setConnectionFactory(connectionFactory);
  }

  @Test
  public void testSubscribe_shouldSubscribe() throws Exception{
    final ArgumentCaptor<MessageHandler> handlerCaptor = ArgumentCaptor.forClass(MessageHandler.class);
    final ArgumentCaptor<SubscriptionOptions> optionsCaptor = ArgumentCaptor.forClass(SubscriptionOptions.class);
    messageSubscriber.subscribe();
    verify(connection, atMostOnce()).subscribe(eq(STUDENT_API_TOPIC), eq("student"), handlerCaptor.capture(), optionsCaptor.capture());
  }

  @Test
  public void testClose_shouldClose() throws Exception{
    messageSubscriber.close();
    verify(connection, atMostOnce()).close();
  }

}
