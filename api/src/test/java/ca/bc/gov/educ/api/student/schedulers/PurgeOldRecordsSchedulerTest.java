package ca.bc.gov.educ.api.student.schedulers;

import ca.bc.gov.educ.api.student.StudentApiApplication;
import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import ca.bc.gov.educ.api.student.repository.v1.StudentEventRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = StudentApiApplication.class)
@AutoConfigureMockMvc
public class PurgeOldRecordsSchedulerTest {

  @Autowired
  StudentEventRepository eventRepository;

  @Autowired
  PurgeOldRecordsScheduler purgeOldRecordsScheduler;

  @After
  public void after() {
    this.eventRepository.deleteAll();
  }

  @Test
  public void testPurgeOldRecords_givenOldRecordsPresent_shouldBeDeleted() {
    final var payload = " {\n" +
        "    \"createUser\": \"test\",\n" +
        "    \"updateUser\": \"test\",\n" +
        "    \"legalFirstName\": \"Jack\"\n" +
        "  }";

    final var yesterday = LocalDateTime.now().minusDays(1);

    this.eventRepository.save(this.getEvent(payload, LocalDateTime.now()));

    this.eventRepository.save(this.getEvent(payload, yesterday));

    this.purgeOldRecordsScheduler.setEventRecordStaleInDays(1);
    this.purgeOldRecordsScheduler.purgeOldRecords();

    final var servicesEvents = this.eventRepository.findAll();
    assertThat(servicesEvents).hasSize(1);
  }


  private StudentEvent getEvent(final String payload, final LocalDateTime createDateTime) {
    return StudentEvent
      .builder()
      .eventPayloadBytes(payload.getBytes())
      .eventStatus("MESSAGE_PUBLISHED")
      .eventType("UPDATE_STUDENT")
      .sagaId(UUID.randomUUID())
      .eventOutcome("STUDENT_UPDATED")
      .replyChannel("TEST_CHANNEL")
      .createDate(createDateTime)
      .createUser("STUDENT_API")
      .updateUser("STUDENT_API")
      .updateDate(createDateTime)
      .build();
  }
}
