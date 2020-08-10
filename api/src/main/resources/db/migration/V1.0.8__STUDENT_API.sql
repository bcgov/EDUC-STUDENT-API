--A	Active	Active	1
--D	Deceased	Student is Deceased	2
--M	Merged	Student record is a duplicate and data was merged to another record	3
--X	Deleted	Student record was deleted	4


UPDATE STUDENT_STATUS_CODE
SET    STATUS_CODE = 'D'
WHERE  STATUS_CODE = 'M';

UPDATE STUDENT_STATUS_CODE
SET    DISPLAY_ORDER=4
WHERE  STATUS_CODE = 'X';

INSERT INTO STUDENT_STATUS_CODE (STATUS_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE) VALUES ('M','Merged','Student record is a duplicate and data was merged to another record',3,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'),'IDIR/MVILLENE',to_date('2019-11-07','YYYY-MM-DD'));