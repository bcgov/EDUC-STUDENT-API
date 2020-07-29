-- Table Comments
COMMENT ON TABLE FLYWAY_SCHEMA_HISTORY IS 'This table achieves the database migration history used by flwyway.';
COMMENT ON TABLE STUDENT_DEMOG_CODE IS 'Demographic code lists the standard codes for demographic: Accepted, Confirmed, Frozen.';
COMMENT ON TABLE STUDENT_EVENT IS 'This table achieves the events sent to messaging system by EventPoller.';
COMMENT ON TABLE STUDENT_GRADE_CODE IS 'Grade code lists the standard codes for grades: Early Learning, Kindergarten Full, First grade ...';
COMMENT ON TABLE STUDENT_STATUS_CODE IS 'Status code lists the standard codes for status: Active, Deceased.';

-- Column Comments
COMMENT ON COLUMN STUDENT.POSTAL_CODE IS 'The postal code for the student.';
COMMENT ON COLUMN STUDENT.LOCAL_ID IS 'The local identifier for the student. Usually the student number.';
COMMENT ON COLUMN STUDENT.GRADE_CODE IS 'Code identifying the student''s grade level.';
COMMENT ON COLUMN STUDENT.MINCODE IS 'The standard ministry code for the school the student last attended or that last claimed.';
COMMENT ON COLUMN STUDENT.EMAIL_VERIFIED IS 'The email verified flag.';
COMMENT ON COLUMN STUDENT.MEMO IS 'An internal memo field used by staff to record special notes about student records.';

COMMENT ON COLUMN STUDENT_EVENT.EVENT_ID IS 'The unique ID of event.';
COMMENT ON COLUMN STUDENT_EVENT.EVENT_PAYLOAD IS 'The payload of event.';
COMMENT ON COLUMN STUDENT_EVENT.EVENT_STATUS IS 'The status of event: DB_COMMITTED, MESSAGE_PUBLISHED.';
COMMENT ON COLUMN STUDENT_EVENT.EVENT_TYPE IS 'The type of event: GET_STUDENT, CREATE_STUDENT, UPDATE_STUDENT, STUDENT_EVENT_OUTBOX_PROCESSED.';
COMMENT ON COLUMN STUDENT_EVENT.SAGA_ID IS 'The unique ID of saga.';
COMMENT ON COLUMN STUDENT_EVENT.EVENT_OUTCOME IS 'The outcome of processing event: STUDENT_FOUND, STUDENT_NOT_FOUND, STUDENT_CREATED, STUDENT_UPDATED, STUDENT_ALREADY_EXIST.';
COMMENT ON COLUMN STUDENT_EVENT.REPLY_CHANNEL IS 'The topic where the event will be sent.';

COMMENT ON COLUMN STUDENT_SHEDLOCK.NAME IS 'The lock name.';
COMMENT ON COLUMN STUDENT_SHEDLOCK.LOCK_UNTIL IS 'The time when the lock will be released.';
COMMENT ON COLUMN STUDENT_SHEDLOCK.LOCKED_AT IS 'The time when the lock was acquired.';
COMMENT ON COLUMN STUDENT_SHEDLOCK.LOCKED_BY IS 'The component which acquired the lock.';
