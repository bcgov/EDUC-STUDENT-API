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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.student.constant.EventOutcome.STUDENT_FOUND;
import static ca.bc.gov.educ.api.student.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.student.constant.EventType.GET_STUDENT;
import static ca.bc.gov.educ.api.student.constant.EventType.STUDENT_EVENT_OUTBOX_PROCESSED;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test-event")
@SpringBootTest
public class EventTaskSchedulerTest {
  public static final String STUDENT_API_TOPIC = "STUDENT_API_TOPIC";
  @Autowired
  StudentEventRepository studentEventRepository;

  @Autowired
  private EventTaskScheduler eventTaskScheduler;

  @Mock
  private MessagePublisher messagePublisher;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown(){
    studentEventRepository.deleteAll();
  }

  @Test
  public void testEventTaskScheduler_givenNoRecords_shouldDoNothing() throws Exception{
    eventTaskScheduler.pollEventTableAndPublish();
    verify(messagePublisher, never()).dispatchMessage(STUDENT_API_TOPIC, "".getBytes());
  }

  @Test
  public void testEventTaskScheduler_givenOneRecordWithReplyTo_shouldInvokeMessagePublisherToPublishMessage() throws Exception{
    var sagaId = UUID.randomUUID();
    var studentEvent = StudentEvent.builder().sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(GET_STUDENT.toString()).eventOutcome(STUDENT_FOUND.toString()).
      eventStatus(DB_COMMITTED.toString()).eventPayload("").createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(studentEvent);
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
