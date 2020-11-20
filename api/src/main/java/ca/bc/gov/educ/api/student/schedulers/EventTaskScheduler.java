package ca.bc.gov.educ.api.student.schedulers;

import ca.bc.gov.educ.api.student.constant.EventOutcome;
import ca.bc.gov.educ.api.student.constant.EventType;
import ca.bc.gov.educ.api.student.messaging.MessagePublisher;
import ca.bc.gov.educ.api.student.model.StudentEvent;
import ca.bc.gov.educ.api.student.repository.StudentEventRepository;
import ca.bc.gov.educ.api.student.struct.Event;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static ca.bc.gov.educ.api.student.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.student.constant.EventType.STUDENT_EVENT_OUTBOX_PROCESSED;
import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class EventTaskScheduler {

  @Getter(PRIVATE)
  private final MessagePublisher messagePubSub;
  @Getter(PRIVATE)
  private final StudentEventRepository studentEventRepository;

  @Autowired
  public EventTaskScheduler(MessagePublisher messagePubSub, StudentEventRepository studentEventRepository) {
    this.messagePubSub = messagePubSub;
    this.studentEventRepository = studentEventRepository;
  }

  @Scheduled(cron = "${scheduled.jobs.poll.events}")  // "0/1 * * * * *"
  @SchedulerLock(name = "EventTablePoller",
          lockAtLeastFor = "${scheduled.jobs.poll.events.lockAtLeastFor}", lockAtMostFor = "${scheduled.jobs.poll.events.lockAtMostFor}")
  public void pollEventTableAndPublish() throws IOException {
    List<StudentEvent> events = getStudentEventRepository().findByEventStatus(DB_COMMITTED.toString());
    if (!events.isEmpty()) {
      for (StudentEvent event : events) {
        try {
          if (event.getReplyChannel() != null) {
            getMessagePubSub().dispatchMessage(event.getReplyChannel(), studentEventProcessed(event));
          }
          getMessagePubSub().dispatchMessage(STUDENT_API_TOPIC.toString(), createOutboxEvent(event));
        } catch (IOException e) {
          log.error("exception occurred", e);
          throw e;
        }
      }
    } else {
      log.trace("no unprocessed records.");
    }
  }

  private byte[] studentEventProcessed(StudentEvent studentEvent) throws JsonProcessingException {
    Event event = Event.builder()
            .sagaId(studentEvent.getSagaId())
            .eventType(EventType.valueOf(studentEvent.getEventType()))
            .eventOutcome(EventOutcome.valueOf(studentEvent.getEventOutcome()))
            .eventPayload(studentEvent.getEventPayload()).build();
    return JsonUtil.getJsonStringFromObject(event).getBytes();
  }

  private byte[] createOutboxEvent(StudentEvent studentEvent) throws JsonProcessingException {
    Event event = Event.builder().eventType(STUDENT_EVENT_OUTBOX_PROCESSED).eventPayload(studentEvent.getEventId().toString()).build();
    return JsonUtil.getJsonStringFromObject(event).getBytes();
  }
}
