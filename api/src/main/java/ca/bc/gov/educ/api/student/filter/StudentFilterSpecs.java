package ca.bc.gov.educ.api.student.filter;

import ca.bc.gov.educ.api.student.model.StudentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.function.Function;

@Service
@Slf4j
public class StudentFilterSpecs {

  private final FilterSpecifications<StudentEntity, ChronoLocalDate> dateFilterSpecifications;
  private final FilterSpecifications<StudentEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications;
  private final FilterSpecifications<StudentEntity, Integer> integerFilterSpecifications;
  private final FilterSpecifications<StudentEntity, String> stringFilterSpecifications;
  private final FilterSpecifications<StudentEntity, Long> longFilterSpecifications;
  private final Converters converters;

  public StudentFilterSpecs(FilterSpecifications<StudentEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<StudentEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<StudentEntity, Integer> integerFilterSpecifications, FilterSpecifications<StudentEntity, String> stringFilterSpecifications, FilterSpecifications<StudentEntity, Long> longFilterSpecifications, Converters converters) {
    this.dateFilterSpecifications = dateFilterSpecifications;
    this.dateTimeFilterSpecifications = dateTimeFilterSpecifications;
    this.integerFilterSpecifications = integerFilterSpecifications;
    this.stringFilterSpecifications = stringFilterSpecifications;
    this.longFilterSpecifications = longFilterSpecifications;
    this.converters = converters;
  }

  public Specification<StudentEntity> getDateTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(ChronoLocalDate.class), dateFilterSpecifications);
  }

  public Specification<StudentEntity> getDateTimeTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(ChronoLocalDateTime.class), dateTimeFilterSpecifications);
  }

  public Specification<StudentEntity> getIntegerTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(Integer.class), integerFilterSpecifications);
  }

  public Specification<StudentEntity> getLongTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(Long.class), longFilterSpecifications);
  }

  public Specification<StudentEntity> getStringTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(String.class), stringFilterSpecifications);
  }

  private <T extends Comparable<T>> Specification<StudentEntity> getSpecification(String fieldName,
                                                                                     String filterValue,
                                                                                     FilterOperation filterOperation,
                                                                                     Function<String, T> converter,
                                                                                     FilterSpecifications<StudentEntity, T> specifications) {
    FilterCriteria<T> criteria = new FilterCriteria<>(fieldName, filterValue, filterOperation, converter);
    return specifications.getSpecification(criteria.getOperation()).apply(criteria);
  }
}