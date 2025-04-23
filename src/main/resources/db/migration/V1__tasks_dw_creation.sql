---- TASKS DATA WAREHOUSE CREATION ----

CREATE SCHEMA IF NOT EXISTS dw_dashflow;
SET search_path TO dw_dashflow;

----------------------------------------

CREATE TABLE IF NOT EXISTS tools(
    tool_id SERIAL PRIMARY KEY,
    seq INT NOT NULL,
    tool_name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE DEFAULT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT unique_tool_seq UNIQUE (tool_id, seq)
);

----------------------------------------

CREATE OR REPLACE FUNCTION manage_scd2()
RETURNS TRIGGER AS $$
DECLARE
    max_seq INT;
    business_key_value TEXT;
    business_key_column TEXT := TG_ARGV[0]; -- Get the business key column name from trigger argument
BEGIN
    -- Get the business key value from the new row
    EXECUTE format(
        'SELECT ($1).%I',
        business_key_column
    ) INTO business_key_value USING NEW;

    -- Get the maximum sequence number for the business key
    EXECUTE format(
        'SELECT COALESCE(MAX(seq), 0) FROM %I.%I WHERE %I = $1',
        TG_TABLE_SCHEMA, TG_TABLE_NAME, business_key_column
    ) INTO max_seq USING business_key_value;

    -- Set the new sequence number
    NEW.seq := max_seq + 1;

    -- If this is not the first row, update the previous row
    IF max_seq > 0 THEN
        EXECUTE format(
            'UPDATE %I.%I
             SET end_date = CURRENT_DATE, is_current = FALSE
             WHERE %I = $1 AND is_current = TRUE',
            TG_TABLE_SCHEMA, TG_TABLE_NAME, business_key_column
        ) USING business_key_value;
    END IF;

    -- Set start_date, end_date and is_current for the new row
    NEW.start_date := CURRENT_DATE;
    NEW.end_date := NULL;
    NEW.is_current := TRUE;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

---------------------------------

CREATE OR REPLACE FUNCTION manage_scd2_tools()
RETURNS TRIGGER AS $$
DECLARE
    max_seq INT;
BEGIN
    -- Get the maximum sequence number for the tool name
    SELECT COALESCE(MAX(seq), 0)
    INTO max_seq
    FROM dw_dashflow.tools
    WHERE tool_name = NEW.tool_name;

    -- Set the new sequence number
    NEW.seq := max_seq + 1;

    -- If this is not the first row, update the previous row
    IF max_seq > 0 THEN
        UPDATE dw_dashflow.tools
        SET end_date = CURRENT_DATE, is_current = FALSE
        WHERE tool_name = NEW.tool_name AND is_current = TRUE;
    END IF;

    -- Set start_date, end_date and is_current for the new row
    NEW.start_date := CURRENT_DATE;
    NEW.end_date := NULL;
    NEW.is_current := TRUE;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

---------------------------------

CREATE OR REPLACE TRIGGER tools_scd2_trigger
    BEFORE INSERT ON tools
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2_tools();

INSERT INTO dw_dashflow.tools(
    tool_name)
VALUES ('taiga');
---------------------------------

CREATE TABLE IF NOT EXISTS roles(
    role_id SERIAL PRIMARY KEY,
    seq INT NOT NULL,
    original_id TEXT NOT NULL,
    tool_id INT NOT NULL,
    role_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE DEFAULT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_roles_tools FOREIGN KEY (tool_id) REFERENCES tools(tool_id),
    CONSTRAINT unique_role_seq UNIQUE (original_id, seq, tool_id)
);

CREATE OR REPLACE TRIGGER roles_scd2_trigger
    BEFORE INSERT ON roles
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2('original_id');

--------------------------------

CREATE TABLE IF NOT EXISTS users(
    user_id SERIAL PRIMARY KEY,
    seq INT NOT NULL,
    original_id TEXT NOT NULL,
    tool_id INT NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    description TEXT,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE DEFAULT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_users_tools FOREIGN KEY (tool_id) REFERENCES tools(tool_id),
    CONSTRAINT unique_user_seq UNIQUE (original_id, seq, tool_id)
);

CREATE OR REPLACE TRIGGER users_scd2_trigger
    BEFORE INSERT ON users
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2('original_id');

----------------------------------

