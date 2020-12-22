package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.messaging.MessagePublisher;
import ca.bc.gov.educ.api.student.struct.*;
import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static ca.bc.gov.educ.api.student.service.EventHandlerService.PAYLOAD_LOG;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
@SuppressWarnings("java:S3864")
public class EventHandlerDelegatorService {


  private final MessagePublisher messagePublisher;
  private final EventHandlerService eventHandlerService;

  /**
   * Instantiates a new Event handler delegator service.
   *
   * @param messagePublisher    the message publisher
   * @param eventHandlerService the event handler service
   */
  @Autowired
  public EventHandlerDelegatorService(MessagePublisher messagePublisher, EventHandlerService eventHandlerService) {
    this.messagePublisher = messagePublisher;
    this.eventHandlerService = eventHandlerService;
  }

  /**
   * Handle event.
   *
   * @param event   the event
   * @param message the message
   */
  public void handleEvent(final Event event, final Message message) {
    byte[] response;
    boolean isSynchronous = message.getReplyTo() != null;
    try {
      switch (event.getEventType()) {
        case GET_STUDENT:
          log.info("received GET_STUDENT event :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = eventHandlerService.handleGetStudentEvent(event, isSynchronous);
          if (isSynchronous) { // sync, req/reply pattern of nats
            messagePublisher.dispatchMessage(message.getReplyTo(), response);
          }
          else  { // async, pub/sub
            messagePublisher.dispatchMessage(event.getReplyTo(), response);
          }
          break;
        case CREATE_STUDENT:
          log.info("received create student event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = eventHandlerService.handleCreateStudentEvent(event);
          messagePublisher.dispatchMessage(event.getReplyTo(), response);
          break;
        case UPDATE_STUDENT:
          log.info("received update student event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = eventHandlerService.handleUpdateStudentEvent(event);
          messagePublisher.dispatchMessage(event.getReplyTo(), response);
          break;
        case ADD_STUDENT_TWINS:
          log.info("received ADD_STUDENT_TWINS event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = eventHandlerService.handleAddStudentTwins(event);
          messagePublisher.dispatchMessage(event.getReplyTo(), response);
          break;
        case DELETE_STUDENT_TWINS:
          log.info("received DELETE_STUDENT_TWINS event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = eventHandlerService.handleDeleteStudentTwins(event);
          messagePublisher.dispatchMessage(event.getReplyTo(), response);
          break;
        case GET_PAGINATED_STUDENT_BY_CRITERIA:
          log.info("received GET_PAGINATED_STUDENT_BY_CRITERIA event :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          eventHandlerService
              .handleGetPaginatedStudent(event)
              .thenAcceptAsync(resBytes -> {
                if (isSynchronous) { // sync, req/reply pattern of nats
                  messagePublisher.dispatchMessage(message.getReplyTo(), resBytes);
                }
                else  { // async, pub/sub
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

}
