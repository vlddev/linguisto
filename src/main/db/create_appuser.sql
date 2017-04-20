CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'appuser';

GRANT SELECT, INSERT, UPDATE, DELETE ON linguistodb.* TO 'appuser'@'localhost';
