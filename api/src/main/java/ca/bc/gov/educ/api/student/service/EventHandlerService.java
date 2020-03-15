package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.constant.EventStatus;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.model.StudentEvent;
import ca.bc.gov.educ.api.student.repository.StudentEventRepository;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import ca.bc.gov.educ.api.student.struct.Event;
import ca.bc.gov.educ.api.student.struct.StudentSagaData;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.student.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.student.constant.EventStatus.MESSAGE_PUBLISHED;
import static ca.bc.gov.educ.api.student.struct.Event.EventType.*;
import static lombok.AccessLevel.PRIVATE;

@Service
@Slf4j
public class EventHandlerService {

  @Getter(PRIVATE)
  private final StudentRepository studentRepository;
  private final StudentMapper mapper = StudentMapper.mapper;
  @Getter(PRIVATE)
  private final StudentEventRepository studentEventRepository;

  @Autowired
  public EventHandlerService(final StudentRepository studentRepository, final StudentEventRepository studentEventRepository) {
    this.studentRepository = studentRepository;
    this.studentEventRepository = studentEventRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleEvent(Event event) {
    try {
      if (event.getEventType().toString().equals(PEN_REQUEST_COMPLETE_SAGA.toString())) {
        log.info(event.getEventPayload());
        StudentSagaData studentSagaData = JsonUtil.getJsonObjectFromString(StudentSagaData.class, event.getEventPayload());
        handlePenRequestCompleteEvent(studentSagaData, event);
      } else if (event.getEventType().toString().equals(STUDENT_EVENT_OUTBOX_PROCESSED.toString())) {
        log.info(event.getEventPayload());
        handleStudentOutboxProcessedEvent(event.getEventPayload());
      } else {
        log.info("silently ignoring other events.");
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }

  private void handleStudentOutboxProcessedEvent(String studentEventId) {
    try {
      val studentEventFromDB = getStudentEventRepository().findById(UUID.fromString(studentEventId));
      if (studentEventFromDB.isPresent()) {
        val studEvent = studentEventFromDB.get();
        studEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
        getStudentEventRepository().save(studEvent);
      }
    } catch (final Exception e) {
      log.error("Exception ", e);
    }
  }

  /**
   * Saga should never be null for this type of event.
   *
   * @param studentSagaData the student saga data to be persisted in its own table and event table.
   */
  public void handlePenRequestCompleteEvent(StudentSagaData studentSagaData, Event event) {
    try {
      val studentEventOptional = getStudentEventRepository().findBySagaId(UUID.fromString(studentSagaData.getSagaId()));
      StudentEvent studentEvent;
      if (!studentEventOptional.isPresent()) {
        StudentEntity entity = mapper.mapFromSagaData(studentSagaData);
        val optionalStudent = getStudentRepository().findStudentEntityByPen(entity.getPen());
        if (optionalStudent.isPresent()) {
          StudentEntity existingStudent = optionalStudent.get();
          entity.setStudentID(existingStudent.getStudentID());
          BeanUtils.copyProperties(entity, existingStudent);
          existingStudent.setUpdateDate(LocalDateTime.now());
          entity = getStudentRepository().save(existingStudent);
        } else {
          entity.setCreateDate(LocalDateTime.now());
          entity.setUpdateDate(LocalDateTime.now());
          entity = getStudentRepository().save(entity);
        }
        studentSagaData.setStudentID(entity.getStudentID().toString());
        event.setEventPayload(JsonUtil.getJsonStringFromObject(studentSagaData));//update the payload to include studentId
        event.setReplyTo(null);
        studentEvent = createStudentEventRecord(studentSagaData, event);
      } else { // just update the status of the event so that it will be polled and send again to the saga orchestrator.
        studentEvent = studentEventOptional.get();
        studentEvent.setEventStatus(DB_COMMITTED.toString());
      }
      getStudentEventRepository().save(studentEvent);
    } catch (final Exception e) {
      log.error("Exception ", e);
    }
  }

  private StudentEvent createStudentEventRecord(StudentSagaData studentSagaData, Event event) throws JsonProcessingException {
    return StudentEvent.builder()
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser(studentSagaData.getCreateUser())
            .updateUser(studentSagaData.getUpdateUser())
            .eventPayload(JsonUtil.getJsonStringFromObject(event))
            .eventType(STUDENT_CREATED_OR_UPDATED.toString())
            .sagaId(UUID.fromString(studentSagaData.getSagaId()))
            .eventStatus(DB_COMMITTED.toString())
            .build();
  }
}
