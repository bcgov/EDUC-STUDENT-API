package ca.bc.gov.educ.api.student.endpoint.v1;

import ca.bc.gov.educ.api.student.struct.v1.Student;
import ca.bc.gov.educ.api.student.struct.v1.StudentHistory;
import ca.bc.gov.educ.api.student.struct.v1.StudentHistoryActivityCode;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static ca.bc.gov.educ.api.student.constant.v1.URL.*;

/**
 * The interface Student history endpoint.
 */
@RequestMapping(STUDENT)
public interface StudentHistoryEndpoint {

  /**
   * Gets student history activity codes.
   *
   * @return the student history activity codes
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_CODES')")
  @GetMapping(HISTORY_ACTIVITY_CODES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<StudentHistoryActivityCode> getStudentHistoryActivityCodes();

  /**
   * Find student history by student id completable future.
   *
   * @param studentID        the student id
   * @param pageNumber       the page number
   * @param pageSize         the page size
   * @param sortCriteriaJson the sort criteria json
   * @return the completable future
   */
  @GetMapping("/{studentID}" + HISTORY + PAGINATED)
  @Async
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_HISTORY')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to support student history data table view in frontend, with sort, filter and pagination.", description = "This API endpoint exposes flexible way to query the entity by leveraging JPA specifications.")
  CompletableFuture<Page<StudentHistory>> findStudentHistoryByStudentID(@PathVariable String studentID,
                                                                        @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                        @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson);

  /**
   * Find all completable future.
   *
   * @param pageNumber             the page number
   * @param pageSize               the page size
   * @param sortCriteriaJson       the sort criteria json
   * @param searchCriteriaListJson the search criteria list json
   * @return the completable future
   */
  @GetMapping(HISTORY + PAGINATED)
  @Async
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_HISTORY')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to support history data table view in frontend, with sort, filter and pagination.", description = "This API endpoint exposes flexible way to query the audit history entity by leveraging JPA specifications.")
  CompletableFuture<Page<StudentHistory>> findAll(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
                                                  @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson);

  @GetMapping(HISTORY + PAGINATED + DISTINCT + STUDENTS)
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_HISTORY')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to retrieve unique student records with sort, filter and pagination from audit records.", description = "Endpoint to retrieve unique student records with sort, filter and pagination from audit records.")
  Page<Student> findDistinctStudents(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                     @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
                                     @RequestParam(name = "searchCriteriaList") String searchCriteriaListJson);
}
