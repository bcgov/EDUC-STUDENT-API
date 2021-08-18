package ca.bc.gov.educ.api.student.repository.v1.impl;

import ca.bc.gov.educ.api.student.filter.FilterOperation;
import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryEntity;
import ca.bc.gov.educ.api.student.repository.v1.StudentHistoryRepositoryCustom;
import ca.bc.gov.educ.api.student.struct.v1.Search;
import ca.bc.gov.educ.api.student.struct.v1.SearchCriteria;
import ca.bc.gov.educ.api.student.struct.v1.ValueType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * This is special implementation to get paged unique students by searching the audit history of student.
 * this is a custom implementation of building where clause and order by based on the sorts and search criteria provided.
 *
 * @author om
 */
@Repository
@Slf4j
@SuppressWarnings("java:S2077")
public class StudentHistoryRepositoryCustomImpl implements StudentHistoryRepositoryCustom {
  private static final String START_QUERY = "SELECT * FROM (SELECT ROW_.*, ROWNUM ROWNUM_   FROM (SELECT DISTINCT (B.STUDENT_ID) AS STUDENT_DISTINCT_ID, A.* FROM STUDENT_HISTORY B INNER JOIN STUDENT A ON B.STUDENT_ID = A.STUDENT_ID WHERE ";
  private static final String END_QUERY = " ) ROW_   WHERE ROWNUM <=%d) WHERE ROWNUM_ > %d";
  private static final String START_COUNT_QUERY = "SELECT COUNT(STUDENT_DISTINCT_ID) FROM (SELECT DISTINCT (B.STUDENT_ID) AS STUDENT_DISTINCT_ID FROM STUDENT_HISTORY B INNER JOIN STUDENT A ON B.STUDENT_ID = A.STUDENT_ID WHERE ";
  private static final String END_COUNT_QUERY = ")";
  private final EntityManager entityManager;

  private final Map<String, String> entityColumnMap = new HashMap<>();
  private final Map<String, String> symbolMap = new HashMap<>();

  /**
   * Instantiates a new Student history repository custom.
   *
   * @param entityManager the entity manager
   */
  @Autowired
  public StudentHistoryRepositoryCustomImpl(final EntityManager entityManager) {
    this.entityManager = entityManager;
    final Field[] fields = StudentHistoryEntity.class.getDeclaredFields();
    for (final Field field : fields) {
      if (field.getAnnotation(Column.class) != null) {
        this.entityColumnMap.put(field.getName(), field.getAnnotation(Column.class).name());
      }
    }
    this.symbolMap.put("eq", "=");
    this.symbolMap.put("neq", "<>");
    this.symbolMap.put("in", "IN");
    this.symbolMap.put("nin", "NOT IN");
    this.symbolMap.put("btn", "BETWEEN");
    this.symbolMap.put("like", "LIKE");// %value%
    this.symbolMap.put("starts_with", "LIKE"); // value%
  }


