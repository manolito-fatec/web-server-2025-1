ALTER TABLE api5.dashflow_appl.accounts
ADD COLUMN account_id SERIAL,
ADD COLUMN project VARCHAR(255),
ADD COLUMN role_id INT;

-- Define a nova chave primária
ALTER TABLE api5.dashflow_appl.accounts
ADD CONSTRAINT pk_account  PRIMARY KEY (account_id);

-- Define a chave estrangeira para role_id
ALTER TABLE api5.dashflow_appl.accounts
ADD CONSTRAINT fk_role FOREIGN KEY (role_id)
REFERENCES api5.dashflow_appl.roles(role_id)
ON DELETE CASCADE;