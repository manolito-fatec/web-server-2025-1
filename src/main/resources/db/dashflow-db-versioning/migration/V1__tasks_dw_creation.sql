-- CREATE USER dw_dashflow IDENTIFIED BY password
--     DEFAULT TABLESPACE users
--     TEMPORARY TABLESPACE temp
--     QUOTA UNLIMITED ON users;

GRANT CONNECT, RESOURCE TO dw_dashflow;

CREATE TABLE dw_dashflow.tools (
                                   tool_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   seq NUMBER NOT NULL,
                                   tool_name VARCHAR2(255) NOT NULL,
                                   start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                   end_date DATE,
                                   is_current NUMBER(1) DEFAULT 1 NOT NULL
);

CREATE OR REPLACE TRIGGER tools_scd2_trigger
    BEFORE INSERT ON dw_dashflow.tools
    FOR EACH ROW
DECLARE
    max_seq NUMBER;
BEGIN
    SELECT NVL(MAX(seq), 0) INTO max_seq
    FROM dw_dashflow.tools
    WHERE tool_name = :NEW.tool_name;

    :NEW.seq := max_seq + 1;

    IF max_seq > 0 THEN
        UPDATE dw_dashflow.tools
        SET end_date = TRUNC(SYSDATE), is_current = 0
        WHERE tool_name = :NEW.tool_name AND is_current = 1;
    END IF;

    :NEW.start_date := TRUNC(SYSDATE);
    :NEW.end_date := NULL;
    :NEW.is_current := 1;
END;
/

-- Create unique constraint
CREATE UNIQUE INDEX uk_tool_seq ON dw_dashflow.tools(tool_id, seq);

-- Insert initial tool data
INSERT INTO dw_dashflow.tools(tool_name) VALUES ('taiga');
INSERT INTO dw_dashflow.tools(tool_name) VALUES ('trello');
INSERT INTO dw_dashflow.tools(tool_name) VALUES ('jira');
COMMIT;

-- ROLES table with SCD2 handling
CREATE TABLE dw_dashflow.roles (
                                   role_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   seq NUMBER NOT NULL,
                                   original_id VARCHAR2(255) NOT NULL,
                                   tool_id NUMBER NOT NULL,
                                   role_name VARCHAR2(255) NOT NULL,
                                   description VARCHAR2(255),
                                   start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                   end_date DATE,
                                   is_current NUMBER(1) DEFAULT 1 NOT NULL,

                                   CONSTRAINT fk_roles_tools FOREIGN KEY (tool_id) REFERENCES dw_dashflow.tools(tool_id)
);

CREATE OR REPLACE TRIGGER dw_dashflow.roles_scd2_trigger
    BEFORE INSERT ON dw_dashflow.roles
    FOR EACH ROW
DECLARE
    PRAGMA AUTONOMOUS_TRANSACTION;
    max_seq NUMBER;
BEGIN
    -- Handle special case for '0' records
    IF :NEW.original_id = '0' THEN
        :NEW.seq := 1;
        :NEW.start_date := TRUNC(SYSDATE);
        :NEW.end_date := NULL;
        :NEW.is_current := 1;
        RETURN;
    END IF;

    -- Get the max sequence number safely
    BEGIN
        SELECT NVL(MAX(seq), 0) INTO max_seq
        FROM dw_dashflow.roles
        WHERE original_id = :NEW.original_id
          AND tool_id = :NEW.tool_id
          AND ROWNUM = 1; -- Ensure single row
    EXCEPTION
        WHEN OTHERS THEN
            max_seq := 0;
    END;

    :NEW.seq := max_seq + 1;
    :NEW.start_date := TRUNC(SYSDATE);
    :NEW.end_date := NULL;
    :NEW.is_current := 1;

    -- Update previous records if needed
    IF max_seq > 0 THEN
        UPDATE dw_dashflow.roles
        SET end_date = TRUNC(SYSDATE),
            is_current = 0
        WHERE original_id = :NEW.original_id
          AND tool_id = :NEW.tool_id
          AND is_current = 1;
    END IF;

    COMMIT; -- Required for autonomous transaction
END;
/

CREATE UNIQUE INDEX uk_role_seq ON dw_dashflow.roles(original_id, seq, tool_id);

