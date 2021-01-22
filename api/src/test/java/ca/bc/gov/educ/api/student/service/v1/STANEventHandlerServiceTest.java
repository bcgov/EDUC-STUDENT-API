package ca.bc.gov.educ.api.student.service.v1;

import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import ca.bc.gov.educ.api.student.repository.v1.StudentEventRepository;
import ca.bc.gov.educ.api.student.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.student.struct.v1.StudentCreate;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.student.constant.EventOutcome.STUDENT_CREATED;
import static ca.bc.gov.educ.api.student.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.student.constant.EventStatus.MESSAGE_PUBLISHED;
import static ca.bc.gov.educ.api.student.constant.EventType.CREATE_STUDENT;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class STANEventHandlerServiceTest {

  @Autowired
  STANEventHandlerService stanEventHandlerService;

  @Autowired
  StudentEventRepository studentEventRepository;

  @After
  public void tearDown() {
    studentEventRepository.deleteAll();
  }

  @Test
  public void testUpdateEventStatus_givenNoDataInDB_shouldDONothing() throws JsonProcessingException {
    ChoreographedEvent choreographedEvent = new ChoreographedEvent();
    choreographedEvent.setEventID(UUID.randomUUID().toString());
    choreographedEvent.setEventOutcome(STUDENT_CREATED);
    choreographedEvent.setEventType(CREATE_STUDENT);
    choreographedEvent.setEventPayload(JsonUtil.getJsonStringFromObject(new StudentCreate()));
    stanEventHandlerService.updateEventStatus(choreographedEvent);
    var results = studentEventRepository.findByEventStatus(MESSAGE_PUBLISHED.toString());
    assertThat(results).isEmpty();
  }

  @Test
  public void testUpdateEventStatus_givenDataInDB_shouldUpdateStatus() throws JsonProcessingException {
    var studentEvent = studentEventRepository.save(createStudentEvent());
    ChoreographedEvent choreographedEvent = new ChoreographedEvent();
    choreographedEvent.setEventID(studentEvent.getEventId().toString());
    choreographedEvent.setEventOutcome(STUDENT_CREATED);
    choreographedEvent.setEventType(CREATE_STUDENT);
    choreographedEvent.setEventPayload(JsonUtil.getJsonStringFromObject(new StudentCreate()));
    stanEventHandlerService.updateEventStatus(choreographedEvent);
    var results = studentEventRepository.findByEventStatus(MESSAGE_PUBLISHED.toString());
    assertThat(results).hasSize(1);
    assertThat(results.get(0)).isNotNull();
  }

  private StudentEvent createStudentEvent() throws JsonProcessingException {
    return StudentEvent.builder()
        .eventId(UUID.randomUUID())
        .createDate(LocalDateTime.now())
        .createUser("TEST")
        .eventOutcome(STUDENT_CREATED.toString())
        .eventStatus(DB_COMMITTED.toString())
        .eventType(CREATE_STUDENT.toString())
        .eventPayload(JsonUtil.getJsonStringFromObject(new StudentCreate()))
        .updateDate(LocalDateTime.now())
        .updateUser("TEST")
        .build();
  }
}
