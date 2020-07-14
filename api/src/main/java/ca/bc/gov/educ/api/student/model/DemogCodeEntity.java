package ca.bc.gov.educ.api.student.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "STUDENT_DEMOG_CODE")
public class DemogCodeEntity {

    @Id
    @Column(name = "DEMOG_CODE", unique = true, updatable = false)
    String demogCode;
    
    @NotNull(message="label cannot be null")
    @Column(name = "LABEL")
    String label;
    
    @NotNull(message="description cannot be null")
    @Column(name = "DESCRIPTION")
    String description;

    @NotNull(message="displayOrder cannot be null")
    @Column(name = "DISPLAY_ORDER")
    Integer displayOrder;

    @NotNull(message="effectiveDate cannot be null")
    @Column(name = "EFFECTIVE_DATE")
    LocalDateTime effectiveDate;
    
    @NotNull(message="expiryDate cannot be null")
    @Column(name = "EXPIRY_DATE")
    LocalDateTime expiryDate;

    @Column(name = "CREATE_USER", updatable = false)
    String createUser;

    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    LocalDateTime createDate;

    @Column(name = "UPDATE_USER", updatable = false)
    String updateUser;

    @PastOrPresent
    @Column(name = "UPDATE_DATE", updatable = false)
    LocalDateTime updateDate;
}