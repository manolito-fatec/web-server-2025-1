---- ISSUES DATA WAREHOUSE CREATION ----

SET search_path TO dw_dashflow;

----------------------------------------

CREATE TABLE IF NOT EXISTS issue_status(
    status_id SERIAL PRIMARY KEY,
    seq INT NOT NULL,
    original_id TEXT NOT NULL,
    project_id INT NOT NULL,
    status_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE DEFAULT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_issue_status_projects FOREIGN KEY (project_id) REFERENCES projects(project_id),
    CONSTRAINT unique_issues_status_seq UNIQUE (original_id, seq, project_id)
);

CREATE OR REPLACE TRIGGER issue_status_scd2_trigger
    BEFORE INSERT ON issue_status
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2('original_id');

CREATE TABLE IF NOT EXISTS issue_type(
    type_id SERIAL PRIMARY KEY,
    seq INT NOT NULL,
    original_id TEXT NOT NULL,
    project_id INT NOT NULL,
    type_name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE DEFAULT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_issue_status_projects FOREIGN KEY (project_id) REFERENCES projects(project_id),
    CONSTRAINT unique_issues_type_seq UNIQUE (original_id, seq, project_id)
);

CREATE OR REPLACE TRIGGER issue_type_scd2_trigger
    BEFORE INSERT ON issue_type
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2('original_id');

CREATE TABLE IF NOT EXISTS issue_severity(
    severity_id SERIAL PRIMARY KEY,
    seq INT NOT NULL,
    original_id TEXT NOT NULL,
    project_id INT NOT NULL,
    severity_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE DEFAULT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_issue_status_projects FOREIGN KEY (project_id) REFERENCES projects(project_id),
    CONSTRAINT unique_issues_severity_seq UNIQUE (original_id, seq, project_id)
);

CREATE OR REPLACE TRIGGER issue_severity_scd2_trigger
    BEFORE INSERT ON issue_severity
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2('original_id');

CREATE TABLE IF NOT EXISTS issue_priority(
     priority_id SERIAL PRIMARY KEY,
     seq INT NOT NULL,
     original_id TEXT NOT NULL,
     project_id INT NOT NULL,
     priority_name VARCHAR(255) NOT NULL,
     description TEXT,
     start_date DATE NOT NULL DEFAULT CURRENT_DATE,
     end_date DATE DEFAULT NULL,
     is_current BOOLEAN NOT NULL DEFAULT TRUE,

     CONSTRAINT fk_issue_status_projects FOREIGN KEY (project_id) REFERENCES projects(project_id),
     CONSTRAINT unique_issues_priority_seq UNIQUE (original_id, seq, project_id)
);

CREATE OR REPLACE TRIGGER issue_priority_scd2_trigger
    BEFORE INSERT ON issue_priority
    FOR EACH ROW
EXECUTE FUNCTION manage_scd2('original_id');
----------------------------------------

CREATE TABLE IF NOT EXISTS fact_issues(
    issue_id SERIAL PRIMARY KEY,
    original_id TEXT NOT NULL,
    status_id INT NOT NULL,
    type_id INT NOT NULL,
    severity_id INT NOT NULL,
    priority_id INT NOT NULL,
    assignee_id INT,
    project_id INT NOT NULL,

    created_at INT NOT NULL,
    completed_at INT,

    issue_name VARCHAR(255) NOT NULL,

    CONSTRAINT fk_fact_issues_status FOREIGN KEY (status_id) REFERENCES issue_status(status_id),
    CONSTRAINT fk_fact_issue_type FOREIGN KEY (type_id) REFERENCES issue_type(type_id),
    CONSTRAINT fk_fact_issue_severity FOREIGN KEY (severity_id) REFERENCES issue_severity(severity_id),
    CONSTRAINT fk_fact_issue_priority FOREIGN KEY (priority_id) REFERENCES issue_priority(priority_id),
    CONSTRAINT fk_fact_issues_assignee FOREIGN KEY (assignee_id) REFERENCES users(user_id),
    CONSTRAINT fk_fact_issues_project FOREIGN KEY (project_id) REFERENCES projects(project_id),
    CONSTRAINT fk_fact_issues_created_at FOREIGN KEY (created_at) REFERENCES dates(date_id),
    CONSTRAINT fk_fact_issues_completed_at FOREIGN KEY (completed_at) REFERENCES dates(date_id)
);

----------------------------------------
