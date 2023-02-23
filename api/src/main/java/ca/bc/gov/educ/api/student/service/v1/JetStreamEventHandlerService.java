package ca.bc.gov.educ.api.student.service.v1;

import ca.bc.gov.educ.api.student.repository.v1.StudentEventRepository;
import ca.bc.gov.educ.api.student.struct.v1.ChoreographedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.UUID;

import static ca.bc.gov.educ.api.student.constant.EventStatus.MESSAGE_PUBLISHED;

/**
 * This class will process events from Jet Stream, which is used in choreography pattern, where messages are published if a student is created or updated.
 */
@Service
@Slf4j
public class JetStreamEventHandlerService {

  private final StudentEventRepository studentEventRepository;


  /**
   * Instantiates a new Stan event handler service.
   *
   * @param studentEventRepository the student event repository
   */
  @Autowired
  public JetStreamEventHandlerService(StudentEventRepository studentEventRepository) {
    this.studentEventRepository = studentEventRepository;
  }

  /**
   * Update event status.
   *
   * @param choreographedEvent the choreographed event
   */
  @Transactional
  public void updateEventStatus(ChoreographedEvent choreographedEvent) {
    if (choreographedEvent != null && choreographedEvent.getEventID() != null) {
      var eventID = UUID.fromString(choreographedEvent.getEventID());
      var eventOptional = studentEventRepository.findById(eventID);
      if (eventOptional.isPresent()) {
        var studentEvent = eventOptional.get();
        studentEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
        studentEventRepository.save(studentEvent);
      }
    }
  }
}