CREATE TABLE IF NOT EXISTS user_role(
    role_id INT NOT NULL,
    user_id INT NOT NULL,

    CONSTRAINT pk_user_role PRIMARY KEY (role_id, user_id),
    CONSTRAINT fk_user_role_users FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_user_role_roles FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-------------------------------------

CREATE TABLE IF NOT EXISTS projects(
    project_id SERIAL PRIMARY KEY,
    seq INT NOT NULL,
    original_id TEXT NOT NULL,
    tool_id INT NOT NULL,
    project_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE DEFAULT NULL,
    is_finished BOOLEAN,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_projects_tools FOREIGN KEY (tool_id) REFERENCES tools(tool_id),
    CONSTRAINT unique_project_seq UNIQUE (original_id, seq, tool_id)
);

CREATE OR REPLACE TRIGGER projects_scd2_trigger
    BEFORE INSERT ON projects
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2('original_id');

-------------------------------------

CREATE TABLE IF NOT EXISTS status(
    status_id SERIAL PRIMARY KEY,
    seq INT NOT NULL,
    original_id TEXT NOT NULL,
    project_id INT NOT NULL,
    status_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE DEFAULT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_status_projects FOREIGN KEY (project_id) REFERENCES projects(project_id),
    CONSTRAINT unique_status_seq UNIQUE (original_id, seq, project_id)
);

CREATE OR REPLACE TRIGGER status_scd2_trigger
    BEFORE INSERT ON status
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2('original_id');

-------------------------------------

CREATE TABLE IF NOT EXISTS epics(
    epic_id SERIAL PRIMARY KEY,
    seq INT NOT NULL,
    original_id TEXT NOT NULL,
    project_id INT NOT NULL,
    epic_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE DEFAULT NULL,
    is_finished BOOLEAN,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_epics_projects FOREIGN KEY (project_id) REFERENCES projects(project_id),
    CONSTRAINT unique_epics_seq UNIQUE (original_id, seq, project_id)
);

CREATE OR REPLACE TRIGGER epics_scd2_trigger
    BEFORE INSERT ON epics
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2('original_id');

CREATE OR REPLACE FUNCTION create_epicless_epic()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO dw_dashflow.epics (
    original_id,
    project_id,
    epic_name,
    is_finished
)

VALUES (
        '0',
        NEW.project_id,
        'epicless',
        FALSE);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_project_create_epicless
    AFTER INSERT ON dw_dashflow.projects
    FOR EACH ROW
    EXECUTE FUNCTION create_epicless_epic();

-------------------------------------

CREATE TABLE IF NOT EXISTS stories(
    story_id SERIAL PRIMARY KEY,
    seq INT NOT NULL,
    original_id TEXT NOT NULL,
    epic_id INT NOT NULL,
    story_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE DEFAULT NULL,
    is_finished BOOLEAN,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_story_epics FOREIGN KEY (epic_id) REFERENCES epics(epic_id),
    CONSTRAINT unique_stories_seq UNIQUE (original_id, seq, epic_id)
);

CREATE OR REPLACE TRIGGER stories_scd2_trigger
    BEFORE INSERT ON stories
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2('original_id');

-------------------------------------

CREATE TABLE IF NOT EXISTS dates(
    date_id SERIAL PRIMARY KEY,
    date_date DATE NOT NULL,
    month INT CHECK (month BETWEEN 1 AND 12),
    year INT,
    quarter INT CHECK (quarter BETWEEN 1 AND 4),
    day_of_week INT CHECK (day_of_week BETWEEN 1 AND 7),
    day_of_month INT CHECK (day_of_month BETWEEN 1 AND 31),
    day_of_year INT CHECK (day_of_year BETWEEN 1 AND 366),
    is_weekend BOOLEAN
);

INSERT INTO dates(date_date, month, year, quarter, day_of_week, day_of_month, day_of_year,is_weekend)
SELECT
    date_date,
    EXTRACT(MONTH FROM date_date) AS month,
    EXTRACT(YEAR FROM date_date) AS year,
    EXTRACT(QUARTER FROM date_date) AS quarter,
    EXTRACT(ISODOW FROM date_date) AS day_of_week, -- Retorna 1-7 (Segunda-Domingo)
    EXTRACT(DAY FROM date_date) AS day_of_month,
    EXTRACT(DOY FROM date_date) AS day_of_year,
    EXTRACT(ISODOW FROM date_date) IN (6,7) AS is_weekend -- True se sab-dom(6-7)
FROM generate_series(
    '2020-01-01'::date,
    '2099-12-31'::date,
    '1 day'::interval
     ) AS date_date;

-------------------------------------

CREATE TABLE IF NOT EXISTS tags(
    tag_id SERIAL PRIMARY KEY,
    seq INT NOT NULL,
    original_id TEXT NOT NULL,
    project_id INT NOT NULL,
    tag_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE DEFAULT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_tags_projects FOREIGN KEY (project_id) REFERENCES projects(project_id),
    CONSTRAINT unique_tags_seq UNIQUE (original_id, seq, project_id)
);

CREATE OR REPLACE TRIGGER tags_scd2_trigger
    BEFORE INSERT ON tags
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2('original_id');

-------------------

CREATE TABLE IF NOT EXISTS fact_tasks(
    task_id SERIAL PRIMARY KEY,
    original_id TEXT NOT NULL,
    status_id INT NOT NULL,
    assignee_id INT,
    tool_id INT NOT NULL,
    story_id INT NOT NULL,

    created_at INT NOT NULL,
    started_at INT,
    completed_at INT,
    due_date INT,

    task_name VARCHAR(255) NOT NULL,
    description TEXT,
    story_points INT,
    is_blocked BOOLEAN,
    is_storyless BOOLEAN,

    CONSTRAINT fk_fact_tasks_status FOREIGN KEY (status_id) REFERENCES status(status_id),
    CONSTRAINT fk_fact_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users(user_id),
    CONSTRAINT fk_fact_tasks_tools FOREIGN KEY (tool_id) REFERENCES tools(tool_id),
    CONSTRAINT fk_fact_tasks_stories FOREIGN KEY (story_id) REFERENCES stories(story_id),
    CONSTRAINT fk_fact_tasks_created_at FOREIGN KEY (created_at) REFERENCES dates(date_id),
    CONSTRAINT fk_fact_tasks_started_at FOREIGN KEY (started_at) REFERENCES dates(date_id),
    CONSTRAINT fk_fact_tasks_completed_at FOREIGN KEY (completed_at) REFERENCES dates(date_id),
    CONSTRAINT fk_fact_tasks_due_date FOREIGN KEY (due_date) REFERENCES dates(date_id)
);

-------------------------

CREATE TABLE IF NOT EXISTS task_tag(
    task_id INT NOT NULL,
    tag_id INT NOT NULL,

    CONSTRAINT pk_task_tag PRIMARY KEY (task_id, tag_id),
    CONSTRAINT fk_task_tag_tasks FOREIGN KEY (task_id) REFERENCES fact_tasks(task_id),
    CONSTRAINT fk_task_tag_tags FOREIGN KEY (tag_id) REFERENCES tags(tag_id)
);