package ca.bc.gov.educ.api.student.endpoint;

import ca.bc.gov.educ.api.student.struct.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequestMapping("/")
public interface StudentHistoryEndpoint {
  @PreAuthorize("#oauth2.hasScope('READ_STUDENT_CODES')")
  @GetMapping("student-history-activity-codes")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<StudentHistoryActivityCode> getStudentHistoryActivityCodes();

  @GetMapping("/{studentID}/student-history/paginated")
  @Async
  @PreAuthorize("#oauth2.hasScope('READ_STUDENT_HISTORY')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to support student history data table view in frontend, with sort, filter and pagination.", description = "This API endpoint exposes flexible way to query the entity by leveraging JPA specifications.")
  CompletableFuture<Page<StudentHistory>> findStudentHistoryByStudentID(@PathVariable String studentID,
                                                                        @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                        @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson);

  @PreAuthorize("#oauth2.hasScope('READ_STUDENT_HISTORY')")
  @GetMapping("/student-history/name-variant")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND ERROR."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to retrieve a name variant of the current student by audit history using names.", description = "Endpoint to retrieve a name variant of the current student by audit history using names.")
  NameVariant findCurrentStudentNameVariantByAuditHistory(
          @RequestParam(name = "legalFirstName", defaultValue = "") String legalFirstName,
          @RequestParam(name = "legalLastName", defaultValue = "") String legalLastName,
          @RequestParam(name = "legalMiddleNames", defaultValue = "") String legalMiddleNames,
          @RequestParam(name = "usualFirstName", defaultValue = "") String usualFirstName,
          @RequestParam(name = "usualLastName", defaultValue = "") String usualLastName,
          @RequestParam(name = "usualMiddleNames", defaultValue = "") String usualMiddleNames);
}
