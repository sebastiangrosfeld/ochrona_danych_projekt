
CREATE DATABASE IF NOT EXISTS secure_app;

CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%';
GRANT CREATE USER on *.* to 'admin'@'%';
GRANT GRANT OPTION ON secure_app.* TO 'admin'@'%';
GRANT RELOAD ON *.* TO 'admin'@'%';

CREATE USER IF NOT EXISTS 'config' IDENTIFIED BY 'config';
GRANT SELECT, INSERT, DELETE, UPDATE, CREATE VIEW, DROP ON secure_app.* TO 'config'@'%';

CREATE USER IF NOT EXISTS 'reader'@'%' IDENTIFIED BY 'admin';
GRANT SELECT ON secure_app.* TO 'reader'@'%';
select 'This is a comment' AS '';
select 'This is a comment' AS '';
select 'This is a comment' AS '';
select 'This is a comment' AS '';
FLUSH PRIVILEGES;
