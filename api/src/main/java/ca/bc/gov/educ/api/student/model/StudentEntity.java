package ca.bc.gov.educ.api.student.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "student")
public class StudentEntity {
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
			@Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy") })
	@Column(name = "student_id", unique = true, updatable = false, columnDefinition = "BINARY(16)")
	UUID studentID;
	@NotNull(message = "pen cannot be null")
	@Column(name = "pen")
	String pen;
	@Column(name = "legal_first_name")
	String legalFirstName;
	@Column(name = "legal_middle_names")
	String legalMiddleNames;
	@Column(name = "legal_last_name")
	String legalLastName;
	@Column(name = "dob")
	@PastOrPresent
	Date dob;
	@Column(name = "sex_code")
	char sexCode;
	@Column(name = "gender_code")
	char genderCode;
	@Column(name = "data_source_code")
	String dataSourceCode;
	@Column(name = "usual_first_name")
	String usualFirstName;
	@Column(name = "usual_middle_names")
	String usualMiddleNames;
	@Column(name = "usual_last_name")
	String usualLastName;
	@Email(message = "Email must be valid email address")
	@Column(name = "email")
	String email;
	@Column(name = "deceased_date")
	@PastOrPresent
	Date deceasedDate;
	@Column(name = "create_user")
	String createUser;
	@Column(name = "create_date")
	@PastOrPresent
	Date createDate;
	@Column(name = "update_user")
	String updateUser;
	@Column(name = "update_date")
	@PastOrPresent
	Date updateDate;

	public UUID getStudentID() {
		return studentID;
	}

	public void setStudentID(UUID studentID) {
		this.studentID = studentID;
	}

	public String getPen() {
		return pen;
	}

	public void setPen(String pen) {
		this.pen = pen;
	}

	public String getLegalFirstName() {
		return legalFirstName;
	}

	public void setLegalFirstName(String legalFirstName) {
		this.legalFirstName = legalFirstName;
	}

	public String getLegalMiddleNames() {
		return legalMiddleNames;
	}

	public void setLegalMiddleNames(String legalMiddleNames) {
		this.legalMiddleNames = legalMiddleNames;
	}

	public String getLegalLastName() {
		return legalLastName;
	}

	public void setLegalLastName(String legalLastName) {
		this.legalLastName = legalLastName;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public char getSexCode() {
		return sexCode;
	}

	public void setSexCode(char sexCode) {
		this.sexCode = sexCode;
	}

	public char getGenderCode() {
		return genderCode;
	}

	public void setGenderCode(char genderCode) {
		this.genderCode = genderCode;
	}

	public String getDataSourceCode() {
		return dataSourceCode;
	}

	public void setDataSourceCode(String dataSourceCode) {
		this.dataSourceCode = dataSourceCode;
	}

	public String getUsualFirstName() {
		return usualFirstName;
	}

	public void setUsualFirstName(String usualFirstName) {
		this.usualFirstName = usualFirstName;
	}

	public String getUsualMiddleNames() {
		return usualMiddleNames;
	}

	public void setUsualMiddleNames(String usualMiddleNames) {
		this.usualMiddleNames = usualMiddleNames;
	}

	public String getUsualLastName() {
		return usualLastName;
	}

	public void setUsualLastName(String usualLastName) {
		this.usualLastName = usualLastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getDeceasedDate() {
		return deceasedDate;
	}

	public void setDeceasedDate(Date deceasedDate) {
		this.deceasedDate = deceasedDate;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

}
