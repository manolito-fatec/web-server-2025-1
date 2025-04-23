----------- INTERNAL APPLICATION DB CREATION -----------

CREATE SCHEMA IF NOT EXISTS dashflow_appl;
SET search_path TO dashflow_appl;

----- USERS TABLE -----

CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(60) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

----- TOOL TABLE -----

CREATE TABLE IF NOT EXISTS tools(
    tool_id SERIAL PRIMARY KEY,
    tool_name VARCHAR(255) NOT NULL
);

INSERT INTO dw_dashflow.tools(
    tool_name)
VALUES ('taiga');

----- ACCOUNTS TABLE -----

CREATE TABLE IF NOT EXISTS accounts (
    account VARCHAR(255),
    user_id INT,
    tool_id INT,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_tool FOREIGN KEY (tool_id) REFERENCES tools(tool_id) ON DELETE CASCADE,
    CONSTRAINT unique_user_tool UNIQUE (user_id, tool_id));

----- ROLES TABLE -----

CREATE TABLE IF NOT EXISTS roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(60) UNIQUE NOT NULL
);

----- PERMISSION TABLE -----

CREATE TABLE IF NOT EXISTS permissions (
    permission_id SERIAL PRIMARY KEY,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

--------- BRIDGE TABLES ------------

CREATE TABLE IF NOT EXISTS user_roles (
    user_id INT,
    role_id INT,
    PRIMARY KEY (user_id, role_id),

	CONSTRAINT fk_user_roles_users FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
	CONSTRAINT fk_user_roles_roles FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT,
    permission_id INT,
    PRIMARY KEY (role_id, permission_id),

	CONSTRAINT fk_role_permissions_roles FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
	CONSTRAINT fk_role_permissions_permissions FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
);

-------- SEED DATA ---------

--- roles ---
INSERT INTO roles (role_name) VALUES
('ROLE_OPERATOR'),
('ROLE_MANAGER'),
('ROLE_ADMIN')
ON CONFLICT (role_name) DO NOTHING;

--- permissions ---
INSERT INTO permissions (permission_name, description) VALUES
-- ROLE_USER
('view_assigned_cards', 'Visualizar cards atribuídos'),
('view_cards_by_period', 'Visualizar cards por período'),
('view_finished_cards', 'Visualizar cards finalizados'),
('view_cards_by_status', 'Visualizar cards por status'),
('view_average_execution_time', 'Visualizar tempo médio de execução'),
('view_projects_participated', 'Visualizar projetos participados'),
('export_csv', 'Exportar dados para CSV'),

-- ROLE_MANAGER
('view_assigned_cards_count', 'Visualizar quantidade de cards atribuídos'),
('view_cards_by_employee', 'Visualizar cards por colaborador'),
('view_created_cards_by_period', 'Visualizar cards criados por período'),
('view_finished_cards_by_period', 'Visualizar cards finalizados por período'),
('view_average_execution_time_by_employee', 'Visualizar tempo médio de execução por colaborador'),
('view_reworked_cards', 'Visualizar cards com retrabalho'),
('view_issues_by_project', 'Visualizar issues por projeto'),
('view_cards_by_tags', 'Visualizar cards por tags'),

-- ROLE_ADMIN
('view_total_projects', 'Visualizar quantidade total de projetos'),
('view_total_issues', 'Visualizar quantidade total de issues'),
('view_total_cards', 'Visualizar quantidade total de cards'),
('view_project_manager', 'Visualizar gestor do projeto'),
('create_api_integration', 'Criar API para integração')
ON CONFLICT (permission_name) DO NOTHING;

--- role-permissions ---
-- ROLE_USER
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7)
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ROLE_MANAGER
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 8), (2, 9), (2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15), (2, 7)
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 10), (3, 11), (3, 16), (3, 17), (3, 18), (3, 19), (3, 20), (3, 7)
ON CONFLICT (role_id, permission_id) DO NOTHING;

----------------------------------------
----------------------------------------

----- LOGS TABLE -----
CREATE TABLE IF NOT EXISTS audit_log (
    log_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    action_details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN NOT NULL,
    error_message TEXT,

	CONSTRAINT fk_audit_log_users FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);