-- USERS table with SCD2 handling
CREATE TABLE dw_dashflow.users (
                                   user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   seq NUMBER NOT NULL,
                                   original_id VARCHAR2(255) NOT NULL,
                                   tool_id NUMBER NOT NULL,
                                   user_name VARCHAR2(255) NOT NULL,
                                   email VARCHAR2(255),
                                   description VARCHAR2(255),
                                   start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                   end_date DATE,
                                   is_current NUMBER(1) DEFAULT 1 NOT NULL,

                                   CONSTRAINT fk_users_tools FOREIGN KEY (tool_id) REFERENCES dw_dashflow.tools(tool_id)
);

CREATE OR REPLACE TRIGGER DW_DASHFLOW.USERS_SCD2_TRIGGER
    BEFORE INSERT ON DW_DASHFLOW.USERS
    FOR EACH ROW
DECLARE
    PRAGMA AUTONOMOUS_TRANSACTION;
    max_seq NUMBER;
BEGIN
    -- Handle special case for '0' records
    IF :NEW.original_id = '0' THEN
        :NEW.seq := 1;
        :NEW.start_date := TRUNC(SYSDATE);
        :NEW.end_date := NULL;
        :NEW.is_current := 1;
        RETURN;
    END IF;

    -- Get the max sequence number safely
    BEGIN
        SELECT NVL(MAX(seq), 0) INTO max_seq
        FROM DW_DASHFLOW.USERS
        WHERE original_id = :NEW.original_id
          AND tool_id = :NEW.tool_id
          AND ROWNUM = 1;
    EXCEPTION
        WHEN OTHERS THEN
            max_seq := 0;
    END;

    :NEW.seq := max_seq + 1;
    :NEW.start_date := TRUNC(SYSDATE);
    :NEW.end_date := NULL;
    :NEW.is_current := 1;

    -- Update previous records if needed
    IF max_seq > 0 THEN
        BEGIN
            UPDATE DW_DASHFLOW.USERS
            SET end_date = TRUNC(SYSDATE),
                is_current = 0
            WHERE original_id = :NEW.original_id
              AND tool_id = :NEW.tool_id
              AND is_current = 1;
        EXCEPTION
            WHEN OTHERS THEN
                NULL; -- Silently handle any errors
        END;
    END IF;

    COMMIT; -- Required for autonomous transaction
END;
/

CREATE UNIQUE INDEX uk_user_seq ON dw_dashflow.users(original_id, seq, tool_id);

-- USER_ROLE junction table
CREATE TABLE dw_dashflow.user_role (
                                       role_id NUMBER NOT NULL,
                                       user_id NUMBER NOT NULL,

                                       CONSTRAINT pk_user_role PRIMARY KEY (role_id, user_id),
                                       CONSTRAINT fk_user_role_roles FOREIGN KEY (role_id) REFERENCES dw_dashflow.roles(role_id),
                                       CONSTRAINT fk_user_role_users FOREIGN KEY (user_id) REFERENCES dw_dashflow.users(user_id)
);

-- PROJECTS table with SCD2 handling
CREATE TABLE dw_dashflow.projects (
                                      project_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      seq NUMBER NOT NULL,
                                      original_id VARCHAR2(255) NOT NULL,
                                      tool_id NUMBER NOT NULL,
                                      project_name VARCHAR2(255) NOT NULL,
                                      description VARCHAR2(255),
                                      start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                      end_date DATE,
                                      is_finished NUMBER(1),
                                      is_current NUMBER(1) DEFAULT 1 NOT NULL,

                                      CONSTRAINT fk_projects_tools FOREIGN KEY (tool_id) REFERENCES dw_dashflow.tools(tool_id)
);

CREATE OR REPLACE TRIGGER projects_scd2_trigger
    BEFORE INSERT ON dw_dashflow.projects
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
    FROM dw_dashflow.projects
    WHERE original_id = :NEW.original_id AND tool_id = :NEW.tool_id;

    :NEW.seq := max_seq + 1;

    IF max_seq > 0 THEN
        UPDATE dw_dashflow.projects
        SET end_date = TRUNC(SYSDATE), is_current = 0
        WHERE original_id = :NEW.original_id
          AND tool_id = :NEW.tool_id
          AND is_current = 1;
    END IF;

    :NEW.start_date := TRUNC(SYSDATE);
    :NEW.end_date := NULL;
    :NEW.is_current := 1;
END;
/

CREATE UNIQUE INDEX uk_project_seq ON dw_dashflow.projects(original_id, seq, tool_id);

