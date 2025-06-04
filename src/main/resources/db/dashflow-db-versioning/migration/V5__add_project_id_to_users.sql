-- Switch to the dw_dashflow schema (implicit in Oracle through schema prefixing)

-- Add the new column with a default value first (since it's NOT NULL)
ALTER TABLE dw_dashflow.users
    ADD (project_id NUMBER);

-- Update existing records with a default project ID
-- You'll need to specify an appropriate default project ID
UPDATE dw_dashflow.users
SET project_id = (SELECT MIN(project_id) FROM dw_dashflow.projects)
WHERE project_id IS NULL;

-- Now modify the column to be NOT NULL
ALTER TABLE dw_dashflow.users
    MODIFY (project_id NUMBER NOT NULL);

-- Add the foreign key constraint
ALTER TABLE dw_dashflow.users
    ADD CONSTRAINT fk_users_project
        FOREIGN KEY (project_id)
            REFERENCES dw_dashflow.projects(project_id);