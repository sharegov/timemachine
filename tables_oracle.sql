--
-- A hint submitted by a user: Oracle DB MUST be created as "shared" and the 
-- job_queue_processes parameter  must be greater than 2, otherwise a DB lock 
-- will happen.   However, these settings are pretty much standard after any
-- Oracle install, so most users need not worry about this.
--
-- Many other users (including the primary author of Quartz) have had success
-- runing in dedicated mode, so only consider the above as a hint ;-)
--

delete from CIRM_QRTM_FIRED_TRIGGERS;
delete from CIRM_QRTM_SIMPLE_TRIGGERS;
delete from CIRM_QRTM_SIMPROP_TRIGGERS;
delete from CIRM_QRTM_CRON_TRIGGERS;
delete from CIRM_QRTM_BLOB_TRIGGERS;
delete from CIRM_QRTM_TRIGGERS;
delete from CIRM_QRTM_JOB_DETAILS;
delete from CIRM_QRTM_CALENDARS;
delete from CIRM_QRTM_PAUSED_TRIGGER_GRPS;
delete from CIRM_QRTM_LOCKS;
delete from CIRM_QRTM_SCHEDULER_STATE;      

drop table CIRM_QRTM_CALENDARS;
drop table CIRM_QRTM_FIRED_TRIGGERS;
drop table CIRM_QRTM_BLOB_TRIGGERS;
drop table CIRM_QRTM_CRON_TRIGGERS;
drop table CIRM_QRTM_SIMPLE_TRIGGERS;
drop table CIRM_QRTM_SIMPROP_TRIGGERS;
drop table CIRM_QRTM_TRIGGERS;
drop table CIRM_QRTM_JOB_DETAILS;
drop table CIRM_QRTM_PAUSED_TRIGGER_GRPS;
drop table CIRM_QRTM_LOCKS;
drop table CIRM_QRTM_SCHEDULER_STATE;


CREATE TABLE CIRM_QRTM_JOB_DETAILS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    JOB_NAME  VARCHAR2(200) NOT NULL,
    JOB_GROUP VARCHAR2(200) NOT NULL,
    DESCRIPTION VARCHAR2(250) NULL,
    JOB_CLASS_NAME   VARCHAR2(250) NOT NULL, 
    IS_DURABLE VARCHAR2(1) NOT NULL,
    IS_NONCONCURRENT VARCHAR2(1) NOT NULL,
    IS_UPDATE_DATA VARCHAR2(1) NOT NULL,
    REQUESTS_RECOVERY VARCHAR2(1) NOT NULL,
    JOB_DATA BLOB NULL,
    CONSTRAINT CIRMPK_ON_QRTM_JOB_DETAILS PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
);
CREATE TABLE CIRM_QRTM_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    JOB_NAME  VARCHAR2(200) NOT NULL, 
    JOB_GROUP VARCHAR2(200) NOT NULL,
    DESCRIPTION VARCHAR2(250) NULL,
    NEXT_FIRE_TIME NUMBER(13) NULL,
    PREV_FIRE_TIME NUMBER(13) NULL,
    PRIORITY NUMBER(13) NULL,
    TRIGGER_STATE VARCHAR2(16) NOT NULL,
    TRIGGER_TYPE VARCHAR2(8) NOT NULL,
    START_TIME NUMBER(13) NOT NULL,
    END_TIME NUMBER(13) NULL,
    CALENDAR_NAME VARCHAR2(200) NULL,
    MISFIRE_INSTR NUMBER(2) NULL,
    JOB_DATA BLOB NULL,
    CONSTRAINT CIRMPK_ON_QRTM_TRIGGERS PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT CIRMFK_TMTRIGGERS_JOB_DETAILS FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP) 
	REFERENCES CIRM_QRTM_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP) 
);
CREATE TABLE CIRM_QRTM_SIMPLE_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    REPEAT_COUNT NUMBER(7) NOT NULL,
    REPEAT_INTERVAL NUMBER(12) NOT NULL,
    TIMES_TRIGGERED NUMBER(10) NOT NULL,
    CONSTRAINT CIRMPK_ON_QRTM_SIMPLE_TRIGGERS PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT CIRMFK_TMSIMPLETRIGRS_TRIGGERS FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES CIRM_QRTM_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);
CREATE TABLE CIRM_QRTM_CRON_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    CRON_EXPRESSION VARCHAR2(120) NOT NULL,
    TIME_ZONE_ID VARCHAR2(80),
    CONSTRAINT CIRMPK_ON_QRTM_CRON_TRIGGERS PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT CIRMFK_TMCRONTRIGGERS_TRIGGERS FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES CIRM_QRTM_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);
