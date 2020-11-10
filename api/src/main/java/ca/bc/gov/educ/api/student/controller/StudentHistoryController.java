package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.endpoint.StudentHistoryEndpoint;
import ca.bc.gov.educ.api.student.exception.StudentRuntimeException;
import ca.bc.gov.educ.api.student.mappers.StudentHistoryMapper;
import ca.bc.gov.educ.api.student.service.StudentHistoryService;
import ca.bc.gov.educ.api.student.struct.*;
import ca.bc.gov.educ.api.student.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * Student History Controller
 *
 */

@RestController
@EnableResourceServer
@Slf4j
public class StudentHistoryController implements StudentHistoryEndpoint {
  @Getter(AccessLevel.PRIVATE)
  private final StudentHistoryService service;

  private static final StudentHistoryMapper mapper = StudentHistoryMapper.mapper;

  @Autowired
  StudentHistoryController(final StudentHistoryService studentHistoryService) {
    this.service = studentHistoryService;
  }

  @Override
  public List<StudentHistoryActivityCode> getStudentHistoryActivityCodes() {
    return getService().getStudentHistoryActivityCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public CompletableFuture<Page<StudentHistory>> findStudentHistoryByStudentID(Integer pageNumber, Integer pageSize, String sortCriteriaJson, String studentID) {
    final ObjectMapper objectMapper = new ObjectMapper();
    final List<Sort.Order> sorts = new ArrayList<>();
    try {
      RequestUtil.getSortCriteria(sortCriteriaJson, objectMapper, sorts);
    } catch (JsonProcessingException e) {
      throw new StudentRuntimeException(e.getMessage());
    }
    return getService().findStudentHistoryByStudentID(pageNumber, pageSize, sorts, studentID).thenApplyAsync(studentHistoryEntities -> studentHistoryEntities.map(mapper::toStructure));
  }
}
