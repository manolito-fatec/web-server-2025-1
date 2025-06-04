-- Create tables in the dw_dashflow schema (assuming user already exists)

-- ISSUE_STATUS table with SCD2 handling
CREATE TABLE dw_dashflow.issue_status (
                                          status_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                          seq NUMBER NOT NULL,
                                          original_id VARCHAR2(255) NOT NULL,
                                          project_id NUMBER NOT NULL,
                                          status_name VARCHAR2(255) NOT NULL,
                                          description CLOB,
                                          start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                          end_date DATE,
                                          is_current NUMBER(1) DEFAULT 1 NOT NULL,

                                          CONSTRAINT fk_issue_status_projects FOREIGN KEY (project_id) REFERENCES dw_dashflow.projects(project_id)
);

CREATE OR REPLACE TRIGGER issue_status_scd2_trigger
              BEFORE INSERT ON dw_dashflow.issue_status
                         FOR EACH ROW
                         DECLARE
                         max_seq NUMBER;
BEGIN
    -- Skip SCD2 processing for special records
    IF :NEW.original_id = '0' THEN
        :NEW.seq := 1;
:NEW.start_date := TRUNC(SYSDATE);
:NEW.end_date := NULL;
:NEW.is_current := 1;
RETURN;
END IF;

    -- Get the maximum sequence number
SELECT NVL(MAX(seq), 0) INTO max_seq
FROM dw_dashflow.issue_status
WHERE original_id = :NEW.original_id AND project_id = :NEW.project_id;

:NEW.seq := max_seq + 1;

    -- Update previous version if exists
IF max_seq > 0 THEN
UPDATE dw_dashflow.issue_status
SET end_date = TRUNC(SYSDATE), is_current = 0
WHERE original_id = :NEW.original_id
  AND project_id = :NEW.project_id
  AND is_current = 1;
END IF;

:NEW.start_date := TRUNC(SYSDATE);
:NEW.end_date := NULL;
:NEW.is_current := 1;
END;
/

CREATE UNIQUE INDEX uk_issue_status_seq ON dw_dashflow.issue_status(original_id, seq, project_id);

-- ISSUE_TYPE table with SCD2 handling
CREATE TABLE dw_dashflow.issue_type (
                                        type_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        seq NUMBER NOT NULL,
                                        original_id VARCHAR2(255) NOT NULL,
                                        project_id NUMBER NOT NULL,
                                        type_name VARCHAR2(255) NOT NULL,
                                        start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                        end_date DATE,
                                        is_current NUMBER(1) DEFAULT 1 NOT NULL,

                                        CONSTRAINT fk_issue_type_projects FOREIGN KEY (project_id) REFERENCES dw_dashflow.projects(project_id)
);

CREATE OR REPLACE TRIGGER issue_type_scd2_trigger
              BEFORE INSERT ON dw_dashflow.issue_type
                         FOR EACH ROW
                         DECLARE
                         max_seq NUMBER;
BEGIN
    IF :NEW.original_id = '0' THEN
        :NEW.seq := 1;
:NEW.start_date := TRUNC(SYSDATE);
:NEW.end_date := NULL;
:NEW.is_current := 1;
RETURN;
END IF;

SELECT NVL(MAX(seq), 0) INTO max_seq
FROM dw_dashflow.issue_type
WHERE original_id = :NEW.original_id AND project_id = :NEW.project_id;

:NEW.seq := max_seq + 1;

IF max_seq > 0 THEN
UPDATE dw_dashflow.issue_type
SET end_date = TRUNC(SYSDATE), is_current = 0
WHERE original_id = :NEW.original_id
  AND project_id = :NEW.project_id
  AND is_current = 1;
END IF;

:NEW.start_date := TRUNC(SYSDATE);
:NEW.end_date := NULL;
:NEW.is_current := 1;
END;
/

CREATE UNIQUE INDEX uk_issue_type_seq ON dw_dashflow.issue_type(original_id, seq, project_id);

-- ISSUE_SEVERITY table with SCD2 handling
CREATE TABLE dw_dashflow.issue_severity (
                                            severity_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                            seq NUMBER NOT NULL,
                                            original_id VARCHAR2(255) NOT NULL,
                                            project_id NUMBER NOT NULL,
                                            severity_name VARCHAR2(255) NOT NULL,
                                            description CLOB,
                                            start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                            end_date DATE,
                                            is_current NUMBER(1) DEFAULT 1 NOT NULL,

                                            CONSTRAINT fk_issue_severity_projects FOREIGN KEY (project_id) REFERENCES dw_dashflow.projects(project_id)
);

CREATE OR REPLACE TRIGGER issue_severity_scd2_trigger
              BEFORE INSERT ON dw_dashflow.issue_severity
                         FOR EACH ROW
                         DECLARE
                         max_seq NUMBER;
