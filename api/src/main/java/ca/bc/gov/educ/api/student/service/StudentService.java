package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.props.ApplicationProperties;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * StudentService
 *
 * @author John Cox
 */

@Service
public class StudentService {
    private static final Log logger = LogFactory.getLog(StudentService.class);

    @Autowired
    private StudentRepository repository;

    /**
     * Search for StudentEntity by id
     *
     * @param studentID
     * @return
     * @throws EntityNotFoundException
     */
    public StudentEntity retrieveStudent(UUID studentID) throws EntityNotFoundException {
        Optional<StudentEntity> result =  repository.findById(studentID);
        if(result.isPresent()) {
            return result.get();
        } else {
            throw new EntityNotFoundException(StudentEntity.class, "studentID", studentID.toString());
        }
    }

    /**
     * Creates a StudentEntity
     *
     * @param student
     * @return
     * @throws InvalidParameterException
     */
    public StudentEntity createStudent(StudentEntity student) throws InvalidParameterException {

        validateParameters(student);

        if(student.getStudentID()!=null){
            throw new InvalidParameterException("studentID");
        }
        student.setUpdateUser(ApplicationProperties.CLIENT_ID);
        student.setUpdateDate(new Date());
        student.setCreateUser(ApplicationProperties.CLIENT_ID);
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
    public StudentEntity updateStudent(StudentEntity student) throws EntityNotFoundException, InvalidParameterException {

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
            newStudentEntity.setUpdateUser(ApplicationProperties.CLIENT_ID);
            newStudentEntity.setUpdateDate(new Date());
            newStudentEntity = repository.save(newStudentEntity);

            return newStudentEntity;
        } else {
            throw new EntityNotFoundException(StudentEntity.class, "studentID", student.getStudentID().toString());
        }
    }

    private void validateParameters(StudentEntity studentEntity) throws InvalidParameterException {

        if(studentEntity.getCreateDate()!=null)
            throw new InvalidParameterException("createDate");
        if(studentEntity.getCreateUser()!=null)
            throw new InvalidParameterException("createUser");
        if(studentEntity.getUpdateDate()!=null)
            throw new InvalidParameterException("updateDate");
        if(studentEntity.getUpdateUser()!=null)
            throw new InvalidParameterException("updateUser");
    }
}
