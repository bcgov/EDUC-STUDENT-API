package ca.bc.gov.educ.api.student.service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.repository.StudentRepository;

/**
 * StudentService
 *
 * @author John Cox
 */

@Service
public class StudentService {
    private static final String STUDENT_ID_ATTRIBUTE = "studentID";

    @Autowired
    private StudentRepository repository;

    /**
     * Search for StudentEntity by id
     *
     * @param studentID
     * @return
     * @throws EntityNotFoundException
     */
    public StudentEntity retrieveStudent(UUID studentID)  {
        Optional<StudentEntity> result =  repository.findById(studentID);
        if(result.isPresent()) {
            return result.get();
        } else {
            throw new EntityNotFoundException(StudentEntity.class, STUDENT_ID_ATTRIBUTE, studentID.toString());
        }
    }

    /**
     * Creates a StudentEntity
     *
     * @param student
     * @return
     * @throws InvalidParameterException
     */
    public StudentEntity createStudent(StudentEntity student) {

        validateParameters(student);

        if(student.getStudentID()!=null){
            throw new InvalidParameterException(STUDENT_ID_ATTRIBUTE);
        }
        student.setUpdateDate(new Date());
        student.setCreateDate(new Date());

        return repository.save(student);
    }

    /**
     * Updates a StudentEntity
     *
     * @param student
     * @return
     * @throws Exception
     */
    public StudentEntity updateStudent(StudentEntity student) {

        validateParameters(student);


        Optional<StudentEntity> curStudentEntity = repository.findById(student.getStudentID());

        if(curStudentEntity.isPresent())
        {
            StudentEntity newStudentEntity = curStudentEntity.get();
            newStudentEntity.setStudentID(student.getStudentID());
            newStudentEntity.setPen(student.getPen());
            newStudentEntity.setLegalFirstName(student.getLegalFirstName());
            newStudentEntity.setLegalMiddleNames(student.getLegalMiddleNames());
            newStudentEntity.setLegalLastName(student.getLegalLastName());
            newStudentEntity.setDob(student.getDob());
            newStudentEntity.setGenderCode(student.getGenderCode());
            newStudentEntity.setSexCode(student.getSexCode());
            newStudentEntity.setDataSourceCode(student.getDataSourceCode());
            newStudentEntity.setUsualFirstName(student.getUsualFirstName());
            newStudentEntity.setUsualMiddleNames(student.getUsualMiddleNames());
            newStudentEntity.setUsualLastName(student.getUsualLastName());
            newStudentEntity.setEmail(student.getEmail());
            newStudentEntity.setDeceasedDate(student.getDeceasedDate());
            newStudentEntity.setUpdateUser(student.getUpdateUser());
            newStudentEntity.setUpdateDate(new Date());
            newStudentEntity = repository.save(newStudentEntity);

            return newStudentEntity;
        } else {
            throw new EntityNotFoundException(StudentEntity.class, STUDENT_ID_ATTRIBUTE, student.getStudentID().toString());
        }
    }

    private void validateParameters(StudentEntity studentEntity)  {
        if(studentEntity.getCreateDate()!=null)
            throw new InvalidParameterException("createDate");
        if(studentEntity.getUpdateDate()!=null)
            throw new InvalidParameterException("updateDate");
    }
}
