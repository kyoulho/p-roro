/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * SangCheon Park   Mar 14, 2022		    First Draft.
 */
package io.playce.roro.common.dto.migration.enums;

/**
 * <pre>
 * 마이그레이션 상태 정보 정의
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
public enum StatusType {

    /**
     * Migration 요청 예약 상태
     */
    RESERVED("RESERVED"),

    /**
     * Migration 요청 상태
     */
    READY("READY"),

    /**
     * 소스 서버의 디스크 내용을 raw 파일로 저장 중
     */
    CREATE_RAW_FILES("CREATE_RAW_FILES"),

    /**
     * 소스 서버의 디스크 내용을 raw 파일로 저장 완료
     */
    CREATED_RAW_FILES("CREATED_RAW_FILES"),

    /**
     * raw 파일을 S3로 업로드
     */
    UPLOAD_TO_S3("UPLOAD_TO_S3"),

    /**
     * S3로부터 raw 파일을 다운로드
     */
    DOWNLOAD_FROM_S3("DOWNLOAD_FROM_S3"),

    /**
     * 다운로드된 raw 파일을 변환
     */
    CONVERTING("CONVERTING"),

    /**
     * 변환된 파일을 이용하여 인스턴스 생성
     */
    INITIATE_INSTANCE("INITIATE_INSTANCE"),

    /**
     * Sync 요청 상태
     */
    SYNC_READY("SYNC_READY"),

    /**
     * Sync 진행 상태
     */
    SYNC_PENDING("SYNC_PENDING"),

    /**
     * EBS 추가 작업 진행 상태
     */
    ATTACHING_VOLUME("ATTACHING_VOLUME"),

    /**
     * EBS 추가 작업 완료 상태
     */
    ATTACHED_VOLUME("ATTACHED_VOLUME"),

    /**
     * Machine Image 생성 작업 진행 상태
     */
    CREATING_MACHINE_IMAGE("CREATING_MACHINE_IMAGE"),

    /**
     * Machine Image 생성 작업 완료 상태
     */
    CREATED_MACHINE_IMAGE("CREATED_MACHINE_IMAGE"),


    /**
     * AMI 생성 작업 진행 상태
     */
    CREATING_AMI("CREATING_AMI"),

    /**
     * AMI 생성 작업 완료 상태
     */
    CREATED_AMI("CREATED_AMI"),

    /**
     * Instance 종료 작업 진행 상태
     */
    TERMINATING_INSTANCE("TERMINATING_INSTANCE"),

    /**
     * Instance 종료 작업 완료 상태
     */
    TERMINATED_INSTANCE("TERMINATED_INSTANCE"),

    /**
     * Instance 생성 작업 진행 상태
     */
    CREATING_INSTANCE("CREATING_INSTANCE"),

    /**
     * Migration 취소 요청
     */
    CANCELLING("CANCELLING"),

    /**
     * Migration 취소됨
     */
    CANCELLED("CANCELLED"),

    /**
     * Migration 에러
     */
    ERROR("ERROR"),

    /**
     * Migration 완료됨
     */
    COMPLETED("COMPLETED"),

    /**
     * COMPRESSING 작업 진행
     */
    COMPRESSING("COMPRESSING"),
    /**
     * raw 파일을 GCP Storage로 업로드
     */
    UPLOAD_TO_STORAGE("UPLOAD_TO_STORAGE"),
    /**
     * DISK Image 생성 진행중
     */
    CREATING_DISK_IMAGE("CREATING_DISK_IMAGE"),
    /**
     * DISK Image 생성 완료
     */
    CREATED_DISK_IMAGE("CREATED_DISK_IMAGE"),
    /**
     * DISK 생성 진행중
     */
    CREATING_DISK("CREATING_DISK"),
    /**
     * DISK 생성 완료
     */
    CREATED_DISK("CREATED_DISK"),
    /**
     * FIREWALL 생성 진행중
     */
    CREATING_FIREWALL("CREATING_FIREWALL");


    /**
     * The Description.
     */
    private String description;


    /**
     * Instantiates a new Status type.
     *
     * @param description the description
     */
    private StatusType(String description) {
        this.description = description;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return this.getDescription();
    }
}
//end of StatusType.java
