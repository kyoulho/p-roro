INSERT INTO CODE_DETAIL(DOMAIN_CODE, COMMON_CODE, CODE_NAME_KOREAN, CODE_NAME_ENGLISH, CODE_DISPLAY_NUMBER, CODE_VALID_YN) VALUES (1003,'PC','부분 완료됨','Partially Completed',8,'Y');

INSERT INTO CODE_DOMAIN(DOMAIN_CODE, DOMAIN_NAME_KOREAN, DOMAIN_NAME_ENGLISH, DOMAIN_DESCRIPTION_KOREAN, DOMAIN_DESCRIPTION_ENGLISH, DOMAIN_VALID_YN) VALUES (1201,'3rd Party Solution 탐색 유형','3RD_PARTY_SEARCH_TY_CD','3rd Party Solution 탐색 유형','','Y');
INSERT INTO CODE_DETAIL(DOMAIN_CODE, COMMON_CODE, CODE_NAME_KOREAN, CODE_NAME_ENGLISH, CODE_DISPLAY_NUMBER, CODE_VALID_YN) VALUES (1201,'PROCESS','프로세스','Process',1,'Y');
INSERT INTO CODE_DETAIL(DOMAIN_CODE, COMMON_CODE, CODE_NAME_KOREAN, CODE_NAME_ENGLISH, CODE_DISPLAY_NUMBER, CODE_VALID_YN) VALUES (1201,'PKG','패키지','Package',2,'Y');
INSERT INTO CODE_DETAIL(DOMAIN_CODE, COMMON_CODE, CODE_NAME_KOREAN, CODE_NAME_ENGLISH, CODE_DISPLAY_NUMBER, CODE_VALID_YN) VALUES (1201,'CMD','명령','Command',3,'Y');
INSERT INTO CODE_DETAIL(DOMAIN_CODE, COMMON_CODE, CODE_NAME_KOREAN, CODE_NAME_ENGLISH, CODE_DISPLAY_NUMBER, CODE_VALID_YN) VALUES (1201,'FILE','파일','File',4,'Y');
INSERT INTO CODE_DETAIL(DOMAIN_CODE, COMMON_CODE, CODE_NAME_KOREAN, CODE_NAME_ENGLISH, CODE_DISPLAY_NUMBER, CODE_VALID_YN) VALUES (1201,'PORT','포트','Port',5,'Y');
INSERT INTO CODE_DETAIL(DOMAIN_CODE, COMMON_CODE, CODE_NAME_KOREAN, CODE_NAME_ENGLISH, CODE_DISPLAY_NUMBER, CODE_VALID_YN) VALUES (1201,'CRON','크론탭','Crontab',6,'Y');

DROP TABLE IF EXISTS THIRD_PARTY_SOLUTIO;
DROP TABLE IF EXISTS THIRD_PARTY_SEARCH_TYPE;
DROP TABLE IF EXISTS DISCOVERED_THIRD_PARTY;

CREATE TABLE THIRD_PARTY_SOLUTION (
    THIRD_PARTY_SOLUTION_ID   BIGINT          NOT NULL AUTO_INCREMENT COMMENT '솔루션아이디',
    THIRD_PARTY_SOLUTION_NAME VARCHAR(100)    NOT NULL COMMENT '솔루션이름',
    VENDOR                    VARCHAR(100)    NULL     COMMENT '벤더',
    DESCRIPTION               VARCHAR(1000)   NULL     COMMENT '설명',
    DELETE_YN                 VARCHAR(1)      NOT NULL COMMENT '삭제여부',
    REGIST_USER_ID            BIGINT          NOT NULL COMMENT '등록자',
    REGIST_DATETIME           DATETIME        NOT NULL COMMENT '등록일시',
    MODIFY_USER_ID            BIGINT          NOT NULL COMMENT '수정자',
    MODIFY_DATETIME           DATETIME        NOT NULL COMMENT '수정일시',
    PRIMARY KEY (THIRD_PARTY_SOLUTION_ID)
);

