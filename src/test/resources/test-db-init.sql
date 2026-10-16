CREATE ROLE gestion_app_test
    WITH
    LOGIN
    PASSWORD 'gestion_test_password'
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOREPLICATION
    NOBYPASSRLS;


GRANT CONNECT
ON DATABASE gestion_microempresas_test
    TO gestion_app_test;


ALTER SCHEMA public
    OWNER TO gestion_app_test;


GRANT ALL
ON SCHEMA public
    TO gestion_app_test;