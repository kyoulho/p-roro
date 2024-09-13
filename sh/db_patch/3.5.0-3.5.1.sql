TRUNCATE SURVEY_USER_ANSWER;
TRUNCATE QUESTION_ANSWER;
TRUNCATE SURVEY_QUESTION;
TRUNCATE QUESTION;
TRUNCATE ANSWER;
TRUNCATE SURVEY_CATEGORY;
TRUNCATE SURVEY;
TRUNCATE SURVEY_PROCESS;

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

-- Delete FILE type 3rd party discovery rules
DELETE FROM THIRD_PARTY_SEARCH_TYPE WHERE SEARCH_TYPE = 'FILE';