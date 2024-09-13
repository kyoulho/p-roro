-- Resolve constraint for migration
alter table inventory_migration_process modify migration_pre_config_id bigint null comment '마이그레이션 사전 설정 아이디';

-- Netvault 3rd Party Solution에 Package 정보 삭제
DELETE FROM THIRD_PARTY_SEARCH_TYPE WHERE THIRD_PARTY_SOLUTION_ID = (SELECT THIRD_PARTY_SOLUTION_ID FROM THIRD_PARTY_SOLUTION WHERE THIRD_PARTY_SOLUTION_NAME = 'Netvault') AND SEARCH_TYPE = 'PKG';

-- Add column to EXTERNAL_CONNECTION
ALTER TABLE EXTERNAL_CONNECTION ADD LINE_NUM INT NULL AFTER EXTERNAL_CONNECTION_ID;

-- Product life cycle
UPDATE PRODUCT_LIFECYCLE_RULES_VERSION
SET VERSION = '10.0'
WHERE PRODUCT_LIFECYCLE_RULES_VERSION_ID = (SELECT plrv.PRODUCT_LIFECYCLE_RULES_VERSION_ID
                                            FROM product_lifecycle_rules_version plrv
                                                     JOIN product_lifecycle_rules plr on plrv.PRODUCT_LIFECYCLE_RULES_ID = plr.PRODUCT_LIFECYCLE_RULES_ID
                                            WHERE plr.SOLUTION_NAME = 'Tomcat'
                                              AND plrv.VERSION = '10');

INSERT INTO PRODUCT_LIFECYCLE_RULES_VERSION (VERSION, GA_DATETIME, EOS_DATETIME, EOL_DATETIME, PRODUCT_LIFECYCLE_RULES_ID)
VALUES ('10.1', STR_TO_DATE('2021-06-15', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), STR_TO_DATE('', '%Y-%m-%d'), (SELECT PRODUCT_LIFECYCLE_RULES_ID
                                                                                                                  FROM product_lifecycle_rules plr
                                                                                                                  WHERE SOLUTION_NAME = 'Tomcat'));