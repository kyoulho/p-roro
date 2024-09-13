-- Delete FILE type 3rd party discovery rules
DELETE FROM THIRD_PARTY_SEARCH_TYPE WHERE SEARCH_TYPE = 'FILE';

-- common code for NGINX
INSERT INTO CODE_DETAIL(DOMAIN_CODE, COMMON_CODE, CODE_NAME_KOREAN, CODE_NAME_ENGLISH, CODE_DISPLAY_NUMBER, CODE_VALID_YN) VALUES (1013,'NGINX','엔진엑스','Nginx',24,'Y');
INSERT INTO CODE_DOMAIN_REFERENCE_DETAIL VALUES (1013, 'NGINX', 1001, 'MW');

-- Add settings property
UPDATE SETTING SET DISPLAY_ORDER = 3 WHERE PROPERTY_NAME = 'appscan.exclude-filenames';
UPDATE SETTING SET DISPLAY_ORDER = 5 WHERE PROPERTY_NAME = 'appscan.remove.files-after-scan';
UPDATE SETTING SET DISPLAY_ORDER = 6 WHERE PROPERTY_NAME = 'appscan.copy.ignore-filenames';
INSERT INTO SETTING (PARENT_SETTING_ID, CATEGORY_NAME, PROPERTY_NAME, PROPERTY_VALUE, READ_ONLY_YN, DATA_TYPE, DATA_VALUES, PLACEHOLDER_ENG, PLACEHOLDER_KOR, TOOLTIP_ENG, TOOLTIP_KOR, DISPLAY_ORDER, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME)
VALUES ((SELECT s.SETTING_ID FROM SETTING s WHERE CATEGORY_NAME = 'Assessment'), 'Application Scan', 'appscan.copy.only-matched-extensions', 'false', 'N', 'Boolean', 'true,false', '', '', 'Choose whether or not to download only files with extensions specified in \'appscan.file-extensions\'. (Not supported on Windows) Optional - Yes, No (default)', '\'appscan.file-extensions\' 에서 지정된 확장자의 파일만 다운로드 할지 여부를 선택합니다. (Windows에서는 지원되지 않음) 선택 항목 - 예, 아니요 (기본값)', 2, 1, now(), 1, now());
INSERT INTO SETTING (PARENT_SETTING_ID, CATEGORY_NAME, PROPERTY_NAME, PROPERTY_VALUE, READ_ONLY_YN, DATA_TYPE, DATA_VALUES, PLACEHOLDER_ENG, PLACEHOLDER_KOR, TOOLTIP_ENG, TOOLTIP_KOR, DISPLAY_ORDER, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME)
VALUES ((SELECT s.SETTING_ID FROM SETTING s WHERE CATEGORY_NAME = 'Assessment'), 'Application Scan', 'appscan.exclude-domains', 'apache.org,w3.org,mvnrepository.com,springframework.org,springmodules.org,mybatis.org,egovframe.go.kr,egovframework.gov,java.sun.com,jcp.org,npmjs.org,yarnpkg.com,mozilla.org', 'N', 'String', null, 'Enter IP or domains to exclude (comma-separated)', '제외할 IP 또는 도메인을 입력 (쉼표로 구분)', 'Enter IP or domains to be excluded from application scanning, separated by commas. Default - apache.org,w3.org,mvnrepository.com,springframework.org,springmodules.org,mybatis.org,egovframe.go.kr,egovframework.gov,java.sun.com,jcp.org,npmjs.org,yarnpkg.com,mozilla.org', '애플리케이션 검사 시 제외할 IP 또는 도메인을 쉼표로 구분하여 입력합니다. 기본값 - apache.org,w3.org,mvnrepository.com,springframework.org,springmodules.org,mybatis.org,egovframe.go.kr,egovframework.gov,java.sun.com,npmjs.org,yarnpkg.com,mozilla.org', 4, 1, now(), 1, now());

