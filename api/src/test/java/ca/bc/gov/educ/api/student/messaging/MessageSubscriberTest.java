package ca.bc.gov.educ.api.student.messaging;

import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import ca.bc.gov.educ.api.student.service.EventHandlerService;
import ca.bc.gov.educ.api.student.struct.Event;
import io.nats.streaming.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringRunner.class)
@ActiveProfiles("test-event")
@SpringBootTest
public class MessageSubscriberTest {
  public static final String STUDENT_API_TOPIC = "STUDENT_API_TOPIC";

  @Mock
  private StreamingConnection connection;
  @Mock
  private StreamingConnectionFactory connectionFactory;
  @Autowired
  private ApplicationProperties applicationProperties;
  @Mock
  private EventHandlerService eventHandlerService;
  private MessageSubscriber messageSubscriber;

  @Before
  public void setUp() throws IOException, InterruptedException {
    initMocks(this);
    messageSubscriber = new MessageSubscriber(applicationProperties, eventHandlerService, false);
    messageSubscriber.setConnectionFactory(connectionFactory);
    when(connectionFactory.createConnection()).thenReturn(connection);
    messageSubscriber.connnect();
  }

  @Test
  public void testSubscribe_shouldSubscribe() throws Exception{
    messageSubscriber.subscribe();
    verify(connection, atMostOnce()).subscribe(eq(STUDENT_API_TOPIC), eq("student"), any(MessageHandler.class), any(SubscriptionOptions.class));
  }

  @Test
  public void testClose_shouldClose() throws Exception{
    messageSubscriber.close();
    verify(connection, atMostOnce()).close();
  }

  @Test
  public void testClose_givenException_shouldClose() throws Exception{
    doAnswer(invocation -> {
      throw new IOException("Test");
    }).when(connection).close();
    messageSubscriber.close();
    verify(connection, atMostOnce()).close();
  }

  @Test
  public void testOnStudentTopicMessage_givenMessage_shouldHandleEvent() {
    Message message = mock(Message.class);
    when(message.getData()).thenReturn("{\"EventType:\":\"GET_STUDENT\",\"EventOutcome\":\"DB_COMMITTED\"}".getBytes());
    messageSubscriber.onStudentTopicMessage(message);
    verify(eventHandlerService, atMostOnce()).handleEvent(any(Event.class));
  }

  @Test
  public void testOnStudentTopicMessage_givenException_shouldLogError() {
    Message message = mock(Message.class);
    when(message.getData()).thenReturn("{\"EventType:\":\"GET_STUDENT\",\"EventOutcome\":\"DB_COMMITTED\"}".getBytes());
    doAnswer(invocation -> {
      throw new Exception("Test");
    }).when(eventHandlerService).handleEvent(any(Event.class));
    messageSubscriber.onStudentTopicMessage(message);
    verify(eventHandlerService, atMostOnce()).handleEvent(any(Event.class));
  }

  @Test
  public void testConnectionLostHandler_givenException_shouldRetryConnectionAndRetrySubscription() throws IOException, InterruptedException, TimeoutException {
    messageSubscriber.connectionLostHandler(connection, new Exception());
    verify(connectionFactory, atMost(2)).createConnection();
    verify(connection, atMost(1)).subscribe(eq(STUDENT_API_TOPIC), eq("student"), any(MessageHandler.class), any(SubscriptionOptions.class));
  }

  @Test
  public void testConnectionLostHandler_givenCreateConnectionException_shouldRetryConnection() throws IOException, InterruptedException, TimeoutException {
    when(connectionFactory.createConnection()).thenThrow(new IOException("Test")).thenReturn(connection);
    messageSubscriber.connectionLostHandler(connection, new Exception());
    verify(connectionFactory, atLeast(3)).createConnection();
  }

  @Test
  public void testConnectionLostHandler_givenSubscribeException_shouldRetrySubscription() throws IOException, InterruptedException, TimeoutException {
    when(connection.subscribe(eq(STUDENT_API_TOPIC), eq("student"), any(MessageHandler.class), any(SubscriptionOptions.class))).thenThrow(new IOException("Test")).thenReturn(mock(Subscription.class));
    messageSubscriber.connectionLostHandler(connection, new Exception());
    verify(connection, atLeast(2)).subscribe(eq(STUDENT_API_TOPIC), eq("student"), any(MessageHandler.class), any(SubscriptionOptions.class));
  }
}
