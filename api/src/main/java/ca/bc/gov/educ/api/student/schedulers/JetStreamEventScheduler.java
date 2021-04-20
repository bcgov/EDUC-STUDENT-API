package ca.bc.gov.educ.api.student.schedulers;

import ca.bc.gov.educ.api.student.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.student.repository.v1.StudentEventRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.student.constant.EventStatus.DB_COMMITTED;

/**
 * This class is responsible to check the STUDENT_EVENT table periodically and publish messages to JET STREAM, if some them are not yet published
 * this is a very edge case scenario which will occur.
 */
@Component
@Slf4j
public class JetStreamEventScheduler {

  private final StudentEventRepository studentEventRepository;
  private final Publisher publisher;

  /**
   * Instantiates a new Stan event scheduler.
   *
   * @param studentEventRepository the student event repository
   * @param publisher              the publisher
   */
  public JetStreamEventScheduler(StudentEventRepository studentEventRepository, Publisher publisher) {
    this.studentEventRepository = studentEventRepository;
    this.publisher = publisher;
  }

  /**
   * Find and publish student events to stan.
   */
  @Scheduled(cron = "0 0/5 * * * *") // every 5 minutes
  @SchedulerLock(name = "PUBLISH_STUDENT_EVENTS_TO_JET_STREAM", lockAtLeastFor = "PT4M", lockAtMostFor = "PT4M")
  public void findAndPublishStudentEventsToJetStream() {
    LockAssert.assertLocked();
    var results = studentEventRepository.findByEventStatus(DB_COMMITTED.toString());
    if (!results.isEmpty()) {
      results.forEach(el -> {
        if (el.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(5))) {
          try {
            publisher.dispatchChoreographyEvent(el);
          } catch (final Exception ex) {
            log.error("Exception while trying to publish message", ex);
          }
        }
      });
    }

  }
}
