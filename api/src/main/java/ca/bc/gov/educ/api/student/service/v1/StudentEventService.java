package ca.bc.gov.educ.api.student.service.v1;

import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import ca.bc.gov.educ.api.student.struct.v1.StudentCreate;
import ca.bc.gov.educ.api.student.struct.v1.StudentUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


/**
 * This class is just a wrapper over {@link StudentService} to make transactions boundaries atomic for the controller.
 */
@Service
@Slf4j
public class StudentEventService {

  private final StudentService studentService;

  /**
   * Instantiates a new Student event service.
   *
   * @param studentService the student service
   */
  @Autowired
  public StudentEventService(StudentService studentService) {
    this.studentService = studentService;
  }

  /**
   * Create student pair.
   *
   * @param studentCreate the student create
   * @return the pair
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Pair<StudentEntity, StudentEvent> createStudent(StudentCreate studentCreate) throws JsonProcessingException {
    return studentService.createStudent(studentCreate);
  }

  /**
   * Update student pair.
   *
   * @param studentUpdate the student update
   * @param studentID     the student id
   * @return the pair
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Pair<StudentEntity, StudentEvent> updateStudent(StudentUpdate studentUpdate, UUID studentID) throws JsonProcessingException {
    return studentService.updateStudent(studentUpdate, studentID);
  }
}
