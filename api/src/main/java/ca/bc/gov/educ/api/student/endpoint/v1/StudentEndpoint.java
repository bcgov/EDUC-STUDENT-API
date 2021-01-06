package ca.bc.gov.educ.api.student.endpoint.v1;

import ca.bc.gov.educ.api.student.struct.v1.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static ca.bc.gov.educ.api.student.constant.v1.URL.*;
import static org.springframework.http.HttpStatus.CREATED;

@RequestMapping(STUDENT)
@OpenAPIDefinition(info = @Info(title = "API for Student CRU.", description = "This CRU API is related to student data.", version = "1"),
    security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_STUDENT", "WRITE_STUDENT", "READ_STUDENT_HISTORY", "READ_STUDENT_CODES", "DELETE_STUDENT"})})
public interface StudentEndpoint {

  @GetMapping("/{studentID}")
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  Student readStudent(@PathVariable String studentID);

  @GetMapping
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<Student> findStudent(@Param("pen") String pen);

  @PostMapping
  @PreAuthorize("hasAuthority('SCOPE_WRITE_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  @Transactional
  Student createStudent(@Validated @RequestBody StudentCreate student);

  @PutMapping
  @PreAuthorize("hasAuthority('SCOPE_WRITE_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  @Transactional
  Student updateStudent(@Validated @RequestBody StudentUpdate student);

  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_CODES')")
  @GetMapping(SEX_CODES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<SexCode> getSexCodes();

  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_CODES')")
  @GetMapping(STATUS_CODES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<StatusCode> getStatusCodes();

  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_CODES')")
  @GetMapping(DEMOG_CODES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<DemogCode> getDemogCodes();

  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_CODES')")
  @GetMapping(GRADE_CODES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<GradeCode> getGradeCodes();

  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_CODES')")
  @GetMapping(GENDER_CODES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<GenderCode> getGenderCodes();

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('SCOPE_DELETE_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  ResponseEntity<Void> deleteById(@PathVariable UUID id);

  @GetMapping(PAGINATED)
  @Async
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to support data table view in frontend, with sort, filter and pagination.", description = "This API endpoint exposes flexible way to query the entity by leveraging JPA specifications.")
  CompletableFuture<Page<Student>> findAll(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                           @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
                                           @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson);
}
