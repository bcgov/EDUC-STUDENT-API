package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.endpoint.StudentEndpoint;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.struct.Student;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


/**
 * Student controller
 *
 * @author John Cox
 */

@RestController
@EnableResourceServer
@Slf4j
public class StudentController implements StudentEndpoint {

  @Getter(AccessLevel.PRIVATE)
  private final StudentService service;
  private final StudentMapper mapper = StudentMapper.mapper;

  StudentController(@Autowired final StudentService studentService) {
    this.service = studentService;
  }

  public Student readStudent(String studentID) {
    return mapper.toStructure(service.retrieveStudent(UUID.fromString(studentID)));
  }

  public Student createStudent(Student student) {
    return mapper.toStructure(service.createStudent(mapper.toModel(student)));
  }

  public Student updateStudent(Student student) {
    return mapper.toStructure(service.updateStudent(mapper.toModel(student)));
  }

  @Override
  public String health() {
    log.info("Health Check OK, returning OK");
    return "OK";
  }
}
