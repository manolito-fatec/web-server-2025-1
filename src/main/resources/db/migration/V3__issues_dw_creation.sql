---- ISSUES DATA WAREHOUSE CREATION ----

SET search_path TO dw_dashflow;

----------------------------------------

CREATE TABLE IF NOT EXISTS fact_issues(
    issue_id SERIAL PRIMARY KEY,
    original_id TEXT NOT NULL,
    status_id INT NOT NULL,
    assignee_id INT,
    tool_id INT NOT NULL,

    created_at INT NOT NULL,
    completed_at INT,

    issue_name VARCHAR(255) NOT NULL,
    description TEXT,
    story_points INT,
    is_blocked BOOLEAN,
    is_storyless BOOLEAN,

    CONSTRAINT fk_fact_issues_status FOREIGN KEY (status_id) REFERENCES status(status_id),
    CONSTRAINT fk_fact_issues_assignee FOREIGN KEY (assignee_id) REFERENCES users(user_id),
    CONSTRAINT fk_fact_issues_tools FOREIGN KEY (tool_id) REFERENCES tools(tool_id),
    CONSTRAINT fk_fact_issues_created_at FOREIGN KEY (created_at) REFERENCES dates(date_id),
    CONSTRAINT fk_fact_issues_completed_at FOREIGN KEY (completed_at) REFERENCES dates(date_id)
);

----------------------------------------
