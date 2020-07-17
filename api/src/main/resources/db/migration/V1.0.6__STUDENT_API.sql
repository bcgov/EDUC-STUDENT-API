-- Student status codes
UPDATE STUDENT_STATUS_CODE
SET    DISPLAY_ORDER = 1
WHERE  STATUS_CODE = 'A';

UPDATE STUDENT_STATUS_CODE
SET    DISPLAY_ORDER = 2
WHERE  STATUS_CODE = 'M';

INSERT INTO STUDENT_STATUS_CODE (STATUS_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('X','Deleted','Student record was deleted',3,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));