-- Delete Nginx for 3rd Party
DELETE FROM THIRD_PARTY_SEARCH_TYPE WHERE THIRD_PARTY_SOLUTION_ID = (SELECT THIRD_PARTY_SOLUTION_ID FROM THIRD_PARTY_SOLUTION WHERE THIRD_PARTY_SOLUTION_NAME = 'Nginx');
DELETE FROM THIRD_PARTY_SOLUTION WHERE THIRD_PARTY_SOLUTION_NAME = 'Nginx';

-- Product lifecycle rules for NGINX
INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Middleware', 'Nginx', 'Nginx, Inc', 'Y');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.8', STR_TO_DATE('2015-04-21', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2016-04-26', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.9', STR_TO_DATE('2015-04-28', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2016-04-26', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.10', STR_TO_DATE('2016-04-26', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2017-04-12', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.11', STR_TO_DATE('2015-05-24', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2017-04-12', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.12', STR_TO_DATE('2017-04-12', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2018-04-17', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.13', STR_TO_DATE('2017-04-25', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2018-04-17', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.14', STR_TO_DATE('2018-04-17', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2019-04-23', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.15', STR_TO_DATE('2018-06-05', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2019-04-23', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.16', STR_TO_DATE('2019-04-23', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2020-04-21', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.17', STR_TO_DATE('2019-05-21', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2020-04-21', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.18', STR_TO_DATE('2020-04-21', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2021-04-20', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.19', STR_TO_DATE('2020-05-26', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2021-04-20', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.20', STR_TO_DATE('2021-04-20', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2022-05-24', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.21', STR_TO_DATE('2021-05-25', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2022-05-24', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.22', STR_TO_DATE('2022-05-24', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2023-04-11', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.23', STR_TO_DATE('2022-06-21', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2023-04-11', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.24', STR_TO_DATE('2023-04-11', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

-- Product lifecycle rules for PostgreSQL
INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Database', 'PostgreSQL', 'PostgreSQL Global Development Group', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6.3', STR_TO_DATE('1998-03-01', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2003-03-01', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6.4', STR_TO_DATE('1998-10-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2003-10-30', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6.5', STR_TO_DATE('1999-06-09', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2004-06-09', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7', STR_TO_DATE('2000-05-08', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2005-05-08', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7.1', STR_TO_DATE('2001-04-13', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2006-04-13', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7.2', STR_TO_DATE('2002-02-04', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2007-02-04', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7.3', STR_TO_DATE('2002-11-27', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2007-11-27', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7.4', STR_TO_DATE('2003-11-17', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2010-10-01', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8', STR_TO_DATE('2005-01-19', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2010-10-01', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8.1', STR_TO_DATE('2005-11-08', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2010-11-08', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8.2', STR_TO_DATE('2006-12-05', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2011-12-05', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8.3', STR_TO_DATE('2008-02-04', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2013-02-07', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8.4', STR_TO_DATE('2009-07-01', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2014-07-24', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9', STR_TO_DATE('2010-09-20', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2015-10-08', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9.1', STR_TO_DATE('2011-09-12', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2016-10-27', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9.2', STR_TO_DATE('2012-09-10', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2017-11-09', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9.3', STR_TO_DATE('2013-09-09', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2018-11-08', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9.4', STR_TO_DATE('2014-12-18', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2020-02-13', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9.5', STR_TO_DATE('2016-01-07', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2021-02-11', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9.6', STR_TO_DATE('2016-09-29', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2021-11-11', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10', STR_TO_DATE('2017-10-05', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2022-11-10', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11', STR_TO_DATE('2018-10-18', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2023-11-09', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12', STR_TO_DATE('2019-10-03', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2024-11-14', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('13', STR_TO_DATE('2020-09-24', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2025-11-13', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('14', STR_TO_DATE('2021-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2026-11-12', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('15', STR_TO_DATE('2022-10-13', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2027-11-11', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

-- Create external_connection table
CREATE TABLE EXTERNAL_CONNECTION
(
	EXTERNAL_CONNECTION_ID BIGINT AUTO_INCREMENT COMMENT '외부연결아이디',
	FILE_NAME            VARCHAR(1024) NULL COMMENT '  파일명',
	PROTOCOL             VARCHAR(20) NULL COMMENT '프로토콜',
	IP                   VARCHAR(1024) NULL COMMENT '아이피',
	PORT                 INTEGER NULL COMMENT '포트',
	APPLICATION_INVENTORY_ID BIGINT NULL COMMENT '어플리케이션_인벤토리_아이디',
	PRIMARY KEY (EXTERNAL_CONNECTION_ID)
);

CREATE INDEX _EXTERNAL_CONNECTION_1 ON EXTERNAL_CONNECTION
(
	APPLICATION_INVENTORY_ID
);

-- Replace view
create or replace view i_app_node
as
/*application for middleware*/
select concat(dim.inventory_type_code,'-', midi.application_instance_id) as id
     , dim.inventory_type_code as type
     , dim.discovered_instance_id as type_id
     , im.inventory_name as name
     , null as service_names
     , im.inventory_detail_type_code as detail_type
     , concat('MW', '-', dima.discovered_instance_id) as parent_id
     , im.project_id
     , dim.discovered_ip_address as ip
     , 'Y' as is_inventory
     , im.inventory_id as engine_id
     , case when plr.solution_name is null then
                case when im.inventory_detail_type_code in ('EAR', 'WAR', 'JAR') then 'Java'
                     else null
                end
            else plr.solution_name
       end as solution_name
     , null as running_status
  from discovered_instance_master dim
  join inventory_master im
    on im.inventory_id = dim.possession_inventory_id
   and dim.inventory_type_code = 'APP'
   and im.inventory_type_code = 'APP'
  join middleware_instance_application_instance midi
    on midi.application_instance_id = dim.discovered_instance_id
  join discovered_instance_master dima
    on dima.discovered_instance_id = midi.middleware_instance_id
  left join inventory_lifecycle_version_link ilvl
    on ilvl.inventory_id = im.inventory_id
  left join product_lifecycle_rules_version plrv
    on plrv.product_lifecycle_rules_version_id = ilvl.java_version_id
  left join product_lifecycle_rules plr
    on plr.product_lifecycle_rules_id = plrv.product_lifecycle_rules_id
 where dim.delete_yn = 'N'
   and im.delete_yn = 'N'
 union all
/*application for standalone*/
select concat(dim.inventory_type_code,'-', dim.discovered_instance_id) as id
     , dim.inventory_type_code as type
     , dim.discovered_instance_id as type_id
     , im.inventory_name as name
     , null as service_names
     , im.inventory_detail_type_code as detail_type
     , concat('SVR', '-', im.server_inventory_id) as parent_id
     , im.project_id
     , dim.discovered_ip_address as ip
     , 'Y' as is_inventory
     , im.inventory_id as engine_id
     , case when plr.solution_name is null then
                case when im.inventory_detail_type_code in ('EAR', 'WAR', 'JAR') then 'Java'
                     else null
                end
            else plr.solution_name
       end as solution_name
     , null as running_status
  from discovered_instance_master dim
  join inventory_master im
    on im.inventory_id = dim.possession_inventory_id
   and dim.inventory_type_code = 'APP'
   and im.inventory_type_code = 'APP'
   and dim.delete_yn = 'N'
   and im.delete_yn = 'N'
  left join middleware_instance_application_instance miai
    on miai.application_instance_id = dim.discovered_instance_id
  left join inventory_lifecycle_version_link ilvl
    on ilvl.inventory_id = im.inventory_id
  left join product_lifecycle_rules_version plrv
    on plrv.product_lifecycle_rules_version_id = ilvl.java_version_id
  left join product_lifecycle_rules plr
    on plr.product_lifecycle_rules_id = plrv.product_lifecycle_rules_id
 where miai.application_instance_id is null;

create or replace view i_database_node
as
/*database for server*/
select concat(im.inventory_type_code,'-', di.database_instance_id) as id
     , im.inventory_type_code as type
     , dim.discovered_instance_id as type_id
     , di.database_service_name as name
     , null as service_namesㅁ
     , null as detail_type
     , concat('SVR', '-', im.server_inventory_id) as parent_id
     , im.project_id
     , dim.discovered_ip_address as ip
     , 'Y' as is_inventory
     , im.inventory_id as engine_id
     , case when plr.solution_name is null then cd.code_name_english
            else plr.solution_name
       end as solution_name
     , null as running_status
  from discovered_instance_master dim
  join inventory_master im
    on im.inventory_id = dim.possession_inventory_id
  join database_instance di
    on di.database_instance_id = dim.discovered_instance_id
  left join inventory_lifecycle_version_link ilvl
    on ilvl.inventory_id = im.inventory_id
  left join product_lifecycle_rules_version plrv
    on plrv.product_lifecycle_rules_version_id = ilvl.product_version_id
  left join product_lifecycle_rules plr
    on plr.product_lifecycle_rules_id = plrv.product_lifecycle_rules_id
  left join code_detail cd
    on cd.common_code = im.inventory_detail_type_code
   and cd.domain_code = 1013
 where dim.inventory_type_code = 'DBMS'
   and dim.delete_yn = 'N'
   and im.delete_yn = 'N';

create or replace view i_discovered_datasource_node
as
/*disc dbms for project(not inventory)*/
select concat('DISC', '-', dim.inventory_type_code,'-', di.database_instance_id) as id
     , dim.inventory_type_code as type
     , dim.discovered_instance_id as type_id
     , concat(dim.discovered_ip_address, '-', dim.discovered_detail_division) as name
     , null as service_names
     , 'DBMS' as detail_type
     , 'ROOT' as parent_id
     , dim.project_id
     , dim.discovered_ip_address as ip
     , 'N' as is_inventory
     , null as engine_id
     , case
          when cd.code_name_english is null then dim.inventory_detail_type_code
          else cd.code_name_english
       end as solution_name
     , null as running_status
  from discovered_instance_master dim
  join database_instance di
    on di.database_instance_id = dim.discovered_instance_id
  left join discovered_instance_master dims
    on dims.discovered_ip_address = dim.discovered_ip_address
   and dims.inventory_type_code = 'SVR'
  left join code_detail cd
    on cd.common_code = dim.inventory_detail_type_code
   and cd.domain_code = 1013
 where dim.inventory_type_code = 'DBMS'
   and dim.delete_yn = 'N'
   and dim.possession_inventory_id is null;

create or replace view i_server_node
as
/*server for service*/
select concat(im.inventory_type_code,'-', im.inventory_id) as id
     , im.inventory_type_code as type
     , im.inventory_id as type_id
     , im.inventory_name as name
     , (select group_concat(svc.service_name separator ', ')
          from service_master svc join service_inventory si on svc.service_id = si.service_id
         where si.inventory_id = im.inventory_id) as service_names
     , 'INV' as detail_type
     , concat('SERV', '-', si.service_id) as parent_id
     , im.project_id
     , sm.representative_ip_address as ip
     , 'Y' as is_inventory
     , null as engine_id
     , case when plr.solution_name is null then ss.os_alias
            else plr.solution_name
       end as solution_name
     , null as running_status
  from inventory_master im
  join service_inventory si
    on si.inventory_id  = im.inventory_id
  join server_master sm
    on sm.server_inventory_id = im.inventory_id
  left join inventory_lifecycle_version_link ilvl
    on ilvl.inventory_id = im.inventory_id
  left join product_lifecycle_rules_version plrv
    on plrv.product_lifecycle_rules_version_id = ilvl.product_version_id
  left join product_lifecycle_rules plr
    on plr.product_lifecycle_rules_id = plrv.product_lifecycle_rules_id
  left join server_summary ss
    on im.inventory_id = ss.server_inventory_id
 where im.inventory_type_code = 'SVR'
   and im.delete_yn = 'N';

create or replace view i_middleware_node
as
/*middleware for server*/
select concat(im.inventory_type_code,'-', mi.middleware_instance_id) as id
     , im.inventory_type_code as type
     , dim.discovered_instance_id as type_id
     , mi.middleware_instance_name as name
     , null as service_names
     , mm.middleware_type_code as detail_type
     , concat('SVR', '-', im.server_inventory_id) as parent_id
     , im.project_id
     , sm.representative_ip_address as ip
     , 'Y' as is_inventory
     , im.inventory_id as engine_id
     , case when plr.solution_name is null then cd.code_name_english
            else plr.solution_name
       end as solution_name
     , case when mi.running_user is null or mi.running_user = '' then 'Stopped'
            else 'Running'
       end as running_status
  from inventory_master im
  join middleware_master mm
    on mm.middleware_inventory_id = im.inventory_id
  join server_master sm
    on sm.server_inventory_id = im.server_inventory_id
  join discovered_instance_master dim
    on dim.possession_inventory_id = im.inventory_id
  join middleware_instance mi
    on mi.middleware_instance_id = dim.discovered_instance_id
  left join inventory_lifecycle_version_link ilvl
    on ilvl.inventory_id = im.inventory_id
  left join product_lifecycle_rules_version plrv
    on plrv.product_lifecycle_rules_version_id = ilvl.product_version_id
  left join product_lifecycle_rules plr
    on plr.product_lifecycle_rules_id = plrv.product_lifecycle_rules_id
  left join code_detail cd
    on cd.common_code = im.inventory_detail_type_code
   and cd.domain_code = 1013
 where im.inventory_type_code = 'MW'
   and im.delete_yn = 'N'
   and dim.delete_yn = 'N';

create or replace view v_inventory_node
as
/*service info*/
select concat('SERV','-', service_id) as id
     , 'SERV' as type
     , service_id as type_id
     , service_name as name
     , null as service_names
     , 'SERV' as detail_type
     , 'ROOT' as parent_id
     , project_id
     , null as ip
     , 'Y' as is_inventory
     , null as engine_id
     , null as solution_name
     , null as running_status
  from service_master
 where delete_yn = 'N'
 union all
select *
  from i_server_node
 union all
select *
  from i_middleware_node
 union all
/*database for server*/
select *
  from i_database_node
 union all
/* all applications */
select *
  from i_app_node;

create or replace view v_ip_name_map
as
  select im.inventory_name as name
       , sm.representative_ip_address as ip_address
       , sm.connection_port as port
       , im.inventory_type_code
       , im.project_id
       , 'Y' as is_inventory
    from server_master sm
    join inventory_master im
      on im.inventory_id = sm.server_inventory_id
     and im.delete_yn = 'N'
  union
  select if(im.inventory_id is null, concat('DISC_', dim.inventory_type_code), im.inventory_name) as name
       , dim.discovered_ip_address as ip_address
       , if(im.inventory_id is null, null, sm.connection_port)  as port
       , if(im.inventory_id is null, dim.inventory_type_code, im.inventory_type_code) as inventory_type_code
       , dim.project_id
       , if(im.inventory_id is null, 'N', 'Y') as is_inventory
    from discovered_instance_master dim
    left join server_master sm
      on sm.server_inventory_id = dim.possession_inventory_id
    left join inventory_master im
      on im.inventory_id = dim.possession_inventory_id
     and im.delete_yn = 'N'
   where dim.inventory_type_code in ('SVR', 'DBMS');

create or replace view v_discovered_node
as
/*disc server for project(not inventory)*/
select concat('DISC', '-', dim.inventory_type_code,'-', dim.discovered_instance_id) as id
     , dim.inventory_type_code as type
     , dim.discovered_instance_id as type_id
     , 'DISC_SVR' as name
     , null as service_names
     , 'SVR' as detail_type
     , 'ROOT' as parent_id
     , dim.project_id
     , dim.discovered_ip_address as ip
     , 'N' as is_inventory
     , null as engine_id
     , null as solution_name
     , null as running_status
  from discovered_instance_master dim
 where dim.inventory_type_code = 'SVR'
   and dim.delete_yn = 'N'
   and dim.possession_inventory_id is null
 union all
select *
  from i_discovered_datasource_node;

-- Create excluded_external_connection table
CREATE TABLE EXCLUDED_EXTERNAL_CONNECTION
(
	PROJECT_ID           BIGINT NOT NULL COMMENT '프로젝_아이디',
	IP                   VARCHAR(512) NOT NULL COMMENT '아이피',
	PRIMARY KEY (PROJECT_ID,IP)
);

CREATE INDEX _EXCLUDED_EXTERNAL_CONNECTION_1 ON EXCLUDED_EXTERNAL_CONNECTION
(
	PROJECT_ID
);