CREATE TABLE CIRM_QRTM_SIMPROP_TRIGGERS
  (          
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 NUMBER(10) NULL,
    INT_PROP_2 NUMBER(10) NULL,
    LONG_PROP_1 NUMBER(13) NULL,
    LONG_PROP_2 NUMBER(13) NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 VARCHAR(1) NULL,
    BOOL_PROP_2 VARCHAR(1) NULL,
    CONSTRAINT CIRMPK_ON_QRTM_SIMPROP_TRGGERS PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT CIRMFK_TMSIMPROPTRIGGERS_TRGRS FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
    REFERENCES CIRM_QRTM_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);
CREATE TABLE CIRM_QRTM_BLOB_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    BLOB_DATA BLOB NULL,
    CONSTRAINT CIRMPK_ON_QRTM_BLOB_TRIGGERS PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT CIRMFK_TMBLOB_TRIGGERS_TRGRS FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
        REFERENCES CIRM_QRTM_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);
CREATE TABLE CIRM_QRTM_CALENDARS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    CALENDAR_NAME  VARCHAR2(200) NOT NULL, 
    CALENDAR BLOB NOT NULL,
    CONSTRAINT CIRMPK_ON_QRTM_CALENDARS PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
);
CREATE TABLE CIRM_QRTM_PAUSED_TRIGGER_GRPS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_GROUP  VARCHAR2(200) NOT NULL, 
    CONSTRAINT CIRMPK_ON_QRTMPAUSEDTRIGGRPS PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
);
CREATE TABLE CIRM_QRTM_FIRED_TRIGGERS 
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    ENTRY_ID VARCHAR2(95) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    INSTANCE_NAME VARCHAR2(200) NOT NULL,
    FIRED_TIME NUMBER(13) NOT NULL,
    PRIORITY NUMBER(13) NOT NULL,
    STATE VARCHAR2(16) NOT NULL,
    JOB_NAME VARCHAR2(200) NULL,
    JOB_GROUP VARCHAR2(200) NULL,
    IS_NONCONCURRENT VARCHAR2(1) NULL,
    REQUESTS_RECOVERY VARCHAR2(1) NULL,
    CONSTRAINT CIRMPK_ON_QRTM_FIRED_TRIGGERS PRIMARY KEY (SCHED_NAME,ENTRY_ID)
);
CREATE TABLE CIRM_QRTM_SCHEDULER_STATE 
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    INSTANCE_NAME VARCHAR2(200) NOT NULL,
    LAST_CHECKIN_TIME NUMBER(13) NOT NULL,
    CHECKIN_INTERVAL NUMBER(13) NOT NULL,
    CONSTRAINT CIRMPK_ON_QRTM_SCHEDULER_STATE PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
);
CREATE TABLE CIRM_QRTM_LOCKS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME  VARCHAR2(40) NOT NULL, 
    CONSTRAINT CIRMPK_ON_QRTM_LOCKS PRIMARY KEY (SCHED_NAME,LOCK_NAME)
);
                
CREATE INDEX CIRMI_QRTM_J_REQ_RECOVERY ON CIRM_QRTM_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);
CREATE INDEX CIRMI_QRTM_J_GRP ON CIRM_QRTM_JOB_DETAILS(SCHED_NAME,JOB_GROUP);

CREATE INDEX CIRMI_QRTM_T_J ON CIRM_QRTM_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX CIRMI_QRTM_T_JG ON CIRM_QRTM_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX CIRMI_QRTM_T_C ON CIRM_QRTM_TRIGGERS(SCHED_NAME,CALENDAR_NAME);
CREATE INDEX CIRMI_QRTM_T_G ON CIRM_QRTM_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);
CREATE INDEX CIRMI_QRTM_T_STATE ON CIRM_QRTM_TRIGGERS(SCHED_NAME,TRIGGER_STATE);
CREATE INDEX CIRMI_QRTM_T_N_STATE ON CIRM_QRTM_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX CIRMI_QRTM_T_N_G_STATE ON CIRM_QRTM_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX CIRMI_QRTM_T_NEXT_FIRE_TIME ON CIRM_QRTM_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);
CREATE INDEX CIRMI_QRTM_T_NFT_ST ON CIRM_QRTM_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
CREATE INDEX CIRMI_QRTM_T_NFT_MISFIRE ON CIRM_QRTM_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
CREATE INDEX CIRMI_QRTM_T_NFT_ST_MISFIRE ON CIRM_QRTM_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
CREATE INDEX CIRMI_QRTM_T_NFT_ST_MISFIRE_GR ON CIRM_QRTM_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);

CREATE INDEX CIRMI_QRTM_FT_TRIG_INST_NAME ON CIRM_QRTM_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);
CREATE INDEX CIRMI_QRTM_FT_INST_JOB_RQ_RCVR ON CIRM_QRTM_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
CREATE INDEX CIRMI_QRTM_FT_J_G ON CIRM_QRTM_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX CIRMI_QRTM_FT_JG ON CIRM_QRTM_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX CIRMI_QRTM_FT_T_G ON CIRM_QRTM_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
CREATE INDEX CIRMI_QRTM_FT_TG ON CIRM_QRTM_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);


commit;
