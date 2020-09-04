package ca.bc.gov.educ.api.student.schedulers;

import ca.bc.gov.educ.api.student.constant.EventOutcome;
import ca.bc.gov.educ.api.student.constant.EventType;
import ca.bc.gov.educ.api.student.messaging.MessagePublisher;
import ca.bc.gov.educ.api.student.model.StudentEvent;
import ca.bc.gov.educ.api.student.repository.StudentEventRepository;
import ca.bc.gov.educ.api.student.struct.Event;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.student.constant.EventOutcome.STUDENT_FOUND;
import static ca.bc.gov.educ.api.student.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.student.constant.EventType.GET_STUDENT;
import static ca.bc.gov.educ.api.student.constant.EventType.STUDENT_EVENT_OUTBOX_PROCESSED;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringRunner.class)
//@ActiveProfiles("test-event")
@SpringBootTest
public class EventTaskSchedulerTest {
  public static final String STUDENT_API_TOPIC = "STUDENT_API_TOPIC";
  @Mock
  StudentEventRepository studentEventRepository;

  @InjectMocks
  private EventTaskScheduler eventTaskScheduler;

  @Mock
  private MessagePublisher messagePublisher;

  @Before
  public void setUp() {
    initMocks(this);
  }

  @After
  public void tearDown(){
    studentEventRepository.deleteAll();
  }

  @Test
  public void testEventTaskScheduler_givenNoRecords_shouldDoNothing() throws Exception{
    when(studentEventRepository.findByEventStatus(DB_COMMITTED.toString())).thenReturn(List.of());
    eventTaskScheduler.pollEventTableAndPublish();
    verify(messagePublisher, never()).dispatchMessage(STUDENT_API_TOPIC, "".getBytes());
  }

  @Test
  public void testEventTaskScheduler_givenOneRecordWithReplyTo_shouldInvokeMessagePublisherToPublishMessage() throws Exception{

    var sagaId = UUID.randomUUID();
    var studentEvent = StudentEvent.builder().eventId(UUID.randomUUID()).sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(GET_STUDENT.toString()).eventOutcome(STUDENT_FOUND.toString()).
      eventStatus(DB_COMMITTED.toString()).eventPayload("").createDate(LocalDateTime.now()).createUser("TEST").build();
    when(studentEventRepository.findByEventStatus(DB_COMMITTED.toString())).thenReturn(List.of(studentEvent));
    eventTaskScheduler.pollEventTableAndPublish();
    var eventInBytes = JsonUtil.getJsonStringFromObject(Event.builder()
      .sagaId(studentEvent.getSagaId())
      .eventType(EventType.valueOf(studentEvent.getEventType()))
      .eventOutcome(EventOutcome.valueOf(studentEvent.getEventOutcome()))
      .eventPayload(studentEvent.getEventPayload()).build()).getBytes();
    var selfEventInBytes = JsonUtil.getJsonStringFromObject(Event.builder().eventType(STUDENT_EVENT_OUTBOX_PROCESSED).eventPayload(studentEvent.getEventId().toString()).build()).getBytes();
    doNothing().when(messagePublisher).dispatchMessage(STUDENT_API_TOPIC,eventInBytes);
    verify(messagePublisher, atMostOnce()).dispatchMessage(STUDENT_API_TOPIC,eventInBytes);
    verify(messagePublisher, atMostOnce()).dispatchMessage(STUDENT_API_TOPIC,selfEventInBytes);
  }

}