BEGIN
    IF :NEW.original_id = '0' THEN
        :NEW.seq := 1;
:NEW.start_date := TRUNC(SYSDATE);
:NEW.end_date := NULL;
:NEW.is_current := 1;
RETURN;
END IF;

SELECT NVL(MAX(seq), 0) INTO max_seq
FROM dw_dashflow.issue_severity
WHERE original_id = :NEW.original_id AND project_id = :NEW.project_id;

:NEW.seq := max_seq + 1;

IF max_seq > 0 THEN
UPDATE dw_dashflow.issue_severity
SET end_date = TRUNC(SYSDATE), is_current = 0
WHERE original_id = :NEW.original_id
  AND project_id = :NEW.project_id
  AND is_current = 1;
END IF;

:NEW.start_date := TRUNC(SYSDATE);
:NEW.end_date := NULL;
:NEW.is_current := 1;
END;
/

CREATE UNIQUE INDEX uk_issue_severity_seq ON dw_dashflow.issue_severity(original_id, seq, project_id);

-- ISSUE_PRIORITY table with SCD2 handling
CREATE TABLE dw_dashflow.issue_priority (
                                            priority_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                            seq NUMBER NOT NULL,
                                            original_id VARCHAR2(255) NOT NULL,
                                            project_id NUMBER NOT NULL,
                                            priority_name VARCHAR2(255) NOT NULL,
                                            description CLOB,
                                            start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                            end_date DATE,
                                            is_current NUMBER(1) DEFAULT 1 NOT NULL,

                                            CONSTRAINT fk_issue_priority_projects FOREIGN KEY (project_id) REFERENCES dw_dashflow.projects(project_id)
);

CREATE OR REPLACE TRIGGER issue_priority_scd2_trigger
              BEFORE INSERT ON dw_dashflow.issue_priority
                         FOR EACH ROW
                         DECLARE
                         max_seq NUMBER;
BEGIN
    IF :NEW.original_id = '0' THEN
        :NEW.seq := 1;
:NEW.start_date := TRUNC(SYSDATE);
:NEW.end_date := NULL;
:NEW.is_current := 1;
RETURN;
END IF;

SELECT NVL(MAX(seq), 0) INTO max_seq
FROM dw_dashflow.issue_priority
WHERE original_id = :NEW.original_id AND project_id = :NEW.project_id;

:NEW.seq := max_seq + 1;

IF max_seq > 0 THEN
UPDATE dw_dashflow.issue_priority
SET end_date = TRUNC(SYSDATE), is_current = 0
WHERE original_id = :NEW.original_id
  AND project_id = :NEW.project_id
  AND is_current = 1;
END IF;

:NEW.start_date := TRUNC(SYSDATE);
:NEW.end_date := NULL;
:NEW.is_current := 1;
END;
/

CREATE UNIQUE INDEX uk_issue_priority_seq ON dw_dashflow.issue_priority(original_id, seq, project_id);

-- FACT_ISSUES table
CREATE TABLE dw_dashflow.fact_issues (
                                         issue_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         original_id VARCHAR2(255) NOT NULL,
                                         status_id NUMBER NOT NULL,
                                         type_id NUMBER NOT NULL,
                                         severity_id NUMBER NOT NULL,
                                         priority_id NUMBER NOT NULL,
                                         assignee_id NUMBER,
                                         project_id NUMBER NOT NULL,
                                         created_at NUMBER NOT NULL,
                                         completed_at NUMBER,
                                         issue_name VARCHAR2(255) NOT NULL,

                                         CONSTRAINT fk_fact_issues_status FOREIGN KEY (status_id) REFERENCES dw_dashflow.issue_status(status_id),
                                         CONSTRAINT fk_fact_issues_type FOREIGN KEY (type_id) REFERENCES dw_dashflow.issue_type(type_id),
                                         CONSTRAINT fk_fact_issues_severity FOREIGN KEY (severity_id) REFERENCES dw_dashflow.issue_severity(severity_id),
                                         CONSTRAINT fk_fact_issues_priority FOREIGN KEY (priority_id) REFERENCES dw_dashflow.issue_priority(priority_id),
                                         CONSTRAINT fk_fact_issues_assignee FOREIGN KEY (assignee_id) REFERENCES dw_dashflow.users(user_id),
                                         CONSTRAINT fk_fact_issues_project FOREIGN KEY (project_id) REFERENCES dw_dashflow.projects(project_id),
                                         CONSTRAINT fk_fact_issues_created_at FOREIGN KEY (created_at) REFERENCES dw_dashflow.dates(date_id),
                                         CONSTRAINT fk_fact_issues_completed_at FOREIGN KEY (completed_at) REFERENCES dw_dashflow.dates(date_id)
);

-- Create index on original_id for better performance
CREATE INDEX idx_fact_issues_original_id ON dw_dashflow.fact_issues(original_id);