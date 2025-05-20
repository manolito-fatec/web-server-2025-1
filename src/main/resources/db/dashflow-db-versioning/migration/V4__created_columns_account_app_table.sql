
SET search_path TO dashflow_appl;

ALTER TABLE accounts
ADD COLUMN account_id SERIAL,
ADD COLUMN project VARCHAR(255),
ADD COLUMN role_id INT;

-- Define a nova chave primária
ALTER TABLE accounts
ADD CONSTRAINT pk_account  PRIMARY KEY (account_id);

-- Define a chave estrangeira para role_id
ALTER TABLE accounts
ADD CONSTRAINT fk_role FOREIGN KEY (role_id)
REFERENCES roles(role_id)
ON DELETE CASCADE;