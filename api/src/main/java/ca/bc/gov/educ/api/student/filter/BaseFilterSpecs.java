package ca.bc.gov.educ.api.student.filter;

import org.springframework.data.jpa.domain.Specification;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;
import java.util.function.Function;

/**
 * this is the generic class to support all kind of filter specifications for different entities
 * @param <R> the entity type.
 * @author Om
 */
public abstract class BaseFilterSpecs<R>{

  private final FilterSpecifications<R, ChronoLocalDate> dateFilterSpecifications;
  private final FilterSpecifications<R, ChronoLocalDateTime<?>> dateTimeFilterSpecifications;
  private final FilterSpecifications<R, Integer> integerFilterSpecifications;
  private final FilterSpecifications<R, String> stringFilterSpecifications;
  private final FilterSpecifications<R, Long> longFilterSpecifications;
  private final FilterSpecifications<R, UUID> uuidFilterSpecifications;
  private final Converters converters;

  protected BaseFilterSpecs(FilterSpecifications<R, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<R, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<R, Integer> integerFilterSpecifications, FilterSpecifications<R, String> stringFilterSpecifications, FilterSpecifications<R, Long> longFilterSpecifications, FilterSpecifications<R, UUID> uuidFilterSpecifications, Converters converters) {
    this.dateFilterSpecifications = dateFilterSpecifications;
    this.dateTimeFilterSpecifications = dateTimeFilterSpecifications;
    this.integerFilterSpecifications = integerFilterSpecifications;
    this.stringFilterSpecifications = stringFilterSpecifications;
    this.longFilterSpecifications = longFilterSpecifications;
    this.uuidFilterSpecifications = uuidFilterSpecifications;
    this.converters = converters;
  }

  public Specification<R> getDateTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(ChronoLocalDate.class), dateFilterSpecifications);
  }

  public Specification<R> getDateTimeTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(ChronoLocalDateTime.class), dateTimeFilterSpecifications);
  }

  public Specification<R> getIntegerTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(Integer.class), integerFilterSpecifications);
  }

  public Specification<R> getLongTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(Long.class), longFilterSpecifications);
  }

  public Specification<R> getStringTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(String.class), stringFilterSpecifications);
  }
  public Specification<R> getUUIDTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(UUID.class), uuidFilterSpecifications);
  }

  private <T extends Comparable<T>> Specification<R> getSpecification(String fieldName,
                                                                                  String filterValue,
                                                                                  FilterOperation filterOperation,
                                                                                  Function<String, T> converter,
                                                                                  FilterSpecifications<R, T> specifications) {
    FilterCriteria<T> criteria = new FilterCriteria<>(fieldName, filterValue, filterOperation, converter);
    return specifications.getSpecification(criteria.getOperation()).apply(criteria);
  }
}
