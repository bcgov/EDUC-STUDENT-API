package ca.bc.gov.educ.api.student.endpoint;

import ca.bc.gov.educ.api.student.struct.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;

@RequestMapping("/")
public interface StudentMergeEndpoint {
  @GetMapping("/{studentID}/merges")
  @PreAuthorize("#oauth2.hasScope('READ_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  List<StudentMerge> findStudentMerges(@PathVariable String studentID, @Param("mergeDirection")  String mergeDirection);

  @PostMapping("/{studentID}/merges")
  @PreAuthorize("#oauth2.hasAnyScope('WRITE_STUDENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  StudentMerge createStudentMerge(@PathVariable String studentID, @Validated @RequestBody StudentMerge studentMerge);

  @PreAuthorize("#oauth2.hasScope('READ_STUDENT_CODES')")
  @GetMapping("student-merge-source-codes")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<StudentMergeSourceCode> getStudentMergeSourceCodes();
}
