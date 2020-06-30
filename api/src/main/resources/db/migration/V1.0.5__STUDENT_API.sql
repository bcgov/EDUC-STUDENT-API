--Modify Columns in STUDENT to lengthen names
ALTER TABLE STUDENT RENAME COLUMN GRADE TO GRADE_CODE;

--Modify Columns in STUDENT to make postal field 6
ALTER TABLE STUDENT MODIFY (
     POSTAL_CODE VARCHAR2(6)
);

--Create students grade code table
CREATE TABLE STUDENT_GRADE_CODE (
  GRADE_CODE VARCHAR2(2) NOT NULL,
  LABEL VARCHAR2(30),
  DESCRIPTION VARCHAR2(255),
  DISPLAY_ORDER NUMBER DEFAULT 1 NOT NULL,
  EFFECTIVE_DATE DATE NOT NULL,
  EXPIRY_DATE DATE NOT NULL,
  CREATE_USER VARCHAR2(32) NOT NULL,
  CREATE_DATE DATE DEFAULT SYSDATE NOT NULL,
  UPDATE_USER VARCHAR2(32) NOT NULL,
  UPDATE_DATE DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT STUDENT_STUDENT_GRADE_CODE_PK PRIMARY KEY (GRADE_CODE)
);

--Constraints
ALTER TABLE STUDENT ADD CONSTRAINT STUDENT_STUDENT_GRADE_CODE_FK FOREIGN KEY (GRADE_CODE) REFERENCES STUDENT_GRADE_CODE (GRADE_CODE);

-- Student grade codes
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('01','Grade 1','First grade.',1,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('02','Grade 2','Second grade.',2,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('03','Grade 3','Third grade.',3,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('04','Grade 4','Fourth grade.',4,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('05','Grade 5','Fifth grade.',5,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('06','Grade 6','Sixth grade.',6,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('07','Grade 7','Seventh grade.',7,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('08','Grade 8','Eighth grade.',8,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('09','Grade 9','Ninth grade.',9,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('10','Grade 10','Tenth grade.',10,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('11','Grade 11','Eleventh grade.',11,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('12','Grade 12','Twelfth grade.',12,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('EL','Early Learning','Early learning.',13,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('GA','Graduated Adult','Graduated adult.',14,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('KF','Kindergarten Full','Kindergarten full-time.',15,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('SU','Secondary Upgraded','Secondary upgraded.',16,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('EU','Elementary Upgraded','Elementary upgraded.',17,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('HS','Home schooled','Home schooled.',18,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
INSERT INTO STUDENT_GRADE_CODE (GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('KH','Kindergarten Half','Kindergarten half-time.',19,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));
