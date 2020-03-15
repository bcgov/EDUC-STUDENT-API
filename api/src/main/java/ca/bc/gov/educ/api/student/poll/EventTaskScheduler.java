package ca.bc.gov.educ.api.student.poll;

import ca.bc.gov.educ.api.student.constant.EventStatus;
import ca.bc.gov.educ.api.student.messaging.MessagePubSubImpl;
import ca.bc.gov.educ.api.student.model.StudentEvent;
import ca.bc.gov.educ.api.student.repository.StudentEventRepository;
import ca.bc.gov.educ.api.student.struct.Event;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.student.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_TOPIC;
import static ca.bc.gov.educ.api.student.struct.Event.EventType.STUDENT_CREATED_OR_UPDATED;
import static ca.bc.gov.educ.api.student.struct.Event.EventType.STUDENT_EVENT_OUTBOX_PROCESSED;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class EventTaskScheduler {

  @Getter(PRIVATE)
  private final MessagePubSubImpl messagePubSub;
  @Getter(PRIVATE)
  private final StudentEventRepository studentEventRepository;

  @Autowired
  public EventTaskScheduler(MessagePubSubImpl messagePubSub, StudentEventRepository studentEventRepository) {
    this.messagePubSub = messagePubSub;
    this.studentEventRepository = studentEventRepository;
  }

  @Scheduled(cron = "0/1 * * * * *")
  @SchedulerLock(name = "EventTablePoller",
          lockAtLeastFor = "900ms", lockAtMostFor = "950ms")
  public void pollEventTableAndPublish() throws InterruptedException, IOException, TimeoutException {
    List<StudentEvent> events = getStudentEventRepository().findByEventStatus(DB_COMMITTED.toString());
    if (!events.isEmpty()) {
      for (StudentEvent event : events) {
        try {
          Event sagaEvent = JsonUtil.getJsonObjectFromString(Event.class, event.getEventPayload());
          if (sagaEvent.getReplyTo() != null && event.getSagaId() != null) {
            getMessagePubSub().dispatchMessage(sagaEvent.getReplyTo(), createStudentCreatedOrUpdatedEvent(event.getSagaId(), event.getEventPayload()));
          }
          getMessagePubSub().dispatchMessage(STUDENT_TOPIC.toString(), createOutboxEvent(event));
        } catch (InterruptedException | TimeoutException | IOException e) {
          log.error("exception occurred", e);
          throw e;
        }
      }
    } else {
      log.trace("no unprocessed records.");
    }
  }

  private byte[] createStudentCreatedOrUpdatedEvent(UUID sagaId, String eventPayload) throws JsonProcessingException {
    Event event = Event.builder().sagaId(sagaId.toString()).eventType(STUDENT_CREATED_OR_UPDATED).eventPayload(eventPayload).build();
    return JsonUtil.getJsonStringFromObject(event).getBytes();
  }

  private byte[] createOutboxEvent(StudentEvent studentEvent) throws JsonProcessingException {
    Event event = Event.builder().eventType(STUDENT_EVENT_OUTBOX_PROCESSED).eventPayload(studentEvent.getEventId().toString()).build();
    return JsonUtil.getJsonStringFromObject(event).getBytes();
  }
}
