package ca.bc.gov.educ.api.student.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "STUDENT_TWIN")
@Data
@DynamicUpdate
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentTwinEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "STUDENT_TWIN_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID studentTwinID;

  //To keep the code simple, we didn't use the @ManyToOne association here
  @NotNull(message = "studentID cannot be null")
  @Column(name = "STUDENT_ID")
  UUID studentID;

  @NotNull(message = "studentTwinReasonCode cannot be null")
  @Column(name = "STUDENT_TWIN_REASON_CODE")
  String studentTwinReasonCode;

  @Column(name = "CREATE_USER", updatable = false)
  String createUser;

  @Column(name = "CREATE_DATE", updatable = false)
  @PastOrPresent
  LocalDateTime createDate;

  @Column(name = "UPDATE_USER")
  String updateUser;

  @Column(name = "UPDATE_DATE")
  @PastOrPresent
  LocalDateTime updateDate;

  @NotNull(message = "twinStudent cannot be null")
  @ManyToOne(optional = false, targetEntity = StudentEntity.class)
  @JoinColumn(name = "TWIN_STUDENT_ID", referencedColumnName = "STUDENT_ID", updatable = false)
  StudentEntity twinStudent;
}