-- STATUS table with SCD2 handling
CREATE TABLE dw_dashflow.status (
                                    status_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    seq NUMBER NOT NULL,
                                    original_id VARCHAR2(255) NOT NULL,
                                    project_id NUMBER NOT NULL,
                                    status_name VARCHAR2(255) NOT NULL,
                                    description VARCHAR2(255),
                                    start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                    end_date DATE,
                                    is_current NUMBER(1) DEFAULT 1 NOT NULL,

                                    CONSTRAINT fk_status_projects FOREIGN KEY (project_id) REFERENCES dw_dashflow.projects(project_id)
);

CREATE OR REPLACE TRIGGER status_scd2_trigger
    BEFORE INSERT ON dw_dashflow.status
    FOR EACH ROW
DECLARE
    PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
    -- Handle special case for '0' records
    IF :NEW.original_id = '0' THEN
        :NEW.seq := 1;
        :NEW.start_date := TRUNC(SYSDATE);
        :NEW.end_date := NULL;
        :NEW.is_current := 1;
        RETURN;
    END IF;

    -- Call the equivalent of your manage_scd2 function logic
    -- This is inline implementation since Oracle doesn't support EXECUTE FUNCTION in triggers
    DECLARE
        max_seq NUMBER;
    BEGIN
        -- Get the max sequence number safely
        BEGIN
            SELECT NVL(MAX(seq), 0) INTO max_seq
            FROM dw_dashflow.status
            WHERE original_id = :NEW.original_id
              AND project_id = :NEW.project_id
              AND ROWNUM = 1;
        EXCEPTION
            WHEN OTHERS THEN
                max_seq := 0;
        END;

        :NEW.seq := max_seq + 1;

        -- Update previous records if needed
        IF max_seq > 0 THEN
            UPDATE dw_dashflow.status
            SET end_date = TRUNC(SYSDATE),
                is_current = 0
            WHERE original_id = :NEW.original_id
              AND project_id = :NEW.project_id
              AND is_current = 1;
        END IF;

        :NEW.start_date := TRUNC(SYSDATE);
        :NEW.end_date := NULL;
        :NEW.is_current := 1;
    END;

    COMMIT; -- Required for autonomous transaction
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/

-- EPICS table with SCD2 handling and epicless trigger
CREATE TABLE dw_dashflow.epics (
                                   epic_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   seq NUMBER NOT NULL,
                                   original_id VARCHAR2(255) NOT NULL,
                                   project_id NUMBER NOT NULL,
                                   epic_name VARCHAR2(255) NOT NULL,
                                   description VARCHAR2(255),
                                   start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                   end_date DATE,
                                   is_finished NUMBER(1),
                                   is_current NUMBER(1) DEFAULT 1 NOT NULL,

                                   CONSTRAINT fk_epics_projects FOREIGN KEY (project_id) REFERENCES dw_dashflow.projects(project_id)
);

CREATE OR REPLACE TRIGGER epics_scd2_trigger
    BEFORE INSERT ON dw_dashflow.epics
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
    FROM dw_dashflow.epics
    WHERE original_id = :NEW.original_id AND project_id = :NEW.project_id;

    :NEW.seq := max_seq + 1;

    IF max_seq > 0 THEN
        UPDATE dw_dashflow.epics
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

CREATE UNIQUE INDEX uk_epics_seq ON dw_dashflow.epics(original_id, seq, project_id);

-- Trigger for creating epicless epics
CREATE OR REPLACE TRIGGER trg_project_create_epicless
    AFTER INSERT ON dw_dashflow.projects
    FOR EACH ROW
DECLARE
    max_seq NUMBER;
BEGIN
    SELECT NVL(MAX(seq), 0) INTO max_seq
    FROM dw_dashflow.epics
    WHERE original_id = '0' AND project_id = :NEW.project_id;

    INSERT INTO dw_dashflow.epics (
        original_id,
        project_id,
        epic_name,
        is_finished,
        seq,
        start_date,
        end_date,
        is_current
    ) VALUES (
                 '0',
                 :NEW.project_id,
                 'epicless',
                 0,
                 max_seq + 1,
                 TRUNC(SYSDATE),
                 NULL,
                 1
             );

    IF max_seq > 0 THEN
        UPDATE dw_dashflow.epics
        SET end_date = TRUNC(SYSDATE),
            is_current = 0
        WHERE original_id = '0'
          AND project_id = :NEW.project_id
          AND is_current = 1;
    END IF;
