package ca.bc.gov.educ.api.student.filter;

import ca.bc.gov.educ.api.student.model.StudentHistoryEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class StudentHistoryFilterSpecs {

  private final FilterSpecifications<StudentHistoryEntity, ChronoLocalDate> dateFilterSpecifications;
  private final FilterSpecifications<StudentHistoryEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications;
  private final FilterSpecifications<StudentHistoryEntity, Integer> integerFilterSpecifications;
  private final FilterSpecifications<StudentHistoryEntity, String> stringFilterSpecifications;
  private final FilterSpecifications<StudentHistoryEntity, Long> longFilterSpecifications;
  private final FilterSpecifications<StudentHistoryEntity, UUID> uuidFilterSpecifications;
  private final Converters converters;

  public StudentHistoryFilterSpecs(FilterSpecifications<StudentHistoryEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<StudentHistoryEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<StudentHistoryEntity, Integer> integerFilterSpecifications, FilterSpecifications<StudentHistoryEntity, String> stringFilterSpecifications, FilterSpecifications<StudentHistoryEntity, Long> longFilterSpecifications, FilterSpecifications<StudentHistoryEntity, UUID> uuidFilterSpecifications, Converters converters) {
    this.dateFilterSpecifications = dateFilterSpecifications;
    this.dateTimeFilterSpecifications = dateTimeFilterSpecifications;
    this.integerFilterSpecifications = integerFilterSpecifications;
    this.stringFilterSpecifications = stringFilterSpecifications;
    this.longFilterSpecifications = longFilterSpecifications;
    this.uuidFilterSpecifications = uuidFilterSpecifications;
    this.converters = converters;
  }

  public Specification<StudentHistoryEntity> getDateTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(ChronoLocalDate.class), dateFilterSpecifications);
  }

  public Specification<StudentHistoryEntity> getDateTimeTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(ChronoLocalDateTime.class), dateTimeFilterSpecifications);
  }

  public Specification<StudentHistoryEntity> getIntegerTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(Integer.class), integerFilterSpecifications);
  }

  public Specification<StudentHistoryEntity> getLongTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(Long.class), longFilterSpecifications);
  }

  public Specification<StudentHistoryEntity> getStringTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(String.class), stringFilterSpecifications);
  }
  public Specification<StudentHistoryEntity> getUUIDTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(UUID.class), uuidFilterSpecifications);
  }

  private <T extends Comparable<T>> Specification<StudentHistoryEntity> getSpecification(String fieldName,
                                                                                     String filterValue,
                                                                                     FilterOperation filterOperation,
                                                                                     Function<String, T> converter,
                                                                                     FilterSpecifications<StudentHistoryEntity, T> specifications) {
    FilterCriteria<T> criteria = new FilterCriteria<>(fieldName, filterValue, filterOperation, converter);
    return specifications.getSpecification(criteria.getOperation()).apply(criteria);
  }
}
