-- Changing column 'VALUE' from VARCHAR(500) to VARCHAR(20000)

alter table MOTECH_TASKS_TASKACTIONINFORMATION_VALUES modify VALUE varchar(20000) NOT NULL;