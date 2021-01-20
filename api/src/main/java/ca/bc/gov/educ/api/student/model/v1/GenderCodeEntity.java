package ca.bc.gov.educ.api.student.model.v1;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "STUDENT_GENDER_CODE")
public class GenderCodeEntity {

	@Id
	@Column(name = "GENDER_CODE", unique = true, updatable = false)
	String genderCode;

	@NotNull(message = "label cannot be null")
	@Column(name = "label")
	String label;

	@NotNull(message = "description cannot be null")
	@Column(name = "DESCRIPTION")
	String description;

	@NotNull(message = "displayOrder cannot be null")
	@Column(name = "DISPLAY_ORDER")
	Integer displayOrder;

	@NotNull(message = "effectiveDate cannot be null")
	@Column(name = "EFFECTIVE_DATE")
	LocalDateTime effectiveDate;

	@NotNull(message = "expiryDate cannot be null")
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