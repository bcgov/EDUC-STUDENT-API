package ca.bc.gov.educ.api.student.service.v1;

import ca.bc.gov.educ.api.student.constant.EventOutcome;
import ca.bc.gov.educ.api.student.constant.EventType;
import ca.bc.gov.educ.api.student.messaging.MessagePublisher;
import ca.bc.gov.educ.api.student.messaging.stan.Publisher;
import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import ca.bc.gov.educ.api.student.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.student.struct.v1.Event;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_EVENTS_TOPIC;
import static ca.bc.gov.educ.api.student.service.v1.EventHandlerService.PAYLOAD_LOG;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
@SuppressWarnings({"java:S3864", "java:S3776"})
public class EventHandlerDelegatorService {


  /**
   * The constant RESPONDING_BACK_TO_NATS_ON_CHANNEL.
   */
  public static final String RESPONDING_BACK_TO_NATS_ON_CHANNEL = "responding back to NATS on {} channel ";
  private final MessagePublisher messagePublisher;
  private final EventHandlerService eventHandlerService;
  private final Publisher publisher;

  /**
   * Instantiates a new Event handler delegator service.
   *
   * @param messagePublisher    the message publisher
   * @param eventHandlerService the event handler service
   * @param publisher           the publisher
   */
  @Autowired
  public EventHandlerDelegatorService(MessagePublisher messagePublisher, EventHandlerService eventHandlerService, Publisher publisher) {
    this.messagePublisher = messagePublisher;
    this.eventHandlerService = eventHandlerService;
    this.publisher = publisher;
  }

  /**
   * Handle event.
   *
   * @param event   the event
   * @param message the message
   */
  public void handleEvent(final Event event, final Message message) {
    byte[] response;
    Pair<byte[], StudentEvent> pair;
    boolean isSynchronous = message.getReplyTo() != null;
    try {
      switch (event.getEventType()) {
        case GET_STUDENT:
          log.info("received GET_STUDENT event :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = eventHandlerService.handleGetStudentEvent(event, isSynchronous);
          log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, message.getReplyTo() != null ? message.getReplyTo() : event.getReplyTo());
          if (isSynchronous) { // sync, req/reply pattern of nats
            messagePublisher.dispatchMessage(message.getReplyTo(), response);
          } else { // async, pub/sub
            messagePublisher.dispatchMessage(event.getReplyTo(), response);
          }
          break;
        case CREATE_STUDENT:
          log.info("received create student event :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          pair = eventHandlerService.handleCreateStudentEvent(event);
          log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, message.getReplyTo() != null ? message.getReplyTo() : event.getReplyTo());
          if (isSynchronous) { // sync, req/reply pattern of nats
            messagePublisher.dispatchMessage(message.getReplyTo(), pair.getLeft());
          } else { // async, pub/sub
            messagePublisher.dispatchMessage(event.getReplyTo(), pair.getLeft());
          }
          if (pair.getRight() != null) {// this will publish event to STAN for choreography
            publisher.dispatchMessage(STUDENT_EVENTS_TOPIC.toString(), createChoreographyEvent(pair.getRight()));
          }
          break;
        case UPDATE_STUDENT:
          log.info("received update student event :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          pair = eventHandlerService.handleUpdateStudentEvent(event);
          log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, message.getReplyTo() != null ? message.getReplyTo() : event.getReplyTo());
          if (isSynchronous) { // sync, req/reply pattern of nats
            messagePublisher.dispatchMessage(message.getReplyTo(), pair.getLeft());
          } else { // async, pub/sub
            messagePublisher.dispatchMessage(event.getReplyTo(), pair.getLeft());
          }
          if (pair.getRight() != null) {// this will publish event to STAN for choreography
            publisher.dispatchMessage(STUDENT_EVENTS_TOPIC.toString(), createChoreographyEvent(pair.getRight()));
          }
          break;
        case GET_PAGINATED_STUDENT_BY_CRITERIA:
          log.info("received GET_PAGINATED_STUDENT_BY_CRITERIA event :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          eventHandlerService
              .handleGetPaginatedStudent(event)
              .thenAcceptAsync(resBytes -> {
                log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, message.getReplyTo() != null ? message.getReplyTo() : event.getReplyTo());
                if (isSynchronous) { // sync, req/reply pattern of nats
                  messagePublisher.dispatchMessage(message.getReplyTo(), resBytes);
                } else { // async, pub/sub
                  messagePublisher.dispatchMessage(event.getReplyTo(), resBytes);
                }
              });
          break;
        default:
          log.info("silently ignoring other events :: {}", event);
          break;
      }
    } catch (final Exception e) {
      log.error("Exception", e);
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
