-- Switch to the dashflow_appl schema (no direct equivalent to SET search_path in Oracle)
-- All table references will use the schema prefix

-- Add new columns to ACCOUNTS table
ALTER TABLE dashflow_appl.accounts
    ADD (
        account_id NUMBER GENERATED ALWAYS AS IDENTITY,
        project VARCHAR2(255),
        role_id NUMBER
        );

-- Create a sequence for the identity column (Oracle 19c handles this automatically with GENERATED ALWAYS AS IDENTITY)

-- Define the new primary key
ALTER TABLE dashflow_appl.accounts
    ADD CONSTRAINT pk_account PRIMARY KEY (account_id);

-- Define the foreign key for role_id
ALTER TABLE dashflow_appl.accounts
    ADD CONSTRAINT fk_account_role FOREIGN KEY (role_id)
        REFERENCES dashflow_appl.roles(role_id)
        ON DELETE CASCADE;

-- Recreate the unique constraint to include the new account_id
-- First drop the existing constraint if it exists
BEGIN
EXECUTE IMMEDIATE 'ALTER TABLE dashflow_appl.accounts DROP CONSTRAINT unique_user_tool';
EXCEPTION
    WHEN OTHERS THEN NULL; -- Ignore if constraint doesn't exist
END;
/

-- Add new unique constraint
ALTER TABLE dashflow_appl.accounts
    ADD CONSTRAINT uk_account_user_tool UNIQUE (user_id, tool_id);