END;
/

-- STORIES table with SCD2 handling
CREATE TABLE dw_dashflow.stories (
                                     story_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     seq NUMBER NOT NULL,
                                     original_id VARCHAR2(255) NOT NULL,
                                     epic_id NUMBER NOT NULL,
                                     story_name VARCHAR2(255) NOT NULL,
                                     description VARCHAR2(255),
                                     start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                     end_date DATE,
                                     is_finished NUMBER(1),
                                     is_current NUMBER(1) DEFAULT 1 NOT NULL,

                                     CONSTRAINT fk_stories_epics FOREIGN KEY (epic_id) REFERENCES dw_dashflow.epics(epic_id)
);

CREATE OR REPLACE TRIGGER stories_scd2_trigger
    BEFORE INSERT ON dw_dashflow.stories
    FOR EACH ROW
DECLARE
    PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
    -- Handle special case for '0' records
    IF :NEW.original_id = '0' THEN
        :NEW.seq := 1;
        :NEW.start_date := TRUNC(SYSDATE);
        :NEW.end_date := NULL;
        :NEW.is_current := 1;
        RETURN;
    END IF;

    -- Call the equivalent of your manage_scd2 function logic
    -- This is inline implementation since Oracle doesn't support EXECUTE FUNCTION in triggers
    DECLARE
        max_seq NUMBER;
    BEGIN
        -- Get the max sequence number safely
        BEGIN
            SELECT NVL(MAX(seq), 0) INTO max_seq
            FROM dw_dashflow.stories
            WHERE original_id = :NEW.original_id
              AND epic_id = :NEW.epic_id
              AND ROWNUM = 1;
        EXCEPTION
            WHEN OTHERS THEN
                max_seq := 0;
        END;

        :NEW.seq := max_seq + 1;

        -- Update previous records if needed
        IF max_seq > 0 THEN
            UPDATE dw_dashflow.stories
            SET end_date = TRUNC(SYSDATE),
                is_current = 0
            WHERE original_id = :NEW.original_id
              AND epic_id = :NEW.epic_id
              AND is_current = 1;
        END IF;

        :NEW.start_date := TRUNC(SYSDATE);
        :NEW.end_date := NULL;
        :NEW.is_current := 1;
    END;

    COMMIT; -- Required for autonomous transaction
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/

-- DATES dimension table
CREATE TABLE dw_dashflow.dates (
                                   date_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   date_date DATE NOT NULL,
                                   month NUMBER(2) CHECK (month BETWEEN 1 AND 12),
                                   year NUMBER(4),
                                   quarter NUMBER(1) CHECK (quarter BETWEEN 1 AND 4),
                                   day_of_week NUMBER(1) CHECK (day_of_week BETWEEN 1 AND 7),
                                   day_of_month NUMBER(2) CHECK (day_of_month BETWEEN 1 AND 31),
                                   day_of_year NUMBER(3) CHECK (day_of_year BETWEEN 1 AND 366),
                                   is_weekend NUMBER(1)
);

-- Populate DATES table (2020-2099)
INSERT INTO dw_dashflow.dates (
    date_date, month, year, quarter, day_of_week,
    day_of_month, day_of_year, is_weekend
)
SELECT
    dt,
    EXTRACT(MONTH FROM dt),
    EXTRACT(YEAR FROM dt),
    TO_CHAR(dt, 'Q'),
    TO_CHAR(dt, 'D'), -- 1=Sunday, 7=Saturday
    EXTRACT(DAY FROM dt),
    TO_CHAR(dt, 'DDD'),
    CASE WHEN TO_CHAR(dt, 'D') IN (1,7) THEN 1 ELSE 0 END
FROM (
         SELECT TO_DATE('2020-01-01', 'YYYY-MM-DD') + LEVEL - 1 AS dt
         FROM dual
         CONNECT BY TO_DATE('2020-01-01', 'YYYY-MM-DD') + LEVEL - 1 <= TO_DATE('2099-12-31', 'YYYY-MM-DD')
     );
COMMIT;

