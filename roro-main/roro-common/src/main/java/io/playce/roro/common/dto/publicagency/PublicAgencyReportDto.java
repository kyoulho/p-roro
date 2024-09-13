/*
 * Copyright 2023 The playce-roro-v3 Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * SangCheon Park   Feb 20, 2023		    First Draft.
 */
package io.playce.roro.common.dto.publicagency;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
public class PublicAgencyReportDto {

    /**
     * ServerStatus, StorageStatus, BackupStatus는 Linux인 경우에만 저장이 된다.
     * 따라서 Unix(AIX, Solaris, HP-UX) 및 Windows의 경우 serverStatus 정보를 생성할 때에는 server_summary, server_disk_information 등의 정보를 참고한다.
     * <p>
     * SERVER_STATUS 테이블에 없는 데이터는 다음과 같이 처리한다.
     * - 호스트명은 서비스 현황조사서 작성 시 해당 서버의 hostName 값으로 저장한다.
     * - Manufacturer와 Model 값은 server_status의 값을 우선으로 하고 비어 있으면 server_master의 makerName, modelName을 사용한다.
     * - CPU 및 메모리 사용율은 스캔 시점에 백그라운드로 5분 평균 작업을 진행하고 완료되는 시점에 별도로 저장한다.
     * - 서비스용도(serviceType)은 서비스 현황조사서 작성 시 해당 서버 내의 미들웨어, 데이터베이스 존재 여부로 결정한다.
     * - 이중화여부는 서비스 현황조사서 작성 시 해당 서버의 dualizationTypeCode 값으로 결정한다.
     * - 망구분 (인터넷망/내부망)은 서비스 현황조사서 작성 시 해당 서버의 대표 IP가 내부망에 속하는지 외부망에 속하는지에 따라 결정한다.
     * - OS 종류는 서비스 현황조사서 작성 시 해당 서버의 inventoryDetailTypeCode 값으로 결정한다. (Linux / Unix / Windows)
     * - OS 버전은 서비스 현황조사서 작성 시 해당 서버의 osVersion 값으로 저장한다.
     * - 커널버전은 서비스 현황조사서 작성 시 해당 서버의 osKernel 값으로 저장한다.
     * - CPU 코어 수는 서비스 현황조사서 작성 시 해당 서버의 cpuCoreCount 값으로 저장한다.
     * - CPU 소켓 수는 서비스 현황조사서 작성 시 해당 서버의 cpuSocketCount 값으로 저장한다.
     * - 메모리 용량 (GB)은 서비스 현황조사서 작성 시 해당 서버의 memSize 값으로 저장한다.
     * - 도입년월은 서비스 현황조사서 작성 시 해당 서버의 buyDate 값으로 저장한다.
     */

    private List<ServerStatus> serverStatusList = new ArrayList<>();
    private List<StorageStatus> storageStatusList = new ArrayList<>();
    private List<BackupStatus> backupStatusList = new ArrayList<>();
    private List<SoftwareStatus> softwareStatusList = new ArrayList<>();
    private List<ApplicationStatus> applicationStatusList = new ArrayList<>();
    private List<DatabaseStatus> databaseStatusList = new ArrayList<>();

    @Getter
    @Setter
    public static class ServerStatus {
        // 정보시스템ID
        private Long systemId;
        // 정보시스템명
        private String systemName;
        // 서버ID
        private Long serverId;
        // 서버명
        private String serverName;
        // 호스트네임
        private String hostname;
        // 서버구성현황 (물리/가상화)
        private String serverType;
        // 제조사
        private String manufacturer;
        // 모델
        private String model;
        // 서비스용도 (WEB/WAS/DB/기타)
        private String serviceType;
        // 이중화여부
        private Boolean highAvailability;
        // 망구분 (인터넷망/내부망)
        private String networkType;
        // OS 종류 (Unix / Linux / Windows)
        private String osType;
        // OS 버전
        private String osVersion;
        // 커널버전
        private String kernel;
        // CPU 코어 수
        private Integer cpuCores;
        // CPU 소켓 수
        private Integer cpuSockets;
        // 메모리 용량 (GB)
        private Long memorySize;
        // 전체 디스크 용량 (GB)
        // GB로의 변환은 DataSize.ofBytes(totalDiskSize).toGigabytes() 사용
        private Long diskSize;
        // 전체 디스크 갯수
        private Integer diskCount;
        // 전체 디스크 사용량 (GB)
        // GB로의 변환은 DataSize.ofBytes(totalDiskSize).toGigabytes() 사용
        private Long diskUsed;
        // CPU 사용율
        private Double cpuUsage;
        // Memory 사용율
        private Double memUsage;
        // CPU, Memory 사용율 조회 날짜
        private Date monitoringDatetime;
        // 백업 사용 여부 (Y/N)
        private String useBackup;
        // 도입년월
        private String purchaseDate;
    }

    @Getter
    @Setter
    public static class StorageStatus {
        // 정보시스템ID
        private Long systemId;
        // 정보시스템명
        private String systemName;
        // 서버ID
        private Long serverId;
        // 서버명
        private String serverName;
        // 제조사
        private String manufacturer;
        // 모델
        private String model;
        // 디스크 타입 (SSD / SATA / SAS)
        private String diskType;
        // 연결 타입 (SAN / NAS / Internal))
        private String connectionType;
        // 공유 여부
        private String sharingYn;
    }

    @Getter
    @Setter
    public static class BackupStatus {
        // 정보시스템ID
        private Long systemId;
        // 정보시스템명
        private String systemName;
        // 서버ID
        private Long serverId;
        // 서버명
        private String serverName;
        // 모델
        private String model;
    }

    @Getter
    @Setter
    public static class SoftwareStatus {
        // 정보시스템ID
        private Long systemId;
        // 정보시스템명
        private String systemName;
        // 서버ID
        private Long serverId;
        // 서버명
        private String serverName;
        // 소프트웨어명
        private String softwareName;
        // 버전
        private String version;
        // 공개SW 여부
        private Boolean isOpenSource;
        // 제공업체명
        private String vendor;
        // 용도 (OS / WEB / WAS / DBMS)
        private String category;
    }

    @Getter
    @Setter
    public static class ApplicationStatus {
        // 정보시스템ID
        private Long systemId;
        // 정보시스템명
        private String systemName;
        // 서버ID
        private Long serverId;
        // 서버명
        private String serverName;
        // 애플리케이션 ID
        private Long applicationId;
        // 애플리케이션명
        private String applicationName;
        // 개발언어
        private String developLanguage;
        // 개발언어 버전
        private String developLanguageVersion;
        // 사용 프레임워크 명
        private String frameworkName;
        // 사용 프레임워크 버전
        private String frameworkVersion;
        // https 사용 여부
        private String httpsUseYn;
        // 애플리케이션 타입 (CS / WEB / 기타)
        private String applicationType;
        // 애플리케이션 사이즈
        private Long applicationSize;
        // 사용 DBMS
        private String useDbms;
    }

    @Getter
    @Setter
    public static class DatabaseStatus {
        // 정보시스템ID
        private Long systemId;
        // 정보시스템명
        private String systemName;
        // 서버ID
        private Long serverId;
        // 서버명
        private String serverName;
        // Database ID
        private Long databaseId;
        // Database명
        private String databaseName;
        // DBMS 제품명
        private String engineName;
        // 버전
        private String version;
        // DBMS 용량 (MB)
        private Long dbSizeMb;
    }

}