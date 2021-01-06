package ca.bc.gov.educ.api.student.endpoint.v1;

import ca.bc.gov.educ.api.student.struct.v1.StudentTwin;
import ca.bc.gov.educ.api.student.struct.v1.StudentTwinReasonCode;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.student.constant.v1.URL.*;
import static org.springframework.http.HttpStatus.CREATED;

@RequestMapping(STUDENT)
public interface StudentTwinEndpoint {
  @GetMapping("/{studentID}" + TWINS)
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  List<StudentTwin> findStudentTwins(@PathVariable String studentID);

  @PostMapping("/{studentID}" + TWINS)
  @PreAuthorize("hasAuthority('SCOPE_WRITE_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  StudentTwin createStudentTwin(@PathVariable String studentID, @Validated @RequestBody StudentTwin studentTwin);

  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_CODES')")
  @GetMapping(TWIN_REASON_CODES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<StudentTwinReasonCode> getStudentTwinReasonCodes();

  @DeleteMapping("/{studentID}" + TWINS + "/{studentTwinID}")
  @PreAuthorize("hasAuthority('SCOPE_DELETE_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  ResponseEntity<Void> deleteById(@PathVariable UUID studentID, @PathVariable UUID studentTwinID);
}
