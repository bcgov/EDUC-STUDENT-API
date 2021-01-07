package ca.bc.gov.educ.api.student.model.v1;

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
@Table(name = "STUDENT_MERGE")
@Data
@DynamicUpdate
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentMergeEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "STUDENT_MERGE_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID studentMergeID;

  //To keep the code simple, we didn't use the @ManyToOne association here
  @NotNull(message = "studentID cannot be null")
  @Column(name = "STUDENT_ID")
  UUID studentID;

  @NotNull(message = "studentMergeDirectionCode cannot be null")
  @Column(name = "STUDENT_MERGE_DIRECTION_CODE")
  String studentMergeDirectionCode;

  @NotNull(message = "studentMergeSourceCode cannot be null")
  @Column(name = "STUDENT_MERGE_SOURCE_CODE")
  String studentMergeSourceCode;

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

  @NotNull(message = "mergeStudent cannot be null")
  @ManyToOne(optional = false, targetEntity = StudentEntity.class)
  @JoinColumn(name = "MERGE_STUDENT_ID", referencedColumnName = "STUDENT_ID", updatable = false)
  StudentEntity mergeStudent;
}
