INSERT INTO dataflow_appl.tools(
	tool_id, tool_name)
	VALUES (1, 'taiga');

INSERT INTO dataflow_appl.users (username, password, email)
VALUES (
    'Andre',
	'andre',
    'andre.andre@andre.com'
);

INSERT INTO dataflow_appl.users (username, password, email)
VALUES (
    'Bia',
	'bia',
    'bia.bia@bia.com'
);

INSERT INTO dataflow_appl.users (username, password, email)
VALUES (
    'Caue',
	'caue',
    'caue.caue@caue.com'
);

INSERT INTO dataflow_appl.accounts(
	account, user_id, tool_id)
	VALUES ('755290', 1, 1);

INSERT INTO dataflow_appl.accounts(
	account, user_id, tool_id)
	VALUES ('758256', 2, 1);

INSERT INTO dataflow_appl.accounts(
	account, user_id, tool_id)
	VALUES ('754575', 3, 1);
