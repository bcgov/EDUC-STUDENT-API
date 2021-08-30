CREATE TABLE DOCUMENT_TYPE_CODE
(
    DOCUMENT_TYPE_CODE VARCHAR2(10)           NOT NULL,
    LABEL              VARCHAR2(30),
    DESCRIPTION        VARCHAR2(255),
    DISPLAY_ORDER      NUMBER DEFAULT 1       NOT NULL,
    EFFECTIVE_DATE     DATE                   NOT NULL,
    EXPIRY_DATE        DATE                   NOT NULL,
    CREATE_USER        VARCHAR2(32)           NOT NULL,
    CREATE_DATE        DATE   DEFAULT SYSDATE NOT NULL,
    UPDATE_USER        VARCHAR2(32)           NOT NULL,
    UPDATE_DATE        DATE   DEFAULT SYSDATE NOT NULL,
    CONSTRAINT DOCUMENT_TYPE_CODE_PK PRIMARY KEY (DOCUMENT_TYPE_CODE)
);
COMMENT ON TABLE DOCUMENT_TYPE_CODE IS 'Document Type Code lists the semantic types of documents that are supported. Examples include Birth Certificate (image of), Passport image, Permanent Resident Card image, etc.';



INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CABIRTH', 'Canadian Birth Certificate', 'Canadian Birth Certificate', 10,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CAPASSPORT', 'Canadian Passport', 'Canadian Passport', 20,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CADL', 'Canadian Driver''s Licence', 'Canadian Driver''s Licence', 30,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('BCIDCARD', 'Provincial Identification Card', 'Provincial Identification Card', 40,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('BCSCPHOTO', 'BC Services Card w Photo', 'BC Services Card (Photo version only)', 50,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CACITZCARD', 'Canadian Citizenship Card', 'Canadian Citizenship Card', 60,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('PRCARD', 'Permanent Residence Card', 'Permanent Residence Card', 70,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('STUDENTPMT', 'Student / Study Permit', 'Student / Study Permit', 80,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('IMM5292', 'IMM5292 Conf of Perm Residence', 'Confirmation of Permanent Residence (IMM5292)', 90,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('IMM1000', 'IMM1000 Record of Landing',
        'Canadian Immigration Record of Landing (IMM 1000, not valid after June 2002)', 100,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('INDSTATUS', 'Indian Status Card', 'Indian Status Card', 110,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('NAMECHANGE', 'Legal Name Change document', 'Canadian court order approving legal change of name', 120,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('FORPASSPRT', 'Foreign Passport', 'Foreign Passport', 130,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('ADOPTION', 'Canadian adoption order', 'Canadian adoption order', 140,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('MARRIAGE', 'Marriage Certificate', 'Marriage Certificate', 150,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('FORBIRTH', 'Foreign Birth Certificate', 'Foreign Birth Certificate (with English translation)', 160,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('OTHER', 'Other', 'Other document type', 170, to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2299-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'STUDENT-API',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

--Add Columns to STUDENT
ALTER TABLE STUDENT ADD (
    DOCUMENT_TYPE_CODE VARCHAR2(10),
    DATE_OF_CONFIRMATION TIMESTAMP
    );

COMMENT ON COLUMN STUDENT.DOCUMENT_TYPE_CODE IS 'The Document type that is associated with the student for confirmed demog code.';
COMMENT ON COLUMN STUDENT.DATE_OF_CONFIRMATION IS 'The Date on which the Document type that was associated with the student for confirmed demog code.';

--Add Columns to STUDENT_HISTORY
ALTER TABLE STUDENT_HISTORY ADD (
    DOCUMENT_TYPE_CODE VARCHAR2(10),
    DATE_OF_CONFIRMATION TIMESTAMP
    );

COMMENT ON COLUMN STUDENT_HISTORY.DOCUMENT_TYPE_CODE IS 'The Document type that is associated with the student for confirmed demog code.';
COMMENT ON COLUMN STUDENT_HISTORY.DATE_OF_CONFIRMATION IS 'The Date on which the Document type that was associated with the student for confirmed demog code.';

ALTER TABLE STUDENT
    ADD CONSTRAINT STUDENT_DOCUMENT_TYPE_CODE_FK FOREIGN KEY (DOCUMENT_TYPE_CODE) REFERENCES DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE);

ALTER TABLE STUDENT_HISTORY
    ADD CONSTRAINT STUDENT_HISTORY_DOCUMENT_TYPE_CODE_FK FOREIGN KEY (DOCUMENT_TYPE_CODE) REFERENCES DOCUMENT_TYPE_CODE (DOCUMENT_TYPE_CODE);
