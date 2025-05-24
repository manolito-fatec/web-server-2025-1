
SET search_path TO dw_dashflow;

ALTER TABLE users ADD COLUMN project_id INT NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_project
        FOREIGN KEY (project_id) REFERENCES projects(project_id);

