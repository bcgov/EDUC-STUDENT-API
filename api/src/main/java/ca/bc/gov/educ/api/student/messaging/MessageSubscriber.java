package ca.bc.gov.educ.api.student.messaging;

import ca.bc.gov.educ.api.student.service.v1.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.student.struct.v1.Event;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_API_TOPIC;


@Component
@Slf4j
public class MessageSubscriber extends MessagePubSub {
  private final Executor messageProcessingThreads = Executors.newFixedThreadPool(10);
  private final EventHandlerDelegatorService eventHandlerDelegatorServiceV1;

  @Autowired
  public MessageSubscriber(final Connection con, EventHandlerDelegatorService eventHandlerDelegatorServiceV1) {
    this.eventHandlerDelegatorServiceV1 = eventHandlerDelegatorServiceV1;
    super.connection = con;
  }

  /**
   * This subscription will makes sure the messages are required to acknowledge manually to STAN.
   * Subscribe.
   */
  @PostConstruct
  public void subscribe() {
    String queue = STUDENT_API_TOPIC.toString().replace("_", "-");
    var dispatcher = connection.createDispatcher(onMessage());
    dispatcher.subscribe(STUDENT_API_TOPIC.toString(), queue);
  }

  /**
   * On message message handler.
   *
   * @return the message handler
   */
  private MessageHandler onMessage() {
    return (Message message) -> {
      if (message != null) {
        try {
          var eventString = new String(message.getData());
          var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          if(event.getPayloadVersion() == null){
            event.setPayloadVersion("V1");
          }
          //place holder to have different versions
          if("V1".equalsIgnoreCase(event.getPayloadVersion())){
            messageProcessingThreads.execute(() -> eventHandlerDelegatorServiceV1.handleEvent(event, message));
          }
        } catch (final Exception e) {
          log.error("Exception ", e);
        }
      }
    };
  }


}
