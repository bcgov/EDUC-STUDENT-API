package ca.bc.gov.educ.api.student.schedulers;

import ca.bc.gov.educ.api.student.constant.EventOutcome;
import ca.bc.gov.educ.api.student.constant.EventType;
import ca.bc.gov.educ.api.student.messaging.stan.Publisher;
import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import ca.bc.gov.educ.api.student.repository.v1.StudentEventRepository;
import ca.bc.gov.educ.api.student.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.student.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_EVENTS_TOPIC;

/**
 * This class is responsible to check the STUDENT_EVENT table periodically and publish messages to STAN, if some them are not yet published
 * this is a very edge case scenario which will occur.
 */
@Component
@Slf4j
public class STANEventScheduler {

  private final StudentEventRepository studentEventRepository;
  private final Publisher publisher;

  /**
   * Instantiates a new Stan event scheduler.
   *
   * @param studentEventRepository the student event repository
   * @param publisher              the publisher
   */
  public STANEventScheduler(StudentEventRepository studentEventRepository, Publisher publisher) {
    this.studentEventRepository = studentEventRepository;
    this.publisher = publisher;
  }

  /**
   * Find and publish student events to stan.
   */
  @Scheduled(cron = "0 0/5 * * * *") // every 5 minutes
  @SchedulerLock(name = "PUBLISH_STUDENT_EVENTS_TO_STAN", lockAtLeastFor = "PT4M", lockAtMostFor = "PT4M")
  public void findAndPublishStudentEventsToSTAN() {
    LockAssert.assertLocked();
    var results = studentEventRepository.findByEventStatus(DB_COMMITTED.toString());
    if (!results.isEmpty()) {
      results.forEach(el -> {
        if (el.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(5))) {
          try {
            publisher.dispatchMessage(STUDENT_EVENTS_TOPIC.toString(), createChoreographyEvent(el));
          } catch (final Exception ex) {
            log.error("Exception while trying to publish message", ex);
          }
        }
      });
    }

  }

  private byte[] createChoreographyEvent(StudentEvent event) throws JsonProcessingException {
    ChoreographedEvent choreographedEvent = new ChoreographedEvent();
    choreographedEvent.setEventType(EventType.valueOf(event.getEventType()));
    choreographedEvent.setEventOutcome(EventOutcome.valueOf(event.getEventOutcome()));
    choreographedEvent.setEventPayload(event.getEventPayload());
    choreographedEvent.setEventID(event.getEventId().toString());
    return JsonUtil.getJsonBytesFromObject(choreographedEvent);
  }
}
