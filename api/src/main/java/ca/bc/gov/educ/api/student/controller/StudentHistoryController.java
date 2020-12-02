package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.endpoint.StudentHistoryEndpoint;
import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.exception.StudentRuntimeException;
import ca.bc.gov.educ.api.student.filter.FilterOperation;
import ca.bc.gov.educ.api.student.filter.StudentHistoryFilterSpecs;
import ca.bc.gov.educ.api.student.mappers.StudentHistoryMapper;
import ca.bc.gov.educ.api.student.model.StudentHistoryEntity;
import ca.bc.gov.educ.api.student.service.StudentHistoryService;
import ca.bc.gov.educ.api.student.struct.*;
import ca.bc.gov.educ.api.student.util.RequestUtil;
import ca.bc.gov.educ.api.student.util.TransformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
  private final StudentHistoryFilterSpecs studentHistoryFilterSpecs;

  @Autowired
  StudentHistoryController(final StudentHistoryService studentHistoryService, StudentHistoryFilterSpecs studentHistoryFilterSpecs) {
    this.service = studentHistoryService;
    this.studentHistoryFilterSpecs = studentHistoryFilterSpecs;
  }

  @Override
  public List<StudentHistoryActivityCode> getStudentHistoryActivityCodes() {
    return getService().getStudentHistoryActivityCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public CompletableFuture<Page<StudentHistory>> findStudentHistoryByStudentID(String studentID, Integer pageNumber, Integer pageSize, String sortCriteriaJson) {
    final ObjectMapper objectMapper = new ObjectMapper();
    final List<Sort.Order> sorts = new ArrayList<>();
    try {
      RequestUtil.getSortCriteria(sortCriteriaJson, objectMapper, sorts);
    } catch (JsonProcessingException e) {
      throw new StudentRuntimeException(e.getMessage());
    }
    return getService().findStudentHistoryByStudentID(pageNumber, pageSize, sorts, studentID).thenApplyAsync(studentHistoryEntities -> studentHistoryEntities.map(mapper::toStructure));
  }

  @Override
  public CompletableFuture<Page<StudentHistory>> findAll(Integer pageNumber, Integer pageSize, String sortCriteriaJson, String searchCriteriaListJson) {
    final ObjectMapper objectMapper = new ObjectMapper();
    final List<Sort.Order> sorts = new ArrayList<>();
    Specification<StudentHistoryEntity> studentHistorySpecs = null;
    try {
      RequestUtil.getSortCriteria(sortCriteriaJson, objectMapper, sorts);
      if (StringUtils.isNotBlank(searchCriteriaListJson)) {
        List<Search> searches = objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {
        });
        int i = 0;
        for (var search : searches) {
          studentHistorySpecs = getSpecifications(studentHistorySpecs, i, search);
          i++;
        }
      }
    } catch (JsonProcessingException e) {
      throw new StudentRuntimeException(e.getMessage());
    }
    return getService().findAll(studentHistorySpecs, pageNumber, pageSize, sorts).thenApplyAsync(studentHistoryEntities -> studentHistoryEntities.map(mapper::toStructure));
  }

  /**
   * Gets specifications.
   *
   * @param studentHistorySpecs   the student history entity specs
   * @param i                     the
   * @param search                the search
   * @return the specifications
   */
  private Specification<StudentHistoryEntity> getSpecifications(Specification<StudentHistoryEntity> studentHistorySpecs, int i, Search search) {
    if (i == 0) {
      studentHistorySpecs = getStudentHistoryEntitySpecification(search.getSearchCriteriaList());
    } else {
      if (search.getCondition() == Condition.AND) {
        studentHistorySpecs = studentHistorySpecs.and(getStudentHistoryEntitySpecification(search.getSearchCriteriaList()));
      } else {
        studentHistorySpecs = studentHistorySpecs.or(getStudentHistoryEntitySpecification(search.getSearchCriteriaList()));
      }
    }
    return studentHistorySpecs;
  }

  private Specification<StudentHistoryEntity> getStudentHistoryEntitySpecification(List<SearchCriteria> criteriaList) {
    Specification<StudentHistoryEntity> studentHistorySpecs = null;
    if (!criteriaList.isEmpty()) {
      int i = 0;
      for (SearchCriteria criteria : criteriaList) {
        if (criteria.getKey() != null && criteria.getOperation() != null && criteria.getValueType() != null) {
          var criteriaValue = criteria.getValue();
          if(criteriaValue != null && TransformUtil.isUppercaseField(StudentHistoryEntity.class, criteria.getKey())) {
            criteriaValue = criteriaValue.toUpperCase();
          }
          Specification<StudentHistoryEntity> typeSpecification = getTypeSpecification(criteria.getKey(), criteria.getOperation(), criteriaValue, criteria.getValueType());
          studentHistorySpecs = getSpecificationPerGroup(studentHistorySpecs, i, criteria, typeSpecification);
          i++;
        } else {
          throw new InvalidParameterException("Search Criteria can not contain null values for key, value and operation type");
        }
      }
    }
    return studentHistorySpecs;
  }

  /**
   * Gets specification per group.
   *
   * @param studentHistoryEntitySpecification the student history entity specification
   * @param i                                 the
   * @param criteria                          the criteria
   * @param typeSpecification                 the type specification
   * @return the specification per group
   */
  private Specification<StudentHistoryEntity> getSpecificationPerGroup(Specification<StudentHistoryEntity> studentHistoryEntitySpecification, int i, SearchCriteria criteria, Specification<StudentHistoryEntity> typeSpecification) {
    if (i == 0) {
      studentHistoryEntitySpecification = Specification.where(typeSpecification);
    } else {
      if (criteria.getCondition() == Condition.AND) {
        studentHistoryEntitySpecification = studentHistoryEntitySpecification.and(typeSpecification);
      } else {
        studentHistoryEntitySpecification = studentHistoryEntitySpecification.or(typeSpecification);
      }
    }
    return studentHistoryEntitySpecification;
  }

  private Specification<StudentHistoryEntity> getTypeSpecification(String key, FilterOperation filterOperation, String value, ValueType valueType) {
    Specification<StudentHistoryEntity> studentHistoryEntitySpecification = null;
    switch (valueType) {
      case STRING:
        studentHistoryEntitySpecification = studentHistoryFilterSpecs.getStringTypeSpecification(key, value, filterOperation);
        break;
      case DATE_TIME:
        studentHistoryEntitySpecification = studentHistoryFilterSpecs.getDateTimeTypeSpecification(key, value, filterOperation);
        break;
      case LONG:
        studentHistoryEntitySpecification = studentHistoryFilterSpecs.getLongTypeSpecification(key, value, filterOperation);
        break;
      case INTEGER:
        studentHistoryEntitySpecification = studentHistoryFilterSpecs.getIntegerTypeSpecification(key, value, filterOperation);
        break;
      case DATE:
        studentHistoryEntitySpecification = studentHistoryFilterSpecs.getDateTypeSpecification(key, value, filterOperation);
        break;
      case UUID:
        studentHistoryEntitySpecification = studentHistoryFilterSpecs.getUUIDTypeSpecification(key, value, filterOperation);
        break;
      default:
        break;
    }
    return studentHistoryEntitySpecification;
  }

}
