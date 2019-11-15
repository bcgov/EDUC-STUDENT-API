package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * Student controller
 *
 * @author John Cox
 */

@RestController
@RequestMapping("student")
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableResourceServer
public class StudentController {

    @Autowired
    private StudentService service;

    StudentController(StudentService studentService){
        this.service = studentService;
    }

    @GetMapping("/{studentID}")
    @PreAuthorize("#oauth2.hasScope('READ_STUDENT')")
    public StudentEntity readStudent(@PathVariable Long studentID) throws Exception {
        return service.retrieveStudent(studentID);
    }

    @PostMapping()
    @PreAuthorize("#oauth2.hasAnyScope('WRITE_STUDENT')")
    public StudentEntity createStudent(@Validated @RequestBody StudentEntity student) throws Exception {
        return service.createStudent(student);
    }

    @PutMapping()
    //@PreAuthorize("#oauth2.hasAnyScope('WRITE_STUDENT')")
    public StudentEntity updateStudent(@Validated @RequestBody StudentEntity student) throws Exception {
        return service.updateStudent(student);
    }
}
