ALTER TABLE STUDENT_EVENT
MODIFY (
           CREATE_USER VARCHAR2(100),
           UPDATE_USER VARCHAR2(100)
       );

ALTER TABLE STUDENT
MODIFY (
           CREATE_USER VARCHAR2(100),
           UPDATE_USER VARCHAR2(100)
       );

ALTER TABLE STUDENT_HISTORY
MODIFY (
           CREATE_USER VARCHAR2(100),
           UPDATE_USER VARCHAR2(100)
       );
