package ca.bc.gov.educ.api.student.endpoint;

import ca.bc.gov.educ.api.student.struct.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;
import java.util.UUID;

@RequestMapping("/")
@OpenAPIDefinition(info = @Info(title = "API for Student Twin CRU.", description = "This CRU API is related to student twin data.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_STUDENT", "WRITE_STUDENT", "READ_STUDENT_CODES"})})
public interface StudentTwinEndpoint {
  @GetMapping("/{studentID}/twins")
  @PreAuthorize("#oauth2.hasScope('READ_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  List<StudentTwin> findStudentTwins(@PathVariable String studentID);

  @PostMapping("/{studentID}/twins")
  @PreAuthorize("#oauth2.hasAnyScope('WRITE_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  StudentTwin createStudentTwin(@PathVariable String studentID, @Validated @RequestBody StudentTwin studentTwin);

  @PreAuthorize("#oauth2.hasScope('READ_STUDENT_CODES')")
  @GetMapping("student-twin-reason-codes")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<StudentTwinReasonCode> getStudentTwinReasonCodes();

  @DeleteMapping("/{studentID}/twins/{studentTwinID}")
  @PreAuthorize("#oauth2.hasScope('DELETE_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"),  @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  ResponseEntity<Void> deleteById(@PathVariable UUID studentID, @PathVariable UUID studentTwinID);
}
