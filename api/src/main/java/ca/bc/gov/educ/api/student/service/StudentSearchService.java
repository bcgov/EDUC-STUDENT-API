package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.exception.StudentRuntimeException;
import ca.bc.gov.educ.api.student.filter.FilterOperation;
import ca.bc.gov.educ.api.student.filter.StudentFilterSpecs;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.struct.Condition;
import ca.bc.gov.educ.api.student.struct.Search;
import ca.bc.gov.educ.api.student.struct.SearchCriteria;
import ca.bc.gov.educ.api.student.struct.ValueType;
import ca.bc.gov.educ.api.student.util.RequestUtil;
import ca.bc.gov.educ.api.student.util.TransformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentSearchService {
  private final StudentFilterSpecs studentFilterSpecs;

  public StudentSearchService(StudentFilterSpecs studentFilterSpecs) {
    this.studentFilterSpecs = studentFilterSpecs;
  }

  /**
   * Gets specifications.
   *
   * @param studentSpecs the pen reg batch specs
   * @param i            the
   * @param search       the search
   * @return the specifications
   */
  public Specification<StudentEntity> getSpecifications(Specification<StudentEntity> studentSpecs, int i, Search search) {
    if (i == 0) {
      studentSpecs = getStudentEntitySpecification(search.getSearchCriteriaList());
    } else {
      if (search.getCondition() == Condition.AND) {
        studentSpecs = studentSpecs.and(getStudentEntitySpecification(search.getSearchCriteriaList()));
      } else {
        studentSpecs = studentSpecs.or(getStudentEntitySpecification(search.getSearchCriteriaList()));
      }
    }
    return studentSpecs;
  }

  private Specification<StudentEntity> getStudentEntitySpecification(List<SearchCriteria> criteriaList) {
    Specification<StudentEntity> studentSpecs = null;
    if (!criteriaList.isEmpty()) {
      int i = 0;
      for (SearchCriteria criteria : criteriaList) {
        if (criteria.getKey() != null && criteria.getOperation() != null && criteria.getValueType() != null) {
          var criteriaValue = criteria.getValue();
          if(criteriaValue != null && TransformUtil.isUppercaseField(StudentEntity.class, criteria.getKey())) {
            criteriaValue = criteriaValue.toUpperCase();
          }
          Specification<StudentEntity> typeSpecification = getTypeSpecification(criteria.getKey(), criteria.getOperation(), criteriaValue, criteria.getValueType());
          studentSpecs = getSpecificationPerGroup(studentSpecs, i, criteria, typeSpecification);
          i++;
        } else {
          throw new InvalidParameterException("Search Criteria can not contain null values for key, value and operation type");
        }
      }
    }
    return studentSpecs;
  }

  /**
   * Gets specification per group.
   *
   * @param studentEntitySpecification the pen request batch entity specification
   * @param i                          the
   * @param criteria                   the criteria
   * @param typeSpecification          the type specification
   * @return the specification per group
   */
  private Specification<StudentEntity> getSpecificationPerGroup(Specification<StudentEntity> studentEntitySpecification, int i, SearchCriteria criteria, Specification<StudentEntity> typeSpecification) {
    if (i == 0) {
      studentEntitySpecification = Specification.where(typeSpecification);
    } else {
      if (criteria.getCondition() == Condition.AND) {
        studentEntitySpecification = studentEntitySpecification.and(typeSpecification);
      } else {
        studentEntitySpecification = studentEntitySpecification.or(typeSpecification);
      }
    }
    return studentEntitySpecification;
  }

  private Specification<StudentEntity> getTypeSpecification(String key, FilterOperation filterOperation, String value, ValueType valueType) {
    Specification<StudentEntity> studentEntitySpecification = null;
    switch (valueType) {
      case STRING:
        studentEntitySpecification = studentFilterSpecs.getStringTypeSpecification(key, value, filterOperation);
        break;
      case DATE_TIME:
        studentEntitySpecification = studentFilterSpecs.getDateTimeTypeSpecification(key, value, filterOperation);
        break;
      case LONG:
        studentEntitySpecification = studentFilterSpecs.getLongTypeSpecification(key, value, filterOperation);
        break;
      case INTEGER:
        studentEntitySpecification = studentFilterSpecs.getIntegerTypeSpecification(key, value, filterOperation);
        break;
      case DATE:
        studentEntitySpecification = studentFilterSpecs.getDateTypeSpecification(key, value, filterOperation);
        break;
      case UUID:
        studentEntitySpecification = studentFilterSpecs.getUUIDTypeSpecification(key, value, filterOperation);
        break;
      default:
        break;
    }
    return studentEntitySpecification;
  }

  public Specification<StudentEntity> setSpecificationAndSortCriteria(String sortCriteriaJson, String searchCriteriaListJson, ObjectMapper objectMapper, List<Sort.Order> sorts) {
    Specification<StudentEntity> studentSpecs = null;
    try {
      RequestUtil.getSortCriteria(sortCriteriaJson, objectMapper, sorts);
      if (StringUtils.isNotBlank(searchCriteriaListJson)) {
        List<Search> searches = objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {
        });
        int i = 0;
        for (var search : searches) {
          studentSpecs = getSpecifications(studentSpecs, i, search);
          i++;
        }
      }
    } catch (JsonProcessingException e) {
      throw new StudentRuntimeException(e.getMessage());
    }
    return studentSpecs;
  }
}