-- TAGS table with SCD2 handling
CREATE TABLE dw_dashflow.tags (
                                  tag_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  seq NUMBER NOT NULL,
                                  original_id VARCHAR2(255) NOT NULL,
                                  project_id NUMBER NOT NULL,
                                  tag_name VARCHAR2(255) NOT NULL,
                                  description VARCHAR2(255),
                                  start_date DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
                                  end_date DATE,
                                  is_current NUMBER(1) DEFAULT 1 NOT NULL,

                                  CONSTRAINT fk_tags_projects FOREIGN KEY (project_id) REFERENCES dw_dashflow.projects(project_id)
);
CREATE OR REPLACE TRIGGER tags_scd2_trigger
    BEFORE INSERT ON dw_dashflow.tags
    FOR EACH ROW
DECLARE
    PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
    -- Handle special case for '0' records
    IF :NEW.original_id = '0' THEN
        :NEW.seq := 1;
        :NEW.start_date := TRUNC(SYSDATE);
        :NEW.end_date := NULL;
        :NEW.is_current := 1;
        RETURN;
    END IF;

    -- Call the equivalent of your manage_scd2 function logic
    -- This is inline implementation since Oracle doesn't support EXECUTE FUNCTION in triggers
    DECLARE
        max_seq NUMBER;
    BEGIN
        -- Get the max sequence number safely
        BEGIN
            SELECT NVL(MAX(seq), 0) INTO max_seq
            FROM dw_dashflow.tags
            WHERE original_id = :NEW.original_id
              AND project_id = :NEW.project_id
              AND ROWNUM = 1;
        EXCEPTION
            WHEN OTHERS THEN
                max_seq := 0;
        END;

        :NEW.seq := max_seq + 1;

        -- Update previous records if needed
        IF max_seq > 0 THEN
            UPDATE dw_dashflow.tags
            SET end_date = TRUNC(SYSDATE),
                is_current = 0
            WHERE original_id = :NEW.original_id
              AND project_id = :NEW.project_id
              AND is_current = 1;
        END IF;

        :NEW.start_date := TRUNC(SYSDATE);
        :NEW.end_date := NULL;
        :NEW.is_current := 1;
    END;

    COMMIT; -- Required for autonomous transaction
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
-- FACT_TASKS table
CREATE TABLE dw_dashflow.fact_tasks (
                                        task_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        original_id VARCHAR2(255) NOT NULL,
                                        status_id NUMBER NOT NULL,
                                        assignee_id NUMBER,
                                        tool_id NUMBER NOT NULL,
                                        story_id NUMBER NOT NULL,
                                        created_at NUMBER NOT NULL,
                                        started_at NUMBER,
                                        completed_at NUMBER,
                                        due_date NUMBER,
                                        task_name VARCHAR2(255) NOT NULL,
                                        description VARCHAR2(255),
                                        story_points NUMBER,
                                        is_blocked NUMBER(1),
                                        is_storyless NUMBER(1),

                                        CONSTRAINT fk_fact_tasks_status FOREIGN KEY (status_id) REFERENCES dw_dashflow.status(status_id),
                                        CONSTRAINT fk_fact_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES dw_dashflow.users(user_id),
                                        CONSTRAINT fk_fact_tasks_tools FOREIGN KEY (tool_id) REFERENCES dw_dashflow.tools(tool_id),
                                        CONSTRAINT fk_fact_tasks_stories FOREIGN KEY (story_id) REFERENCES dw_dashflow.stories(story_id),
                                        CONSTRAINT fk_fact_tasks_created_at FOREIGN KEY (created_at) REFERENCES dw_dashflow.dates(date_id),
                                        CONSTRAINT fk_fact_tasks_started_at FOREIGN KEY (started_at) REFERENCES dw_dashflow.dates(date_id),
                                        CONSTRAINT fk_fact_tasks_completed_at FOREIGN KEY (completed_at) REFERENCES dw_dashflow.dates(date_id),
                                        CONSTRAINT fk_fact_tasks_due_date FOREIGN KEY (due_date) REFERENCES dw_dashflow.dates(date_id)
);

-- TASK_TAG junction table
CREATE TABLE dw_dashflow.task_tag (
                                      task_id NUMBER NOT NULL,
                                      tag_id NUMBER NOT NULL,

                                      CONSTRAINT pk_task_tag PRIMARY KEY (task_id, tag_id),
                                      CONSTRAINT fk_task_tag_tasks FOREIGN KEY (task_id) REFERENCES dw_dashflow.fact_tasks(task_id),
                                      CONSTRAINT fk_task_tag_tags FOREIGN KEY (tag_id) REFERENCES dw_dashflow.tags(tag_id)
);