-- Create Index
create index discovered_port_relation_server_inventory_id_index on discovered_port_relation (server_inventory_id);
create index discovered_port_relation_target_ip_address_index on discovered_port_relation (target_ip_address);
create index discovered_instance_master_discovered_ip_address_index on discovered_instance_master (discovered_ip_address);
create index discovered_instance_master_delete_yn_index on discovered_instance_master (delete_yn);

-- user
INSERT INTO COMPANY_MASTER(COMPANY_CODE, COUNTRY_CODE, COMPANY_STATUS_CODE, COMPANY_NAME_KOREAN, COMPANY_NAME_ENGLISH,
                           REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES('OSCI', 'KR', '001', '오픈소스컨설팅', 'Open Source Consulting Inc.', 1, now(), 1, now());
INSERT INTO USER_MASTER(USER_ID, USER_COMPANY_CODE, USER_NAME_KOREAN, USER_NAME_ENGLISH, USER_EMAIL, REGIST_USER_ID,
                        REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES(1, 'OSCI', '관리자', 'Admin', 'admin@osci.kr', 1, now(), 1, now());
INSERT INTO USER_ACCESS(USER_ID, USER_LOGIN_ID, USER_LOGIN_PASSWORD, USER_STATUS_CODE, TEMP_PASSWORD_YN,
                        USER_PASSWORD_MODIFY_DATETIME, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES(1, 'admin', '{bcrypt}$2a$10$rlv7Nguvq6Tikzer6n3hzOqgN1iAGzEEKS.MHynvsCKqdWynW8xca', '001', 'N', now(), 1, now(), 1, now());


-- default project
INSERT INTO PROJECT_MASTER(PROJECT_ID, PROJECT_NAME, PROJECT_TYPE_CODE, DELETE_YN, DESCRIPTION, REGIST_USER_ID,
                           REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES(1, 'Default Project', '', 'N', '', 1, now(), 1, now());


INSERT INTO SERVICE_MASTER (SERVICE_ID, PROJECT_ID, SERVICE_NAME, BUSINESS_CATEGORY_CODE, BUSINESS_CATEGORY_NAME,
                            CUSTOMER_SERVICE_CODE, CUSTOMER_SERVICE_NAME, MIGRATION_TARGET_YN, MIGRATION_MAN_MONTH,
                            MIGRATION_ENV_CONFIG_START_DATETIME, MIGRATION_ENV_CONFIG_END_DATETIME,
                            MIGRATION_TEST_START_DATETIME, MIGRATION_TEST_END_DATETIME, MIGRATION_CUT_OVER_DATETIME,
                            SEVERITY, DESCRIPTION,
                            DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES(1, 1, 'Default Service', 'DEFAULT-SERV', 'DEFAULT_SERVICE', 'SERV-001', 'DEFAULT_SERVICE', 'N', NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, 'N', 1, now(), 1, now());

commit;

INSERT INTO CODE_DOMAIN(DOMAIN_CODE, DOMAIN_NAME_KOREAN, DOMAIN_NAME_ENGLISH, DOMAIN_DESCRIPTION_KOREAN, DOMAIN_DESCRIPTION_ENGLISH, DOMAIN_VALID_YN) VALUES
  (1001,'인벤토리 유형','INV_TY_CD','SVR / MW / APP / DBMS','','Y'),
  (1002,'인벤토리 수행 유형','INV_PROC_TY_CD','인벤토리에 대한 처리 수행 유형','','Y'),
  (1003,'수행결과코드','INV_PROC_RSLT_CD','인벤토리에 대한 처리 수행 결과 코드','','Y'),
  (1004,'연결방향유형','INV_CONN_PORT_TY_CD','인벤토리에 포트연결방향구분','','Y'),
  (1005,'연결구분','INV_CONN_TY_CD','인벤토리에 연결유형','','Y'),
  (1006,'인벤토리 등록 구분','INV_REG_TY_CD','인벤토리 등록 유형 구분 (INV/DISC)','','Y'),
  (1007,'연결상태유형','CONN_STAT_TY_CD','연결 상태 유형','','Y'),
  (1008,'리소스 유형','RES_TY_CD','리소스 유형','','Y'),
  (1009,'클라우드 유형','CLOUD_TY_CD','클라우드 유형','','Y'),
  (1010,'데몬시작 유형','DAEMON_ST_TY_CD','데몬시작 유청','','Y'),
  (1011,'데몬 상태','DAEMON_STATUS','데몬 상태','','Y'),
  (1012,'인벤토리 업로드 수행 상태','UPLOAD_STATUS_TY_CD','인벤토리 업로드 수행 상태','','Y'),
  (1013,'인벤토리 상세 유형','INV_DTL_TY_CD','인벤토리 상세 유형','','Y'),
  (1101,'DB 연결유형','DB_CONN_TY_CD','DB 연결유형','','Y'),
  (1102,'MW 유형','MW_TY_CD','미들웨어 유형','','Y'),
  (1103,'APP 유형','APP_TY_CD','애플리케이션 유형','','Y'),
  (1104,'업무중요도','SERV_IMPT_TY_CD','업무중요도','','Y'),
  (1105,'하이퍼바이저 유형','HV_TY_CD','하이퍼바이저 유형','','Y'),
  (1106,'이중화 유형','DUAL_TY_CD','이중화 유형','','Y'),
  (1107,'이관 유형','MIG_TY_CD','이관 유형','','Y'),
  (1108,'인스턴스 유형','INST_TY_CD','인스턴스 유형 (엔진/인스턴스)','','Y'),
  (1109,'인터페이스 상세 유형','INST_IF_DTL_TY_CD','인터페이스 상세 유형 , DISCOVERED_인스턴스 : 인벤토리유형 [1001] 에 따라,  - DBMS -> DBLINK,  - MW -> JNDI, CLUSTER,  - APP -> JNDI, JDBC, IF','','Y'),
  (1110,'서버 구분 유형','SVR_USAGE_TY_CD','서버 구분 유형','','Y'),
  (1111,'매니저 타입 유형','MANAGER_TYPE_CODE','매니저 타입 유형','','Y'),
  (1201,'3rd Party Solution 탐색 유형','3RD_PARTY_SEARCH_TY_CD','3rd Party Solution 탐색 유형','','Y'),
  (101,'여부','','','','Y'),
  (102,'상태','USR_STAT_CD','사용자 계정에 대한 상태값','','Y'),
  (401,'국가코드','CNTRY_CD','국가코드','','Y');


INSERT INTO CODE_DETAIL(DOMAIN_CODE, COMMON_CODE, CODE_NAME_KOREAN, CODE_NAME_ENGLISH, CODE_DISPLAY_NUMBER, CODE_VALID_YN) VALUES
  (1001,'SVR','서버','Server',1,'Y'),
  (1001,'MW','미들웨어','Middleware',2,'Y'),
  (1001,'APP','애플리케이션','Application',3,'Y'),
  (1001,'DBMS','데이터베이스','Database',4,'Y'),
  (1002,'PREQ','사전환경분석','Prerequisite',1,'Y'),
  (1002,'SCAN','검사','Scan',2,'Y'),
  (1002,'MIG','마이그레이션','Migration',3,'Y'),
  (1003,'REQ','요청됨','Requested',1,'Y'),
  (1003,'PEND','대기중','Pending',2,'Y'),
  (1003,'PROC','진행중','In-progress',3,'Y'),
  (1003,'CMPL','완료됨','Completed',4,'Y'),
  (1003,'CNCL','취소됨','Cancelled',5,'Y'),
  (1003,'FAIL','실패됨','Failed',6,'Y'),
  (1003,'NS','지원되지 않음','Not Supported',7,'Y'),
  (1003,'PC','부분 완료됨','Partially Completed',8,'Y'),
  (1004,'LISTEN','연결대기중','Listening',1,'Y'),
  (1004,'INB','인바운드','InBound',2,'Y'),
  (1004,'OUTB','아웃바운드','OutBound',3,'Y'),
  (1005,'REAL','실재','Real',1,'Y'),
  (1005,'VR','가상','Virtual',2,'Y'),
  (1006,'INV','인벤토리','Inventory',1,'Y'),
  (1006,'DISC','탐색됨','Discovered',2,'Y'),
  (1007,'LSN','리슨','Listen',1,'Y'),
  (1007,'EST','연결됨','Established',2,'Y'),
  (1007,'ETC','기타','Etc',3,'Y'),
  (1008,'SERV','서비스','Service',1,'Y'),
  (1008,'SVR','서버','Server',2,'Y'),
  (1009,'AWS','AWS','AWS',1,'Y'),
  (1009,'GCP','GCP','GCP',2,'Y'),
  (1009,'SVR','서버','Server',3,'Y'),
  (1009,'DBMS','DBMS','DBMS',4,'Y'),
  (1010,'AUTO','자동','Automatic',1,'Y'),
  (1010,'MAN','수동','Manual',2,'Y'),
  (1010,'DIS','비활성','Disabled',3,'Y'),
  (1011,'RUN','실행중','Running',1,'Y'),
  (1011,'STOP','중지','Stopped',2,'Y'),
  (1012,'SUCC','성공됨','Succeeded',1,'Y'),
  (1012,'FAIL','실패됨','Failed',2,'Y'),
  (1013,'LINUX','리눅스','Linux',1,'Y'),
  (1013,'AIX','AIX','AIX',2,'Y'),
  (1013,'SUNOS','Solaris','Solaris',3,'Y'),
  (1013,'HP_UX','HP UX','HP UX',4,'Y'),
  (1013,'WINDOWS','윈도우','Windows',5,'Y'),
  (1013,'ORACLE','오라클','Oracle',6,'Y'),
  (1013,'MYSQL','MySQL','MySQL',7,'Y'),
  (1013,'MARIADB','MariaDB','MariaDB',8,'Y'),
  (1013,'POSTGRE','PostgreSQL','PostgreSQL',9,'Y'),
  (1013,'MSSQL','MSSQL','MSSQL',10,'Y'),
  (1013,'TIBERO','티베로','Tibero',11,'Y'),
  (1013,'SYBASE','Sybase','Sybase',12,'Y'),
  (1013,'EAR','EAR','EAR',13,'Y'),
  (1013,'WAR','WAR','WAR',14,'Y'),
  (1013,'JAR','JAR','JAR',15,'Y'),
  (1013,'WSPHERE','웹스피어','Websphere',16,'Y'),
  (1013,'WEBLOGIC','웹로직','WebLogic',17,'Y'),
  (1013,'TOMCAT','톰캣','Tomcat',18,'Y'),
  (1013,'JEUS','제우스','Jeus',19,'Y'),
  (1013,'APACHE','아파치','Apache',20,'Y'),
  (1013,'WEBTOB','웹투비','WebToB',21,'Y'),
  (1013,'JBOSS','제이보스','JBoss',23,'Y'),
  (1013,'NGINX','엔진엑스','Nginx',24,'Y'),
  (1013,'ETC','기타','Etc',22,'Y'),
  (1101,'JDBC','JDBC','JDBC',1,'Y'),
  (1101,'JNDI','JNDI','JNDI',2,'Y'),
  (1101,'DBCP','DBCP','DBCP',3,'Y'),
  (1101,'NATIVE','Native','Native',4,'Y'),
  (1101,'ETC','기타','Etc',5,'Y'),
  (1102,'WEB','WEB','WEB',1,'Y'),
  (1102,'WAS','WAS','WAS',2,'Y'),
  (1102,'CACHE','캐시','CACHE',3,'N'),
  (1102,'MSG','메시징','MESSAGING',4,'N'),
  (1102,'MONTRG','모니터링','MONITORING',5,'N'),
  (1102,'EAI','EAI','EAI',6,'N'),
  (1102,'ESB','ESB','ESB',7,'N'),
  (1102,'ETL','ETL','ETL',8,'N'),
  (1102,'ETC','ETC','ETC',9,'Y'),
  (1103,'EAR','자바 엔터프라이즈 애플리케이션','Java Enterprise Application',1,'Y'),
  (1103,'WAR','자바 웹 애플리케이션','Java Web Application',2,'Y'),
  (1103,'JAR','자바 애플리케이션','Java Application',3,'Y'),
  (1103,'ETC','기타','Etc',4,'Y'),
  (1105,'AWS','AWS','AWS',1,'Y'),
  (1105,'AZURE','Azure','Azure',2,'Y'),
  (1105,'BM','Baremetal','Baremetal',3,'Y'),
  (1105,'GCP','GCP','GCP',4,'Y'),
  (1105,'KVM','KVM','KVM',5,'Y'),
  (1105,'OS','OpenStack','OpenStack',6,'Y'),
  (1105,'VMWARE','VMWare','VMWare',7,'Y'),
  (1105,'ETC','Etc','Etc',8,'Y'),
  (1106,'SINGLE','단독','Single',1,'Y'),
  (1106,'AA','액티브 액티브','Active Active',2,'Y'),
  (1106,'AS','액티브 스탠바이','Active Standby',3,'Y'),
  (1108,'ENG','엔진','Engine',1,'Y'),
  (1108,'INST','인스턴스','Instance',2,'Y'),
  (1107,'RH','리호스트','Rehost',1,'Y'),
  (1107,'RP','리플랫폼','Replatform',2,'Y'),
  (1107,'RF','리펙터','Refactor',3,'Y'),
  (1107,'RA','리아키텍트','Rearchitect',4,'Y'),
  (1107,'RTE','리타이어','Retire',5,'Y'),
  (1107,'RTN','리테인','Retain',6,'Y'),
  (1109,'DBLINK','DB 링크','DB link',1,'Y'),
  (1109,'JNDI','JNDI','JNDI',2,'Y'),
  (1109,'CLUSTER','클러스터','Cluster',3,'Y'),
  (1109,'JDBC','JDBC','JDBC',4,'Y'),
  (1109,'IF','인터페이스','Interface',5,'Y'),
  (1110,'DEV','개발','Development',1,'Y'),
  (1110,'TEST','테스트','Test',2,'Y'),
  (1110,'STG','스테이지','Stage',3,'Y'),
  (1110,'PRD','운영','Production',4,'Y'),
  (1110,'DR','재해복구','Disaster Recovery',5,'Y'),
  (1110,'ETC','기타','Etc',6,'Y'),
  (1111,'DEVELOP','개발','Development',1,'Y'),
  (1111,'MAINT','유지보수','Maintenance',2,'Y'),
  (1111,'OP','운영','Operation',3,'Y'),
  (1111,'DEPLOY','배포','Deployment',4,'Y'),
  (1201,'PROCESS','프로세스','Process',1,'Y'),
  (1201,'RUNUSER','프로세스 실행 사용자','Process Runtime User',2,'Y'),
  (1201,'PKG','패키지','Package',3,'Y'),
  (1201,'SVC','서비스','Service',4,'Y'),
  (1201,'CMD','명령어','Command',5,'Y'),
  (1201,'PORT','포트','Port',6,'Y'),
  (1201,'SCHEDULE','스케쥴','Schedule',7,'Y'),
  (401,'AD','Andorra','Andorra',1,'Y'),
  (401,'AE','United Arab Emi','United Arab Emi',2,'Y'),
  (401,'AF','Afghanistan','Afghanistan',3,'Y'),
  (401,'AG','Antigua & Barbu','Antigua & Barbu',4,'Y'),
  (401,'AI','Anguilla','Anguilla',5,'Y'),
  (401,'AL','Albania','Albania',6,'Y'),
  (401,'AM','Armenia','Armenia',7,'Y'),
  (401,'AN','Netherlands Ant','Netherlands Ant',8,'Y'),
  (401,'AO','Angola','Angola',9,'Y'),
  (401,'AQ','Antarctica','Antarctica',10,'Y'),
  (401,'AR','Argentina','Argentina',11,'Y'),
  (401,'AS','American Somoa','American Somoa',12,'Y'),
  (401,'AT','Austria','Austria',13,'Y'),
  (401,'AU','Australia','Australia',14,'Y'),
  (401,'AW','Aruba','Aruba',15,'Y'),
  (401,'AZ','Azerbaijan','Azerbaijan',16,'Y'),
  (401,'BA','Bosnia-Hercegov','Bosnia-Hercegov',17,'Y'),
  (401,'BB','Barbados','Barbados',19,'Y'),
  (401,'BD','Bangladesh','Bangladesh',20,'Y'),
  (401,'BE','Belgium','Belgium',21,'Y'),
  (401,'BF','Burkina Faso','Burkina Faso',22,'Y'),
  (401,'BG','Bulgaria','Bulgaria',23,'Y'),
  (401,'BH','Bahrain','Bahrain',24,'Y'),
  (401,'BI','Burundi','Burundi',25,'Y'),
  (401,'BJ','Benin','Benin',26,'Y'),
  (401,'BM','Bermuda','Bermuda',27,'Y'),
  (401,'BN','Brunei Darussal','Brunei Darussal',28,'Y'),
  (401,'BO','Bolivia','Bolivia',29,'Y'),
  (401,'BR','Brazil','Brazil',30,'Y'),
  (401,'BS','Bahamas','Bahamas',31,'Y'),
  (401,'BT','Bhutan','Bhutan',32,'Y'),
  (401,'BV','Bouvet Island','Bouvet Island',33,'Y'),
  (401,'BW','Botswana','Botswana',34,'Y'),
  (401,'BY','Belarus','Belarus',35,'Y'),
  (401,'BZ','Belize','Belize',36,'Y'),
  (401,'CA','Canada','Canada',37,'Y'),
  (401,'CC','Cocos Islands','Cocos Islands',38,'Y'),
  (401,'CD','Democratic Repu','Democratic Repu',39,'Y'),
  (401,'CF','Central African','Central African',40,'Y'),
  (401,'CG','Congo','Congo',41,'Y'),
  (401,'CH','Switzerland','Switzerland',42,'Y'),
  (401,'CI','Ivory Coast','Ivory Coast',43,'Y'),
  (401,'CK','Cook Islands','Cook Islands',44,'Y'),
  (401,'CL','Chile','Chile',45,'Y'),
  (401,'CM','Cameroon','Cameroon',46,'Y'),
  (401,'CN','CHINA','CHINA',48,'Y'),
  (401,'CO','Colombia','Colombia',49,'Y'),
  (401,'CR','Costa Rica','Costa Rica',50,'Y'),
  (401,'CU','Cuba','Cuba',51,'Y'),
  (401,'CV','Cape Verde','Cape Verde',52,'Y'),
  (401,'CX','Christmas Islan','Christmas Islan',53,'Y'),
  (401,'CY','Cyprus','Cyprus',54,'Y'),
  (401,'CZ','Czech Republic','Czech Republic',55,'Y'),
  (401,'DE','Germany Fed Rep','Germany Fed Rep',56,'Y'),
  (401,'DJ','Djibouti','Djibouti',57,'Y'),
  (401,'DK','Denmark','Denmark',58,'Y'),
  (401,'DM','Dominica','Dominica',59,'Y'),
  (401,'DO','Dominican Repub','Dominican Repub',60,'Y'),
  (401,'DZ','Algeria','Algeria',61,'Y'),
  (401,'EC','Ecuador','Ecuador',62,'Y'),
  (401,'EE','Estonia','Estonia',63,'Y'),
  (401,'EG','Egypt','Egypt',64,'Y'),
  (401,'EH','Western Sahara','Western Sahara',65,'Y'),
  (401,'ER','Eritrea','Eritrea',66,'Y'),
  (401,'ES','Spain','Spain',67,'Y'),
  (401,'ET','Ethiopia','Ethiopia',68,'Y'),
  (401,'FI','Finland','Finland',69,'Y'),
  (401,'FJ','Fiji','Fiji',70,'Y'),
  (401,'FK','Falkland Island','Falkland Island',71,'Y'),
  (401,'FM','Micronesia Fed','Micronesia Fed',72,'Y'),
  (401,'FO','Faroe Islands','Faroe Islands',73,'Y'),
  (401,'FR','France','France',74,'Y'),
  (401,'FX','France Metropol','France Metropol',75,'Y'),
  (401,'GA','Gabon','Gabon',76,'Y'),
  (401,'GB','United Kingdom','United Kingdom',77,'Y'),
  (401,'GD','Grenada','Grenada',78,'Y'),
  (401,'GE','Georgia','Georgia',79,'Y'),
  (401,'GF','French Guiana','French Guiana',80,'Y'),
  (401,'GH','Ghana','Ghana',81,'Y'),
  (401,'GI','Gibraltar','Gibraltar',82,'Y'),
  (401,'GL','Greenland','Greenland',83,'Y'),
  (401,'GM','Gambia The','Gambia The',84,'Y'),
  (401,'GN','Guinea','Guinea',85,'Y'),
  (401,'GP','Guadeloupe','Guadeloupe',86,'Y'),
  (401,'GQ','Equatorial Guin','Equatorial Guin',87,'Y'),
  (401,'GR','Greece','Greece',88,'Y'),
  (401,'GS','Southern Georgi','Southern Georgi',89,'Y'),
  (401,'GT','Guatemala','Guatemala',90,'Y'),
  (401,'GU','Guam','Guam',91,'Y'),
  (401,'GW','Guinea-Bissau','Guinea-Bissau',92,'Y'),
  (401,'GY','Guyana','Guyana',93,'Y'),
  (401,'GZ','Gaza Strip','Gaza Strip',94,'Y'),
  (401,'HK','HONG KONG','HONG KONG',95,'Y'),
  (401,'HM','Heard & Mcdonal','Heard & Mcdonal',96,'Y'),
  (401,'HN','Honduras','Honduras',97,'Y'),
  (401,'HR','Croatia','Croatia',98,'Y'),
  (401,'HT','HAITI','HAITI',99,'Y'),
  (401,'HU','Hungary','Hungary',100,'Y'),
  (401,'ID','Indonesia','Indonesia',101,'Y'),
  (401,'IE','Ireland','Ireland',102,'Y'),
  (401,'IL','Israel','Israel',103,'Y'),
  (401,'IN','India','India',104,'Y'),
  (401,'IO','British Indian','British Indian',105,'Y'),
  (401,'IQ','Iraq','Iraq',106,'Y'),
  (401,'IR','Iran','Iran',107,'Y'),
  (401,'IS','Iceland','Iceland',108,'Y'),
  (401,'IT','Italy','Italy',109,'Y'),
  (401,'JM','Jamaica','Jamaica',110,'Y'),
  (401,'JO','Jordan','Jordan',111,'Y'),
  (401,'JP','JAPAN','JAPAN',112,'Y'),
  (401,'KE','Kenya','Kenya',113,'Y'),
  (401,'KG','Kyrgyzstan','Kyrgyzstan',114,'Y'),
  (401,'KH','Cambodia','Cambodia',115,'Y'),
  (401,'KI','Kiribati','Kiribati',116,'Y'),
  (401,'KM','Comoros','Comoros',117,'Y'),
  (401,'KN','St. Kitts & Nev','St. Kitts & Nev',118,'Y'),
  (401,'KP','Korea Democrati','Korea Democrati',119,'Y'),
  (401,'KR','KOREA','KOREA',120,'Y'),
  (401,'KW','Kuwait','Kuwait',121,'Y'),
  (401,'KY','Cayman Islands','Cayman Islands',122,'Y'),
  (401,'KZ','Kazakhstan','Kazakhstan',123,'Y'),
  (401,'LA','Lao Peoples'' De','Lao Peoples'' De',124,'Y'),
  (401,'LB','Lebanon','Lebanon',125,'Y'),
  (401,'LC','St. Lucia','St. Lucia',126,'Y'),
  (401,'LI','Liechtenstein','Liechtenstein',127,'Y'),
  (401,'LK','Sri Lanka','Sri Lanka',128,'Y'),
  (401,'LR','Liberia','Liberia',129,'Y'),
  (401,'LS','Lesotho','Lesotho',130,'Y'),
  (401,'LT','Lithuania','Lithuania',131,'Y'),
  (401,'LU','Luxembourg','Luxembourg',132,'Y'),
  (401,'LV','Latvia','Latvia',133,'Y'),
  (401,'LY','Libya','Libya',134,'Y'),
  (401,'MA','Morocco','Morocco',135,'Y'),
  (401,'MC','Monaco','Monaco',136,'Y'),
  (401,'MD','Moldova','Moldova',137,'Y'),
  (401,'MG','Madagascar','Madagascar',138,'Y'),
  (401,'MH','Marshall Island','Marshall Island',139,'Y'),
  (401,'MK','Macedonia (Skop','Macedonia (Skop',140,'Y'),
  (401,'ML','Mali','Mali',141,'Y'),
  (401,'MM','Burma (Myanmar)','Burma (Myanmar)',142,'Y'),
  (401,'MN','Mongolia','Mongolia',143,'Y'),
  (401,'MO','Macau','Macau',144,'Y'),
  (401,'MP','Northern Marian','Northern Marian',145,'Y'),
  (401,'MQ','Martinique','Martinique',146,'Y'),
  (401,'MR','Mauritania','Mauritania',147,'Y'),
  (401,'MS','Montserrat','Montserrat',148,'Y'),
  (401,'MT','Malta','Malta',149,'Y'),
  (401,'MU','Mauritius','Mauritius',150,'Y'),
  (401,'MV','Maldives','Maldives',151,'Y'),
  (401,'MW','Malawi','Malawi',152,'Y'),
  (401,'MX','Mexico','Mexico',153,'Y'),
  (401,'MY','Malaysia','Malaysia',154,'Y'),
  (401,'MZ','Mozambique','Mozambique',155,'Y'),
  (401,'NA','Namibia','Namibia',156,'Y'),
  (401,'NC','New Caledonia','New Caledonia',157,'Y'),
  (401,'NE','Niger','Niger',158,'Y'),
  (401,'NF','Norfolk Island','Norfolk Island',159,'Y'),
  (401,'NG','Nigeria','Nigeria',160,'Y'),
  (401,'NI','Nicaragua','Nicaragua',161,'Y'),
  (401,'NL','Netherlands','Netherlands',162,'Y'),
  (401,'NO','Norway','Norway',163,'Y'),
  (401,'NP','Nepal','Nepal',164,'Y'),
  (401,'NR','Nauru','Nauru',165,'Y'),
  (401,'NT','Iraq S.Arab Nz','Iraq S.Arab Nz',166,'Y'),
  (401,'NU','Niue','Niue',167,'Y'),
  (401,'NZ','New Zealand','New Zealand',168,'Y'),
  (401,'OM','Oman','Oman',169,'Y'),
  (401,'PA','Panama','Panama',170,'Y'),
  (401,'PE','Peru','Peru',171,'Y'),
  (401,'PF','French Polynesi','French Polynesi',172,'Y'),
  (401,'PG','Papua New Guine','Papua New Guine',173,'Y'),
  (401,'PH','Philippines','Philippines',174,'Y'),
  (401,'PK','Pakistan','Pakistan',175,'Y'),
  (401,'PL','Poland','Poland',176,'Y'),
  (401,'PM','St.Pierre & Miq','St.Pierre & Miq',177,'Y'),
  (401,'PN','Pitcairn Island','Pitcairn Island',178,'Y'),
  (401,'PR','Puerto Rico','Puerto Rico',179,'Y'),
  (401,'PT','Portugal','Portugal',180,'Y'),
  (401,'PW','Palau','Palau',181,'Y'),
  (401,'PY','Paraguay','Paraguay',182,'Y'),
  (401,'QA','Qatar','Qatar',183,'Y'),
  (401,'RE','Reunion','Reunion',184,'Y'),
  (401,'RO','Romania','Romania',185,'Y'),
  (401,'RU','Russian Federat','Russian Federat',186,'Y'),
  (401,'RW','Rwanda','Rwanda',187,'Y'),
  (401,'SA','Saudi Arabia','Saudi Arabia',188,'Y'),
  (401,'SB','Solomon Islands','Solomon Islands',189,'Y'),
  (401,'SC','Seychelles','Seychelles',190,'Y'),
  (401,'SD','Sudan','Sudan',191,'Y'),
  (401,'SE','Sweden','Sweden',192,'Y'),
  (401,'SG','SINGAPORE','SINGAPORE',193,'Y'),
  (401,'SH','St. Helena','St. Helena',194,'Y'),
  (401,'SI','Slovenia','Slovenia',195,'Y'),
  (401,'SJ','Svalbard & Jan','Svalbard & Jan',196,'Y'),
  (401,'SK','Slovakia','Slovakia',197,'Y'),
  (401,'SL','Sierra Leone','Sierra Leone',198,'Y'),
  (401,'SM','San Marino','San Marino',199,'Y'),
  (401,'SN','Senegal','Senegal',200,'Y'),
  (401,'SO','Somalia','Somalia',201,'Y'),
  (401,'SR','Suriname','Suriname',202,'Y'),
  (401,'ST','Sao Tome & Prin','Sao Tome & Prin',203,'Y'),
  (401,'SV','El Salvador','El Salvador',204,'Y'),
  (401,'SY','Syrian Arab Rep','Syrian Arab Rep',205,'Y'),
  (401,'SZ','Swaziland','Swaziland',206,'Y'),
  (401,'TC','Turks & Caicos','Turks & Caicos',207,'Y'),
  (401,'TD','Chad','Chad',208,'Y'),
  (401,'TF','Fr Southern Ter','Fr Southern Ter',209,'Y'),
  (401,'TG','Togo','Togo',210,'Y'),
  (401,'TH','Thailand','Thailand',211,'Y'),
  (401,'TJ','Tajikistan','Tajikistan',212,'Y'),
  (401,'TK','Tokelau','Tokelau',213,'Y'),
  (401,'TM','Turkmenistan','Turkmenistan',214,'Y'),
  (401,'TN','Tunisia','Tunisia',215,'Y'),
  (401,'TO','Tonga','Tonga',216,'Y'),
  (401,'TP','East Timor','East Timor',217,'Y'),
  (401,'TR','Turkey','Turkey',218,'Y'),
  (401,'TT','Trinidad & Toba','Trinidad & Toba',219,'Y'),
  (401,'TV','Tuvalu','Tuvalu',220,'Y'),
  (401,'TW','TAIWAN','TAIWAN',221,'Y'),
  (401,'TZ','Tanzania','Tanzania',222,'Y'),
  (401,'UA','Ukraine','Ukraine',223,'Y'),
  (401,'UG','Uganda','Uganda',224,'Y'),
  (401,'UM','US Minor Outlyi','US Minor Outlyi',225,'Y'),
  (401,'US','UNITED STATES','UNITED STATES',226,'Y'),
  (401,'UY','Uruguay','Uruguay',227,'Y'),
  (401,'UZ','Uzbekistan','Uzbekistan',228,'Y'),
  (401,'VA','Vatican City St','Vatican City St',229,'Y'),
  (401,'VC','St.Vincent&Gren','St.Vincent&Gren',230,'Y'),
  (401,'VE','Venezuela','Venezuela',231,'Y'),
  (401,'VG','British Virgin','British Virgin',232,'Y'),
  (401,'VI','Virgin Islands','Virgin Islands',233,'Y'),
  (401,'VN','Vietnam','Vietnam',234,'Y'),
  (401,'VU','Vanuatu','Vanuatu',235,'Y'),
  (401,'WE','West Bank','West Bank',236,'Y'),
  (401,'WF','Wallis & Futuna','Wallis & Futuna',237,'Y'),
  (401,'WS','Samoa','Samoa',238,'Y'),
  (401,'YE','Yemen Republic','Yemen Republic',239,'Y'),
  (401,'YT','Mayotte','Mayotte',240,'Y'),
  (401,'YU','Yugoslavia','Yugoslavia',241,'Y'),
  (401,'ZA','South Africa','South Africa',242,'Y'),
  (401,'ZM','Zambia','Zambia',243,'Y'),
  (401,'ZR','Zaire','Zaire',244,'Y'),
  (401,'ZW','Zimbabwe','Zimbabwe',245,'Y'),
  (401,'n/a','Unkown','Unkown',999,'Y'),
  (101,'Y','Y','Y',1,'Y'),
  (101,'N','N','N',2,'Y'),
  (102,'001','활성','Active',1,'Y'),
  (102,'009','비활성','Inactive',9,'Y'),
  (102,'999','삭제','Deleted',999,'N');


INSERT INTO CODE_DOMAIN_REFERENCE_DETAIL VALUES
  (1013, 'AIX', 1001, 'SVR'),
  (1013, 'HP_UX', 1001, 'SVR'),
  (1013, 'LINUX', 1001, 'SVR'),
  (1013, 'SUNOS', 1001, 'SVR'),
  (1013, 'WINDOWS', 1001, 'SVR'),
  (1013, 'APACHE', 1001, 'MW'),
  (1013, 'JEUS', 1001, 'MW'),
  (1013, 'TOMCAT', 1001, 'MW'),
  (1013, 'WEBLOGIC', 1001, 'MW'),
  (1013, 'WEBTOB', 1001, 'MW'),
  (1013, 'WSPHERE', 1001, 'MW'),
  (1013, 'JBOSS', 1001, 'MW'),
  (1013, 'NGINX', 1001, 'MW'),
  (1013, 'MARIADB', 1001, 'DBMS'),
  (1013, 'MSSQL', 1001, 'DBMS'),
  (1013, 'MYSQL', 1001, 'DBMS'),
  (1013, 'ORACLE', 1001, 'DBMS'),
  (1013, 'POSTGRE', 1001, 'DBMS'),
  (1013, 'SYBASE', 1001, 'DBMS'),
  (1013, 'TIBERO', 1001, 'DBMS'),
  (1013, 'EAR', 1001, 'APP'),
  (1013, 'ETC', 1001, 'APP'),
  (1013, 'JAR', 1001, 'APP'),
  (1013, 'WAR', 1001, 'APP');


-- survey
INSERT INTO SURVEY (SURVEY_NAME_ENGLISH, SURVEY_NAME_KOREAN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES
('Cloud Readiness Survey', '클라우드 전환 진단 평가', 1, now(), 1, now());

INSERT INTO SURVEY_CATEGORY (PARENT_SURVEY_CATEGORY_ID, CATEGORY_STEP, CATEGORY_NAME_ENGLISH, CATEGORY_NAME_KOREAN, EVALUATION_ITEM_ENGLISH, EVALUATION_ITEM_KOREAN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES
(null, 1, 'Business Factors', '업무적 관점', null, null, 1, now(), 1, now()),
(1, null, null, null, 'Business Relevance', '업무 중요도', 1, now(), 1, now()),
(1, null, null, null, 'Scale of Service', '시스템 규모', 1, now(), 1, now()),
(1, null, null, null, 'Target of Service', '서비스 대상', 1, now(), 1, now()),
(1, null, null, null, 'Elasticity of Load', '부하의 탄력성', 1, now(), 1, now()),
(1, null, null, null, 'Business Requirements', '비즈니스 요구', 1, now(), 1, now()),
(null, 2, 'Technical Factors', '기술적 관점', null, null, 1, now(), 1, now()),
(7, null, null, null, 'Usage of Resources', '자원 사용률', 1, now(), 1, now()),
(7, null, null, null, 'Ageing of Resources', '자원 노후화', 1, now(), 1, now()),
(7, null, null, null, 'Number of Systems Interfaced', '연계 시스템 수', 1, now(), 1, now()),
(7, null, null, null, 'Language', '사용 언어', 1, now(), 1, now()),
(7, null, null, null, 'Framework', '프레임워크', 1, now(), 1, now()),
(7, null, null, null, 'System Architecture', '시스템 구조', 1, now(), 1, now()),
(7, null, null, null, 'OS', '사용 OS', 1, now(), 1, now()),
(7, null, null, null, 'Virtualization', '가상화 적용 여부', 1, now(), 1, now()),
(7, null, null, null, 'Business Requirements for Cloud Adoption', '클라우드 전환 비즈니스 요구', 1, now(), 1, now());

INSERT INTO QUESTION (SURVEY_CATEGORY_ID, QUESTION_CONTENT_ENGLISH, QUESTION_CONTENT_KOREAN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES
(2, 'How relevant is the service to business continuity? ', '해당 서비스의 중요도가 비즈니스 연속성 측면에서 어느 수준입니까?', 1, now(), 1, now()),
(3, 'How large is the scale of the system? (Based on the Function Point (FP) or the amount of investment)', '시스템의 규모는 어느 정도 수준입니까? (기능점수 (FP) 또는 투자 금액 기준)', 1, now(), 1, now()),
(4, 'Who are major users of the service?', '해당 서비스의 주 사용자는 누구입니까?', 1, now(), 1, now()),
(5, 'Are system demands concentrated (doubling the average or more) at a specific point of time (a specific month or season) according to the nature of the business?', '비즈니스 특성 상 특정 시기 (월, 계절) 에 시스템 수요가 집중됩니까? (평균값의 2배 이상)', 1, now(), 1, now()),
(6, 'Is the service prioritized to move on to the cloud as part of corporate business goals or strategies? ', '해당 서비스는 기업의 비즈니스 목표나 전략으로 클라우드 전환이 우선시 되고 있습니까?', 1, now(), 1, now()),
(8, 'How high is utilization of the system CPU?', '시스템의 CPU 사용률이 어떠합니까?', 1, now(), 1, now()),
(8, 'How high is usage of the system storage?', '시스템의 스토리지 사용량이 어떠합니까?', 1, now(), 1, now()),
(9, 'When was the hardware deployed?', '하드웨어를 도입한지 얼마나 되었습니까?', 1, now(), 1, now()),
(10, 'How many systems are interfaced with this system?', '해당 시스템과 연결된 유관 시스템은 몇 개입니까?', 1, now(), 1, now()),
(11, 'In which language has the system been developed?', '시스템의 개발 언어는 무엇입니까?', 1, now(), 1, now()),
(12, 'Which framework is the application using? ', '애플리케이션에서 사용하고 있는 프레임워크는 무엇인가요? ', 1, now(), 1, now()),
(13, 'What is the basic architecture of the system?', '시스템의 기본적인 아키텍처 구조가 어떻게 되어 있습니까?', 1, now(), 1, now()),
(14, 'Is system Unix, Linux or an appliance (hardware-combined)?', '시스템이 유닉스, 리눅스, 또는 어플라이언스 (하드웨어 일체형) 중 무엇에 해당됩니까? ', 1, now(), 1, now()),
(15, 'Is the system virtualized (VMware, etc.)?', '시스템에 가상화 (VM웨어 등) 가 적용되어 있습니까? ', 1, now(), 1, now()),
(16, 'How highly is the service required to move to the cloud technically?', '해당 서비스의 클라우드로 전환 필요성이 기술적인 측면에서 높은 편입니까?', 1, now(), 1, now());

INSERT INTO SURVEY_QUESTION (SURVEY_ID, QUESTION_ID, DISPLAY_ORDER, WEIGHT) VALUES
(1, 1, 1, 0.25),
(1, 2, 2, 0.25),
(1, 3, 3, 0.1),
(1, 4, 4, 0.15),
(1, 5, 5, 0.25),
(1, 6, 6, 0.05),
(1, 7, 7, 0.05),
(1, 8, 8, 0.1),
(1, 9, 9, 0.15),
(1, 10, 10, 0.07),
(1, 11, 11, 0.08),
(1, 12, 12, 0.1),
(1, 13, 13, 0.15),
(1, 14, 14, 0.1),
(1, 15, 15, 0.15);

INSERT INTO ANSWER (ANSWER_CONTENT_ENGLISH,ANSWER_CONTENT_KOREAN,REGIST_USER_ID,REGIST_DATETIME,MODIFY_USER_ID,MODIFY_DATETIME) VALUES
('Grade A – Very relevant','A등급 - 매우 중요함',1,now(),1,now()),
('Grade B - Relevant','B등급 - 중요함',1,now(),1,now()),
('Grade C - Average','C등급 - 보통',1,now(),1,now()),
('Grade D – Not so relevant','D등급 - 중요하지 않음',1,now(),1,now()),
('Grade E – Not relevant at all','E등급 - 전혀 중요하지 않음',1,now(),1,now()),
('Large – The system for redundancy, DR, clustering, backup and recovery has been completed.','대규모 - 이중화, DR, 클러스터, 백업/복구 체계가 구축됨',1,now(),1,now()),
('Medium','중규모',1,now(),1,now()),
('Small – Only administrators are in place and system updates are rarely made.','소규모 - 운영 담당자만 존재하고 시스템 업데이트가 거의 없음',1,now(),1,now()),
('Interface systems such as EAI and MCI','EAI. MCI 등의 인터페이스 시스템',1,now(),1,now()),
('Not known','알 수 없음',1,now(),1,now()),
('Internal users','내부 사용자',1,now(),1,now()),
('External and internal users','외부 및 내부 사용자',1,now(),1,now()),
('External users','외부 사용자',1,now(),1,now()),
('System usage that is not high and no loads','시스템 사용량은 높지 않고, 부하도 없음',1,now(),1,now()),
('System usage is high but not concentrated at a specific point of time.','시스템 사용량은 높지만, 특정 시기에 집중되지 않는 평균치의 사용량임',1,now(),1,now()),
('System usage hike during monthly/annual events','월/연간 이벤트 시점에 일정 간격으로 시스템 사용량이 높아짐',1,now(),1,now()),
('Loads increase to ten or more times of the average at a specific point of time such as a marketing event.','마케팅 등의 특정 시기에 10배 이상의 부하가 발생함',1,now(),1,now()),
('Not prioritized at all','전혀 그렇지 않다',1,now(),1,now()),
('Not prioritized so much','그렇지 않다',1,now(),1,now()),
('Average','보통',1,now(),1,now()),
('Prioritized','그렇다',1,now(),1,now()),
('Absolutely prioritized','매우 그렇다',1,now(),1,now()),
('Very high','CPU 사용률이 매우 높음',1,now(),1,now()),
('High','CPU 사용률이 높음',1,now(),1,now()),
('Average','CPU 사용률이 보통임',1,now(),1,now()),
('Low','CPU 사용률이 낮음',1,now(),1,now()),
('Very low','CPU 사용률이 매우 낮음',1,now(),1,now()),
('10 TB or more','스토리지 사용량이 10 TB 이상임',1,now(),1,now()),
('1 TB to 10 TB','스토리지 사용량이 1~10 TB 임',1,now(),1,now()),
('500 GB or more','스토리지 사용량이 500 GB 이상임',1,now(),1,now()),
('100 GB to 500 GB','스토리지 사용량이 100~500 GB 임',1,now(),1,now()),
('Below 100 GB','스토리지 사용량이 100 GB 미만임',1,now(),1,now()),
('7 years ago or earlier','내용연수가 7년 이상',1,now(),1,now()),
('6 to 7 years ago','내용연수가 6~7년',1,now(),1,now()),
('5 years ago or later','내용연수가 5년 이하',1,now(),1,now()),
('20 or more systems are internally and externally interfaced','대내외 인터페이스 기준 20개 이상',1,now(),1,now()),
('10 to 20 systems','인터페이스 10~20개',1,now(),1,now()),
('3 to 10 systems','인터페이스 3~10개',1,now(),1,now()),
('3 or less systems','인터페이스 3개 이하',1,now(),1,now()),
('C (including Pro*C)','C (Pro*C 포함)',1,now(),1,now()),
('.NET','닷넷',1,now(),1,now()),
('Package Software','패키지 소프트웨어',1,now(),1,now()),
('Python, Node.js, Go, PHP','파이썬, Node.js, Go, PHP 등',1,now(),1,now()),
('Java','자바',1,now(),1,now()),
('TP-Monitor such as C, Tuxedo and Tmax','C, 턱시도, 티맥스 등의 TP 모니터',1,now(),1,now()),
('C-based framework such as ProFrame and bankware','프로프레임, 뱅크웨어 등의 C 기반 프레임워크',1,now(),1,now()),
('.NET Framework (Windows)','닷넷 프레임워크 (윈도우)',1,now(),1,now()),
('JAVA-based open source such as SpringBoot','자바 기반 스프링부트 등의 오픈소스',1,now(),1,now()),
('None','해당사항 없음',1,now(),1,now()),
('3-Tier architecture with an external interface such as EAI, MCI','3-티어와 EAI, MCI 등의 외부 인터페이스 연계',1,now(),1,now()),
('TP-Monitor such as Tuxedo and Tmax','턱시도, 티맥스 등의 TP 모니터',1,now(),1,now()),
('2-tier (Visual Basic, Visual C++ and PowerBuilder) runtime','2-티어 (비주얼 베이직, 비주얼 C++, 파워빌더) 런타임',1,now(),1,now()),
('3-tier architecture (WEB-WAS-DB)','WEB-WAS-DB의 3-티어 구조',1,now(),1,now()),
('Mainframe, AS/400 ','메인프레임, AS/400',1,now(),1,now()),
('Including hardware-centric appliance equipment','하드웨어 중심의 어플라이언스 장비 포함',1,now(),1,now()),
('Solely operated with Unix','유닉스 단독 운영',1,now(),1,now()),
('Mixed operation with Unix, Linux and Windows','유닉스, 리눅스, 윈도우 혼용 운영',1,now(),1,now()),
('Solely operated with Linux','리눅스 단독 운영',1,now(),1,now()),
('Not virtualized - Unix (VPar and LPar)','가상화 적용 안됨 - 유닉스 (VPar, LPar)',1,now(),1,now()),
('Not virtualized – ordinary bare metal x86 system','가상화 적용 안됨 - 일반 베어메탈 x86 시스템',1,now(),1,now()),
('Mixed operation with virtualized and non-vitrualized system','가상화 적용 & 미적용으로 혼용 운영',1,now(),1,now()),
('Virtualized - 100%','가상화 적용 - 100%',1,now(),1,now()),
('Very low','매우 낮음',1,now(),1,now()),
('Low','낮음',1,now(),1,now()),
('Average','보통',1,now(),1,now()),
('High','높음',1,now(),1,now()),
('Very high','매우 높음',1,now(),1,now());

INSERT INTO QUESTION_ANSWER (QUESTION_ID, ANSWER_ID, DISPLAY_ORDER, SCORE) VALUES
(1, 1, 1, 1),
(1, 2, 2, 2),
(1, 3, 3, 3),
(1, 4, 4, 4),
(1, 5, 5, 5),
(2, 6, 1, 1),
(2, 7, 2, 3),
(2, 8, 3, 5),
(3, 9, 1, 1),
(3, 10, 2, 2),
(3, 11, 3, 3),
(3, 12, 4, 4),
(3, 13, 5, 5),
(4, 14, 1, 1),
(4, 15, 2, 3),
(4, 16, 3, 4),
(4, 17, 4, 5),
(5, 18, 1, 1),
(5, 19, 2, 2),
(5, 20, 3, 3),
(5, 21, 4, 4),
(5, 22, 5, 5),
(6, 23, 1, 1),
(6, 24, 2, 2),
(6, 25, 3, 3),
(6, 26, 4, 4),
(6, 27, 5, 5),
(7, 28, 1, 1),
(7, 29, 2, 2),
(7, 30, 3, 3),
(7, 31, 4, 4),
(7, 32, 5, 5),
(8, 33, 1, 1),
(8, 34, 2, 3),
(8, 35, 3, 5),
(9, 36, 1, 0),
(9, 37, 2, 1),
(9, 38, 3, 3),
(9, 39, 4, 5),
(10, 40, 1, 1),
(10, 41, 2, 1),
(10, 42, 3, 3),
(10, 43, 4, 3),
(10, 44, 5, 5),
(11, 45, 1, 1),
(11, 46, 2, 1),
(11, 47, 3, 3),
(11, 48, 4, 5),
(11, 49, 5, 2),
(12, 50, 1, 1),
(12, 51, 2, 3),
(12, 52, 3, 3),
(12, 53, 4, 5),
(13, 54, 1, 1),
(13, 55, 2, 1),
(13, 56, 3, 2),
(13, 57, 4, 3),
(13, 58, 5, 5),
(14, 59, 1, 1),
(14, 60, 2, 2),
(14, 61, 3, 3),
(14, 62, 4, 5),
(15, 63, 1, 1),
(15, 64, 2, 2),
(15, 65, 3, 3),
(15, 66, 4, 4),
(15, 67, 5, 5);

-- Elasticsearch
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Elasticsearch', 'Elasticsearch B.V.', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'java,elasticsearch', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'elasticsearch', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '9200', null, null, 'N');

-- Kibana
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Kibana', 'Elasticsearch B.V.', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'node,kibana', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'kibana', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '5601', null, null, 'N');

-- Redis
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Redis', 'Redis Ltd.', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'redis-server', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'redis', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'redis-cli', null, null, 'N');

-- MongoDB
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('MongoDB', 'MongoDB, Inc.', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'mongod', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '27017', null, null, 'N');

-- Cassandra
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Cassandra', 'The Apache Software Foundation', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'cassandra', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '7000', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '7001', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '7199', null, null, 'N');

-- IIS
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('IIS', 'Microsoft', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'w3wp', null, null, 'N');

-- Jennifer
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Jennifer', 'JenniferSoft, Inc.', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'java,jennifer', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'jennifer.config,javaagent', null, null, 'N');

-- Netbackup
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Netbackup', 'Veritas', 'Backup Solution', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'bprd', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'bpsched', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'bpdbm', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'bpjobd', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'bpcd', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'bpbrm', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'bpps', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'vxpbx_exchanged', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'VRTS', null, null, 'N');

-- Networker
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Networker', 'EMC', 'Backup Solution', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nsrd', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nsrexecd', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nsrsnmd', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nsradmin', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'lgto', null, null, 'N');

-- Veeam
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Veeam', 'Veeam Software', 'Backup Solution', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'veeamconfig', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'veeam', null, null, 'N');

-- Commvault
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Commvault', 'Commvault', 'Backup Solution', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'commvault', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'cvlaunchd', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'cvd', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'CvMountd', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'ClMgrS', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'commvault', null, null, 'N');

-- Netvault
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Netvault', 'Quest', 'Backup Solution', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nvdevmgr', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nvpmgr', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nvnmgr', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nvcmgr', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nvlogdaemon', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nvstatsmngr', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nvchmgr', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nvgui', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nvguiproxy', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'netvault-catalog', null, null, 'N');

-- Acronis
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Acronis', 'Acronis', 'Backup Solution', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'acronis_mms', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'acronis_agent', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'schedul2.exe', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'schedhlp.exe', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'active_protection_service.exe', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'arsm.exe', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'mms.exe', null, null, 'N');

-- SolarWinds
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('SolarWinds', 'Solarwinds', 'Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'SolarWinds', null, null, 'N');

-- Datadog
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Datadog', 'Datadog', 'Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'datadog-agent', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PKG', 'datadog-agent', null, null, 'N');

-- Site24x7
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Site24x7', 'Zoho', 'Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'Site24x7', null, null, 'N');

-- Nagios
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Nagios', 'Nagios', 'Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'nagios', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'nagios', null, null, 'N');

-- Zabbix
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Zabbix', 'Zabbix', 'Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'zabbix', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'zabbix', null, null, 'N');

-- ManageEngine OpManager
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('ManageEngine OpManager', 'Zoho', 'Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'OpManager', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'ManageEngine OpManager', null, null, 'N');

-- Spiceworks
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Spiceworks', 'Spiceworks', 'Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'Spiceworks.exe', null, null, 'N');

-- Icinga
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Icinga', 'Icinga', 'Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'icinga', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'icinga', null, null, 'N');

-- PRTG
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('PRTG', 'Paessler', 'Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'PRTG', null, null, 'N');

-- Obkio
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Obkio', 'Obkio', 'Network Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'ObkioAgentService', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PORT', '23999', null, null, 'N');

-- Redgate SQL Monitor
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Redgate SQL Monitor', 'Redgate', 'Database Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'SQL Monitor Web Service', null, null, 'N');

-- dbWatch
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('dbWatch', 'dbWatch AS', 'Database Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'dbWatch', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'dbWatch', null, null, 'N');

-- Oracle Enterprise Manager
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Oracle Enterprise Manager', 'Oracle', 'Database Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'mgmt_agent', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'emctl', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'emctl', null, null, 'N');

-- eG Enterprise
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('eG Enterprise', 'eG Innovations', 'Server Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'eGmon', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'eGurkha', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'eGAgentMon', null, null, 'N');

-- Cacti
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Cacti', 'Cacti Group', 'Server Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'cacti', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'cactid', null, null, 'N');

-- New Relic
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('New Relic', 'New Relic', 'Server Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'newrelic-infra', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'newrelic-infra', null, null, 'N');

-- CONTROL-M
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Control-M', 'BMC', 'Batch Job Management', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'emcms', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'emmaintag', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'emselfservicesrv', null, null, 'N');

-- ERWIN
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('ERwin', 'Quest', 'Data Modeling', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'ERwin', null, null, 'N');

-- NAMO ACTIVE SQUARE
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Namo ACTIVE SQUARE', '지란지교소프트', 'Web Editor', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'namowec', null, null, 'N');

-- BI MATRIX
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('BI MATRIX', '비아이매트릭스', 'Data Reporting Tool', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'BIUpdate.exe', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'i-CHECK5.exe', null, null, 'N');

-- VestID
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('VestID', '예티소프트', '인증, 서명, 위변조방지, 보안메일등의 솔루션', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'VestCert', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'VestMail', null, null, 'N');

-- SYSMASTER
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('SysMaster', '티맥스소프트', 'Application Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'smmaster', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'smmaster', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'smagent', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'smdown', null, null, 'N');

-- Rexpert
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Rexpert', '클립소프트', '문서출력보안', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'rexviewer30.exe', null, null, 'N');

-- ONTUNE
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('onTune', '팀스톤', 'Server Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'ontuned', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'OnTuneAgent.exe', null, null, 'N');

-- Tivoli
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Tivoli', 'IBM', 'Infra Management', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'tacmd', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'itmcmd', null, null, 'N');

-- ez-GATOR
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('ez-GATOR', '세넷시스템즈', 'Secure FTP', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'gator.bat', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'gator.sh', null, null, 'N');

-- Moffice v2.0
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Moffice v2.0', '제이니스', '근태관리', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'Moffice', null, null, 'N');

-- WiseGrid
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('WiseGrid', '유니포스트', 'Web Grid Component', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'WiseGrid', null, null, 'N');

-- e-spider
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('e-spider', '헥토데이터', '', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'espiderMan.exe', null, null, 'N');

-- MCCS
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('MCCS', '맨텍', 'HA Solution', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'MCCS HA Service', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'MCCS', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'mccs_agent.service', null, null, 'N');

-- MaxGauge
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('MaxGauge', '엑셈', 'Database Monitoring', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'MaxGauge', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'SVC', 'Exem', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'MaxGauge', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'sysmon', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'PROCESS', 'DG', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'rtsctl', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'dgboot', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'dgdown', null, null, 'N');

-- Blancco
INSERT INTO THIRD_PARTY_SOLUTION (THIRD_PARTY_SOLUTION_NAME, VENDOR, DESCRIPTION, DELETE_YN, REGIST_USER_ID, REGIST_DATETIME, MODIFY_USER_ID, MODIFY_DATETIME) VALUES ('Blancco', 'Blancco', 'Data Erase ', 'N', 1, current_timestamp, 1, current_timestamp);
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'SITCmd.exe', null, null, 'N');
INSERT INTO THIRD_PARTY_SEARCH_TYPE (THIRD_PARTY_SOLUTION_ID, SEARCH_TYPE, SEARCH_VALUE, INVENTORY_TYPE_CODE, WINDOWS_YN, DELETE_YN) VALUES ((SELECT MAX(THIRD_PARTY_SOLUTION_ID) FROM THIRD_PARTY_SOLUTION), 'CMD', 'SITFileShredding.exe', null, null, 'N');


-- Product Lifecycle Rules
INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Server', 'CentOS', 'CentOS Project','Y');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6', STR_TO_DATE('2011-12-09', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2020-11-30', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7', STR_TO_DATE('2014-07-07', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2024-06-30', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8', STR_TO_DATE('2019-09-24', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2021-12-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Server', 'Ubuntu', 'Canonical','Y');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('14.04 LTS', STR_TO_DATE('2014-04-17', '%Y-%m-%d'), STR_TO_DATE('2019-04-25', '%Y-%m-%d'), STR_TO_DATE('2024-04-25', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('14.1', STR_TO_DATE('2014-10-23', '%Y-%m-%d'), STR_TO_DATE('2019-04-25', '%Y-%m-%d'), STR_TO_DATE('2015-07-23', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('15.04', STR_TO_DATE('2015-04-23', '%Y-%m-%d'), STR_TO_DATE('2019-04-25', '%Y-%m-%d'), STR_TO_DATE('2016-02-04', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('16.04 LTS', STR_TO_DATE('2016-04-21', '%Y-%m-%d'), STR_TO_DATE('2021-04-30', '%Y-%m-%d'), STR_TO_DATE('2026-04-23', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('16.1', STR_TO_DATE('2016-10-13', '%Y-%m-%d'), STR_TO_DATE('2021-04-30', '%Y-%m-%d'), STR_TO_DATE('2017-07-20', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Server', 'RHEL','Red Hat', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6', STR_TO_DATE('2010-11-10', '%Y-%m-%d'), STR_TO_DATE('2016-05-10', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7 POWER', STR_TO_DATE('2017-11-13', '%Y-%m-%d'), STR_TO_DATE('2019-08-06', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7 ARM', STR_TO_DATE('2017-11-13', '%Y-%m-%d'), STR_TO_DATE('2019-08-06', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7 System Z', STR_TO_DATE('2018-04-10', '%Y-%m-%d'), STR_TO_DATE('2019-08-06', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7', STR_TO_DATE('2014-06-10', '%Y-%m-%d'), STR_TO_DATE('2019-08-06', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8', STR_TO_DATE('2019-05-07', '%Y-%m-%d'), STR_TO_DATE('2024-05-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9', STR_TO_DATE('2022-05-18', '%Y-%m-%d'), STR_TO_DATE('2027-05-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Server', 'Oracle Linux', 'Oracle', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6', STR_TO_DATE('2011-01-01', '%Y-%m-%d'), STR_TO_DATE('2021-03-01', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7', STR_TO_DATE('2014-07-01', '%Y-%m-%d'), STR_TO_DATE('2024-07-01', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8', STR_TO_DATE('2019-07-01', '%Y-%m-%d'), STR_TO_DATE('2029-07-01', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9', STR_TO_DATE('2022-06-01', '%Y-%m-%d'), STR_TO_DATE('2032-06-01', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Server', 'Debian', 'Debian Project', 'Y');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6', STR_TO_DATE('2011-02-06', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2014-05-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7', STR_TO_DATE('2013-05-04', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2016-04-25', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8', STR_TO_DATE('2015-04-25', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2018-06-17', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9', STR_TO_DATE('2017-06-17', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2020-07-18', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10', STR_TO_DATE('2019-07-06', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2022-09-10', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11', STR_TO_DATE('2021-08-14', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2024-07-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Server', 'Fedora', 'Red Hat', 'Y');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('19', STR_TO_DATE('2013-07-02', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2015-01-06', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('20', STR_TO_DATE('2013-12-17', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2015-06-23', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('21', STR_TO_DATE('2014-12-09', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2015-12-01', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('22', STR_TO_DATE('2014-10-22', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2016-07-19', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('23', STR_TO_DATE('2015-11-03', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2016-12-20', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('24', STR_TO_DATE('2016-06-21', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2017-08-08', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('25', STR_TO_DATE('2016-11-22', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2017-12-12', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('26', STR_TO_DATE('2017-11-14', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2018-05-29', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('27', STR_TO_DATE('2014-10-22', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2018-11-30', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('28', STR_TO_DATE('2018-05-01', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2019-05-28', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('29', STR_TO_DATE('2018-10-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2019-11-26', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('30', STR_TO_DATE('2019-04-29', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2020-05-26', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('31', STR_TO_DATE('2019-10-29', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2020-11-24', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('32', STR_TO_DATE('2020-04-28', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2021-05-25', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('33', STR_TO_DATE('2020-10-27', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2021-11-30', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('34', STR_TO_DATE('2021-04-27', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2022-06-07', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('35', STR_TO_DATE('2021-11-02', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2022-12-13', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Server', 'AIX', 'IBM', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('5.3 Standard Edition', STR_TO_DATE('2004-07-13', '%Y-%m-%d'), STR_TO_DATE('2012-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6.1 Enterprise Edition', STR_TO_DATE('2008-09-12', '%Y-%m-%d'), STR_TO_DATE('2017-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6.1 Express Edition', STR_TO_DATE('2010-04-23', '%Y-%m-%d'), STR_TO_DATE('2017-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6.1 Standard Edition', STR_TO_DATE('2007-11-09', '%Y-%m-%d'), STR_TO_DATE('2017-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7.1 Express Edition', STR_TO_DATE('2010-09-10', '%Y-%m-%d'), STR_TO_DATE('2023-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7.1 Standard Edition', STR_TO_DATE('2010-09-10', '%Y-%m-%d'), STR_TO_DATE('2023-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7.2 Standard Edition', STR_TO_DATE('2015-12-04', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7.3 Standard Edition', STR_TO_DATE('2021-12-10', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Server', 'HP-UX','HP', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11i v2 Integrity', STR_TO_DATE('2003-10-01', '%Y-%m-%d'), STR_TO_DATE('2023-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11i v2 HP 9000', STR_TO_DATE('2004-02-01', '%Y-%m-%d'), STR_TO_DATE('2023-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11i v3 HP 9000', STR_TO_DATE('2007-02-01', '%Y-%m-%d'), STR_TO_DATE('2021-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11i v3 Integrity', STR_TO_DATE('2007-02-01', '%Y-%m-%d'), STR_TO_DATE('2025-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Server', 'Solaris', 'Oracle', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10', STR_TO_DATE('2005-01-31', '%Y-%m-%d'), STR_TO_DATE('2018-01-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11 Express', STR_TO_DATE('2010-11-15', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11', STR_TO_DATE('2011-11-09', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11.1', STR_TO_DATE('2012-10-03', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11.2', STR_TO_DATE('2014-04-29', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11.3', STR_TO_DATE('2015-10-26', '%Y-%m-%d'), STR_TO_DATE('2021-01-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11.4', STR_TO_DATE('2018-08-28', '%Y-%m-%d'), STR_TO_DATE('2031-11-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Server', 'Windows', 'Microsoft', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2008', STR_TO_DATE('2008-05-06', '%Y-%m-%d'), STR_TO_DATE('2015-01-13', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2008 R2', STR_TO_DATE('2009-10-22', '%Y-%m-%d'), STR_TO_DATE('2015-01-13', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2012', STR_TO_DATE('2012-10-30', '%Y-%m-%d'), STR_TO_DATE('2018-10-09', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2012 R2', STR_TO_DATE('2013-11-25', '%Y-%m-%d'), STR_TO_DATE('2018-10-09', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2016', STR_TO_DATE('2016-10-15', '%Y-%m-%d'), STR_TO_DATE('2022-01-11', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2019', STR_TO_DATE('2018-11-13', '%Y-%m-%d'), STR_TO_DATE('2024-01-09', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2022', STR_TO_DATE('2021-08-18', '%Y-%m-%d'), STR_TO_DATE('2026-10-13', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Server', 'Rocky Linux', 'Rocky Enterprise Software Foundation', 'Y');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8', STR_TO_DATE('2021-06-21', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2029-05-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9', STR_TO_DATE('2022-07-14', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2032-05-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Middleware', 'Tomcat', 'Apache Software Foundation', 'Y');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES ('7', STR_TO_DATE('2013-01-10', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2021-03-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES ('8.0', STR_TO_DATE('2014-01-29', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2018-06-30', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES ('8.5', STR_TO_DATE('2016-03-17', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2024-03-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES ('9', STR_TO_DATE('2017-09-27', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES ('10.0', STR_TO_DATE('2020-12-03', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2022-10-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES ('10.1', STR_TO_DATE('2021-06-15', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Middleware', 'WebLogic','Oracle', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.3.0', STR_TO_DATE('2008-08-01', '%Y-%m-%d'), STR_TO_DATE('2014-01-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.3.1', STR_TO_DATE('2009-06-01', '%Y-%m-%d'), STR_TO_DATE('2018-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.3.2', STR_TO_DATE('2009-06-01', '%Y-%m-%d'), STR_TO_DATE('2018-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.3.3', STR_TO_DATE('2009-06-01', '%Y-%m-%d'), STR_TO_DATE('2018-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.3.4', STR_TO_DATE('2009-06-01', '%Y-%m-%d'), STR_TO_DATE('2018-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.3.5', STR_TO_DATE('2009-06-01', '%Y-%m-%d'), STR_TO_DATE('2018-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.3.6', STR_TO_DATE('2012-02-26', '%Y-%m-%d'), STR_TO_DATE('2018-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12.1', STR_TO_DATE('2011-12-01', '%Y-%m-%d'), STR_TO_DATE('2017-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12.2', STR_TO_DATE('2015-10-01', '%Y-%m-%d'), STR_TO_DATE('2025-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('14.1', STR_TO_DATE('2020-05-01', '%Y-%m-%d'), STR_TO_DATE('2025-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Middleware', 'Jeus', 'TmaxSoft', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6', STR_TO_DATE('2007-06-07', '%Y-%m-%d'), STR_TO_DATE('2018-12-31', '%Y-%m-%d'), STR_TO_DATE('2016-12-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7', STR_TO_DATE('2012-07-04', '%Y-%m-%d'), STR_TO_DATE('2022-06-30', '%Y-%m-%d'), STR_TO_DATE('2021-06-30', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8', STR_TO_DATE('2017-01-31', '%Y-%m-%d'), STR_TO_DATE('2027-03-31', '%Y-%m-%d'), STR_TO_DATE('2026-03-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8.5', STR_TO_DATE('2021-09-30', '%Y-%m-%d'), STR_TO_DATE('2027-03-31', '%Y-%m-%d'), STR_TO_DATE('2026-03-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Middleware', 'WebSphere', 'IBM', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7', STR_TO_DATE('2008-10-17', '%Y-%m-%d'), STR_TO_DATE('2018-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8', STR_TO_DATE('2011-07-22', '%Y-%m-%d'), STR_TO_DATE('2018-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8.5', STR_TO_DATE('2012-07-13', '%Y-%m-%d'), STR_TO_DATE('2018-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9', STR_TO_DATE('2016-06-24', '%Y-%m-%d'), STR_TO_DATE('2030-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Middleware', 'Apache', 'Apache Software Foundation', 'Y');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2.2', STR_TO_DATE('2005-12-01', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2017-12-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2.4', STR_TO_DATE('2012-02-21', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Middleware', 'OHS', 'Oracle', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12.1', STR_TO_DATE('2011-12-01', '%Y-%m-%d'), STR_TO_DATE('2017-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12.2', STR_TO_DATE('2015-10-01', '%Y-%m-%d'), STR_TO_DATE('2025-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Middleware', 'WebToB', 'TmaxSoft', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('4.1.5', STR_TO_DATE('2012-04-10', '%Y-%m-%d'), STR_TO_DATE('2018-06-30', '%Y-%m-%d'), STR_TO_DATE('2017-06-30', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('4.1.6', STR_TO_DATE('2013-12-19', '%Y-%m-%d'), STR_TO_DATE('2019-02-28', '%Y-%m-%d'), STR_TO_DATE('2018-02-28', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('4.1.7', STR_TO_DATE('2014-05-23', '%Y-%m-%d'), STR_TO_DATE('2019-06-30', '%Y-%m-%d'), STR_TO_DATE('2018-06-30', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('4.1.8', STR_TO_DATE('2014-08-29', '%Y-%m-%d'), STR_TO_DATE('2020-08-31', '%Y-%m-%d'), STR_TO_DATE('2019-08-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('4.1.9', STR_TO_DATE('2015-09-25', '%Y-%m-%d'), STR_TO_DATE('2022-06-30', '%Y-%m-%d'), STR_TO_DATE('2021-06-30', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('5', STR_TO_DATE('2016-03-31', '%Y-%m-%d'), STR_TO_DATE('2027-08-31', '%Y-%m-%d'), STR_TO_DATE('2026-08-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Middleware', 'JBoss', 'RedHat', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6', STR_TO_DATE('2012-06-01', '%Y-%m-%d'), STR_TO_DATE('2016-06-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('7', STR_TO_DATE('2016-05-01', '%Y-%m-%d'), STR_TO_DATE('2023-06-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

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

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Database', 'Oracle Database', 'Oracle', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.1', STR_TO_DATE('2004-01-01', '%Y-%m-%d'), STR_TO_DATE('2009-01-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.2', STR_TO_DATE('2005-07-01', '%Y-%m-%d'), STR_TO_DATE('2010-07-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11.1', STR_TO_DATE('2007-08-01', '%Y-%m-%d'), STR_TO_DATE('2012-08-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11.2', STR_TO_DATE('2009-09-01', '%Y-%m-%d'), STR_TO_DATE('2015-01-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12.1 Enterprise Edition', STR_TO_DATE('2013-06-01', '%Y-%m-%d'), STR_TO_DATE('2018-07-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12.1 Standard Edition', STR_TO_DATE('2013-06-01', '%Y-%m-%d'), STR_TO_DATE('2016-08-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12.1 Standard Edition One (SE1)', STR_TO_DATE('2013-06-01', '%Y-%m-%d'), STR_TO_DATE('2016-08-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12.1 Standard Edition One (SE2)', STR_TO_DATE('2015-09-01', '%Y-%m-%d'), STR_TO_DATE('2018-07-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12.2.0.1', STR_TO_DATE('2017-03-01', '%Y-%m-%d'), STR_TO_DATE('2020-11-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Database', 'MySQL', 'Oracle', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('5.6', STR_TO_DATE('2013-02-01', '%Y-%m-%d'), STR_TO_DATE('2018-02-28', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('5.7', STR_TO_DATE('2015-10-01', '%Y-%m-%d'), STR_TO_DATE('2020-10-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('8', STR_TO_DATE('2018-04-01', '%Y-%m-%d'), STR_TO_DATE('2025-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Database', 'MariaDB', 'MariaDB Foundation', 'Y');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('5.5', STR_TO_DATE('2012-04-11', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2020-04-11', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10', STR_TO_DATE('2014-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2019-03-31', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.1', STR_TO_DATE('2015-10-17', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2020-10-17', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.2', STR_TO_DATE('2017-05-23', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2022-05-23', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.3', STR_TO_DATE('2018-05-25', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2023-05-25', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.4', STR_TO_DATE('2019-06-18', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2024-06-18', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10.5', STR_TO_DATE('2020-06-24', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2025-06-24', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Database', 'MSSQL', 'Microsoft', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2008', STR_TO_DATE('2008-11-06', '%Y-%m-%d'), STR_TO_DATE('2014-07-08', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2012', STR_TO_DATE('2012-05-20', '%Y-%m-%d'), STR_TO_DATE('2017-07-11', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2014', STR_TO_DATE('2014-06-05', '%Y-%m-%d'), STR_TO_DATE('2019-07-09', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2017', STR_TO_DATE('2017-09-29', '%Y-%m-%d'), STR_TO_DATE('2022-10-11', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2019', STR_TO_DATE('2019-11-04', '%Y-%m-%d'), STR_TO_DATE('2025-02-28', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('2022', STR_TO_DATE('2022-11-16', '%Y-%m-%d'), STR_TO_DATE('2028-01-11', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Database', 'Sybase', 'SAP', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('16', STR_TO_DATE('2014-03-14', '%Y-%m-%d'), STR_TO_DATE('2025-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Database', 'Tibero', 'TmaxSoft', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('6', STR_TO_DATE('2015-04-01', '%Y-%m-%d'), STR_TO_DATE('2024-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

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

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Java Application', 'Java By Oracle', 'Oracle', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.6', STR_TO_DATE('2006-12-01', '%Y-%m-%d'), STR_TO_DATE('2015-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.7', STR_TO_DATE('2011-07-01', '%Y-%m-%d'), STR_TO_DATE('2019-07-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.8', STR_TO_DATE('2014-03-01', '%Y-%m-%d'), STR_TO_DATE('2022-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9', STR_TO_DATE('2017-09-01', '%Y-%m-%d'), STR_TO_DATE('2018-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10', STR_TO_DATE('2018-03-01', '%Y-%m-%d'), STR_TO_DATE('2018-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11', STR_TO_DATE('2018-09-01', '%Y-%m-%d'), STR_TO_DATE('2023-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12', STR_TO_DATE('2019-03-01', '%Y-%m-%d'), STR_TO_DATE('2019-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('13', STR_TO_DATE('2019-09-01', '%Y-%m-%d'), STR_TO_DATE('2020-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('14', STR_TO_DATE('2020-03-01', '%Y-%m-%d'), STR_TO_DATE('2020-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('15', STR_TO_DATE('2020-09-01', '%Y-%m-%d'), STR_TO_DATE('2021-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('16', STR_TO_DATE('2021-03-01', '%Y-%m-%d'), STR_TO_DATE('2021-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('17', STR_TO_DATE('2021-09-01', '%Y-%m-%d'), STR_TO_DATE('2026-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('18', STR_TO_DATE('2022-03-01', '%Y-%m-%d'), STR_TO_DATE('2022-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('19', STR_TO_DATE('2022-09-01', '%Y-%m-%d'), STR_TO_DATE('2023-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('20', STR_TO_DATE('2023-03-01', '%Y-%m-%d'), STR_TO_DATE('2023-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('21', STR_TO_DATE('2023-09-01', '%Y-%m-%d'), STR_TO_DATE('2028-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Java Application', 'Java By HP-UX', 'HP-UX', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.6', STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2018-06-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.7', STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2022-07-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.8', STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2025-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11', STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('2023-01-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Java Application', 'Java By IBM', 'IBM', 'N');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.6', STR_TO_DATE('2007-11-01', '%Y-%m-%d'), STR_TO_DATE('2017-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.6 z/OS', STR_TO_DATE('2008-01-01', '%Y-%m-%d'), STR_TO_DATE('2018-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.7', STR_TO_DATE('2011-09-01', '%Y-%m-%d'), STR_TO_DATE('2011-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.7 z/OS', STR_TO_DATE('2011-10-01', '%Y-%m-%d'), STR_TO_DATE('2022-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.8', STR_TO_DATE('2015-02-01', '%Y-%m-%d'), STR_TO_DATE('2025-04-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.8 z/OS', STR_TO_DATE('2015-03-01', '%Y-%m-%d'), STR_TO_DATE('2026-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Java Application', 'Java By Azul', 'Azul', 'Y');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.8', STR_TO_DATE('2014-03-01', '%Y-%m-%d'), STR_TO_DATE('2030-12-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('9', STR_TO_DATE('2017-09-01', '%Y-%m-%d'), STR_TO_DATE('2018-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('10', STR_TO_DATE('2018-03-01', '%Y-%m-%d'), STR_TO_DATE('2018-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11', STR_TO_DATE('2018-09-01', '%Y-%m-%d'), STR_TO_DATE('2026-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('12', STR_TO_DATE('2019-03-01', '%Y-%m-%d'), STR_TO_DATE('2019-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('13', STR_TO_DATE('2019-09-01', '%Y-%m-%d'), STR_TO_DATE('2023-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('14', STR_TO_DATE('2020-03-01', '%Y-%m-%d'), STR_TO_DATE('2020-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('15', STR_TO_DATE('2020-09-01', '%Y-%m-%d'), STR_TO_DATE('2023-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('16', STR_TO_DATE('2021-03-01', '%Y-%m-%d'), STR_TO_DATE('2021-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('17', STR_TO_DATE('2021-09-01', '%Y-%m-%d'), STR_TO_DATE('2029-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('18', STR_TO_DATE('2022-03-01', '%Y-%m-%d'), STR_TO_DATE('2022-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('19', STR_TO_DATE('2022-09-01', '%Y-%m-%d'), STR_TO_DATE('2023-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('20', STR_TO_DATE('2023-03-01', '%Y-%m-%d'), STR_TO_DATE('2023-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('21', STR_TO_DATE('2023-09-01', '%Y-%m-%d'), STR_TO_DATE('2031-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));

INSERT INTO PRODUCT_LIFECYCLE_RULES (SOLUTION_TYPE, SOLUTION_NAME, VENDOR, OPENSOURCE_YN) VALUES ('Java Application', 'Java By Eclipse Temurin', 'Eclipse Temurin', 'Y');
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('1.8', STR_TO_DATE('2014-03-01', '%Y-%m-%d'), STR_TO_DATE('2026-11-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('11', STR_TO_DATE('2018-09-01', '%Y-%m-%d'), STR_TO_DATE('2024-10-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('17', STR_TO_DATE('2021-09-01', '%Y-%m-%d'), STR_TO_DATE('2027-10-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('18', STR_TO_DATE('2022-03-01', '%Y-%m-%d'), STR_TO_DATE('2022-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('19', STR_TO_DATE('2022-09-01', '%Y-%m-%d'), STR_TO_DATE('2023-03-31', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID) VALUES('20', STR_TO_DATE('2023-03-01', '%Y-%m-%d'), STR_TO_DATE('2023-09-30', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT MAX(PRODUCT_LIFECYCLE_RULES_ID) FROM PRODUCT_LIFECYCLE_RULES));
commit;