  /**
   * @param sortMap    the map containing the sort
   * @param searches   the search criteria.
   * @param pageNumber page number
   * @param pageSize   page size
   * @return the paged student entity.
   */
  @Override
  public Page<StudentEntity> findDistinctStudentsByStudentHistoryCriteria(@Nullable final Map<String, String> sortMap, @NonNull final List<Search> searches, final int pageNumber, final int pageSize) {
    String orderBy = null;
    final List<Sort.Order> sorts = new ArrayList<>();
    if (sortMap != null && !sortMap.isEmpty()) {
      sortMap.forEach((k, v) -> {
        if ("ASC".equalsIgnoreCase(v)) {
          sorts.add(new Sort.Order(Sort.Direction.ASC, k));
        } else {
          sorts.add(new Sort.Order(Sort.Direction.DESC, k));
        }
      });
    }
    final Map<String, Object> parameterMap = new HashMap<>();
    final String whereClause = this.buildWhereClauseBasedOnSearchCriteria(searches, parameterMap);
    if (sortMap != null && !sortMap.isEmpty()) {
      orderBy = this.buildOrderBy(sortMap);
    }

    final int rowNumLessThanEqualTo = (pageNumber * pageSize) + pageSize;
    final int rowNumGreaterThan = (pageNumber * pageSize);
    final String studentsQuery = START_QUERY + whereClause + (orderBy != null ? orderBy : "") + String.format(END_QUERY, rowNumLessThanEqualTo, rowNumGreaterThan);
    final String countQueryString = START_COUNT_QUERY + whereClause + (orderBy != null ? orderBy : "") + END_COUNT_QUERY;

    final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));

    log.info("query is :: {}", studentsQuery);
    log.info("count query is :: {}", countQueryString);
    log.info(parameterMap.toString());
    final Query q = this.entityManager.createNativeQuery(studentsQuery, StudentEntity.class);
    parameterMap.forEach(q::setParameter);
    final List<StudentEntity> studentEntities = q.getResultList();

    final Query countQuery = this.entityManager.createNativeQuery(countQueryString);
    parameterMap.forEach(countQuery::setParameter);
    final BigDecimal total = (BigDecimal) countQuery.getSingleResult();
    return new PageImpl<>(studentEntities, pageable, total.longValue());
  }

  private String buildOrderBy(final Map<String, String> sortMap) {
    final StringBuilder orderByBuilder = new StringBuilder();
    orderByBuilder.append(" ORDER BY ");
    val totalItemsInSet = sortMap.keySet().size();
    int index = 0;
    for (val entry : sortMap.entrySet()) {
      val value = entry.getValue();
      if ("ASC".equalsIgnoreCase(value)) {
        orderByBuilder.append("A.").append(this.entityColumnMap.get(entry.getKey())).append(" ASC ");
      } else {
        orderByBuilder.append("A.").append(this.entityColumnMap.get(entry.getKey())).append(" DESC ");
      }
      index++;
      if (index != totalItemsInSet) {
        orderByBuilder.append(",");
      }
    }
    return orderByBuilder.toString();
  }

  private String buildWhereClauseBasedOnSearchCriteria(final List<Search> searches, final Map<String, Object> parameterMap) {
    final StringBuilder whereClause = new StringBuilder();
    int index = 0;
    for (val search : searches) {
      if (index != 0 && search.getCondition() != null) {
        whereClause.append(" ").append(search.getCondition().toString()).append(" ");
      }
      val searchCriterias = search.getSearchCriteriaList();
      whereClause.append("( ");
      this.buildInnerWhereClauseAndCreateParamMap(parameterMap, whereClause, searchCriterias);
      whereClause.append(") ");
      index++;
    }
    return whereClause.toString();
  }

  private void buildInnerWhereClauseAndCreateParamMap(final Map<String, Object> parameterMap, final StringBuilder whereClause, final List<SearchCriteria> searchCriterias) {
    int innerIndex = 0;
    for (val innerSearch : searchCriterias) {
      whereClause.append("B.").append(this.entityColumnMap.get(innerSearch.getKey())).append(" ").append(this.symbolMap.get(innerSearch.getOperation().toString())).append(" ");
      if (innerSearch.getOperation().equals(FilterOperation.BETWEEN)) {
        val values = innerSearch.getValue().split(",");
        parameterMap.put("min" + innerSearch.getKey(), this.getConvertedValue(values[0], innerSearch.getValueType()) );
        parameterMap.put("max" + innerSearch.getKey(), this.getConvertedValue(values[1], innerSearch.getValueType()));
        whereClause.append(":min").append(innerSearch.getKey()).append(" AND ").append(":max").append(innerSearch.getKey());
      } else if (innerSearch.getOperation().equals(FilterOperation.STARTS_WITH)) {
        parameterMap.put(innerSearch.getKey(), this.getConvertedValue(innerSearch.getValue(), innerSearch.getValueType()) + "%");
        whereClause.append(":").append(innerSearch.getKey());
      } else if (innerSearch.getOperation().equals(FilterOperation.CONTAINS)) {
        parameterMap.put(innerSearch.getKey(), "%" + this.getConvertedValue(innerSearch.getValue(), innerSearch.getValueType()) + "%");
        whereClause.append(":").append(innerSearch.getKey());
      } else {
        parameterMap.put(innerSearch.getKey(), this.getConvertedValue(innerSearch.getValue(), innerSearch.getValueType()));
        whereClause.append(":").append(innerSearch.getKey());
      }
      innerIndex++;
      if (innerIndex != searchCriterias.size()) {
        whereClause.append(" ").append(innerSearch.getCondition().toString()).append(" ");
      }
    }
  }
  private Object getConvertedValue(final String value, final ValueType valueType){
    switch (valueType){
      case DATE:
        return LocalDate.parse(value);
      case LONG:
        return Long.valueOf(value);
      case UUID:
        return UUID.fromString(value);
      case INTEGER:
        return Integer.valueOf(value);
      case DATE_TIME:
        return LocalDateTime.parse(value);
      default:
        return value;
    }
  }
}
