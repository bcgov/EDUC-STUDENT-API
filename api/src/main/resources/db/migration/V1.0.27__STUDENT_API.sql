UPDATE API_STUDENT.STUDENT SET DEMOG_CODE ='A' WHERE DEMOG_CODE IS NULL ;
UPDATE API_STUDENT.STUDENT SET STATUS_CODE ='A' WHERE STATUS_CODE IS NULL ;
UPDATE API_STUDENT.STUDENT_HISTORY SET DEMOG_CODE ='A' WHERE DEMOG_CODE IS NULL ;
UPDATE API_STUDENT.STUDENT_HISTORY SET STATUS_CODE ='A' WHERE STATUS_CODE IS NULL ;
ALTER TABLE API_STUDENT.STUDENT
    MODIFY (
        DEMOG_CODE NOT NULL ,
        STATUS_CODE NOT NULL
        );

ALTER TABLE API_STUDENT.STUDENT_HISTORY
    MODIFY (
        DEMOG_CODE NOT NULL ,
        STATUS_CODE NOT NULL
        );