CREATE TABLE THIRD_PARTY_SEARCH_TYPE (
    THIRD_PARTY_SEARCH_TYPE_ID BIGINT       NOT NULL AUTO_INCREMENT COMMENT '서드파티솔루션탐색유형ID',
    THIRD_PARTY_SOLUTION_ID    BIGINT       NOT NULL COMMENT '솔루션아이디',
    SEARCH_TYPE                VARCHAR(8)   NOT NULL COMMENT 'Process, Package, Command, File, Port, Crontab',
    SEARCH_VALUE               VARCHAR(100) NOT NULL COMMENT '검색 값에 , 가 들어가있으면 AND 조건으로 다중 검색을 한다.',
    INVENTORY_TYPE_CODE        VARCHAR(8)   NULL     COMMENT '검색유형이 FILE일 때 미들웨어 or 애플리케이션일 때 쓰인다.',
    WINDOWS_YN                 VARCHAR(1)   NULL     COMMENT '- 검색 유형(SEARCH_TYPE)이 File 일 경우에만 쓰인다.',
    DELETE_YN                  VARCHAR(1)   NOT NULL COMMENT '삭제여부',
    PRIMARY KEY (THIRD_PARTY_SEARCH_TYPE_ID)
);

CREATE TABLE DISCOVERED_THIRD_PARTY (
    DISCOVERED_THIRD_PARTY_ID  BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'DISCOVERED서드파티ID',
    INVENTORY_PROCESS_ID       BIGINT          NOT NULL COMMENT '인벤토리프로세스ID',
    THIRD_PARTY_SEARCH_TYPE_ID BIGINT          NOT NULL COMMENT '서드파티솔루션탐색유형ID',
    FIND_CONTENTS              TEXT            NOT NULL COMMENT '찾은내용',
    PRIMARY KEY (DISCOVERED_THIRD_PARTY_ID)
);

-- Elasticsearch
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Elasticsearch', 'Elasticsearch B.V.', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'java,elasticsearch', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'elasticsearch', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'FILE', '/etc/elasticsearch/elasticsearch.yml', 'SVR', 'N', 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '9200', null, null, 'N');

-- Kibana
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Kibana', 'Elasticsearch B.V.', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'node,kibana', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'kibana', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'FILE', '/etc/kibana/kibana.yml', 'SVR', 'N', 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '5601', null, null, 'N');

-- Redis
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Redis', 'Redis Ltd.', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'redis-server', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'redis', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'redis-cli', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'FILE', '/etc/redis.conf', 'SVR', 'N', 'N');

-- MongoDB
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('MongoDB', 'MongoDB, Inc.', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'mongod', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'FILE', '/etc/mongod.conf', 'SVR', 'N', 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '27017', null, null, 'N');

-- PostgreSQL
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('PostgreSQL', 'The PostgreSQL Global Development Group', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'postgres', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'postgresql', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'postgres', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '5432', null, null, 'N');

-- Cassandra
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Cassandra', 'The Apache Software Foundation', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'cassandra', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'FILE', '/etc/cassandra/cassandra.*', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '7000', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '7001', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '7199', null, null, 'N');

-- Nginx
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Nginx', 'Nginx, Inc.', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nginx', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'nginx', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'FILE', '/etc/nginx/nginx.conf', 'SVR', 'N', 'N');

-- IIS
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('IIS', 'Microsoft', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'w3wp', null, null, 'N');

-- Jennifer
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Jennifer', 'JenniferSoft, Inc.', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'java,jennifer', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'jennifer.config,javaagent', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'FILE', '/jennifer/agent/jennifer.boot.jar', 'SVR', 'N', 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'FILE', '/jennifer/agent/jennifer.conf', 'SVR', 'N', 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'FILE', '/jennifer/agent/jennifer.jar', 'SVR', 'N', 'N');

