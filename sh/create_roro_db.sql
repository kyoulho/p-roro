CREATE DATABASE rorodb CHARACTER SET utf8 COLLATE utf8_bin;
GRANT ALL PRIVILEGES ON *.* TO playce@'%' IDENTIFIED BY 'playce';
GRANT ALL PRIVILEGES ON *.* TO playce@'localhost' IDENTIFIED BY 'playce';
FLUSH PRIVILEGES;