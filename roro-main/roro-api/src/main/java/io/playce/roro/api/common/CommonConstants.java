/*
 * Copyright 2020 The Playce-RoRo Project.
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
 * Jeongho Baek     10월 21, 2020        First Draft.
 */
package io.playce.roro.api.common;

/**
 * <pre>
 *   공통 상수를 사용하기 위한 Class
 * </pre>
 *
 * @author Jeongho Baek
 * @version 1.0
 */
public class CommonConstants {

    public static final String AUTHENTICATION_TYPE_BEARER = "Bearer ";
    public static final String AUTHENTICATION_HEADER_NAME = "Authorization";

    public static final String JWT_ISSUER = "https://www.play-ce.io";

    public static final String MEDIA_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String MEDIA_TYPE_JSON = "application/json";
    public static final String MEDIA_TYPE_ZIP = "application/zip";
    public static final String EXCEL_EXTENSION_XLS = "xls";
    public static final String EXCEL_EXTENSION_XLSX = "xlsx";
    public static final String EXCEL_FILE_TYPE = "EXCEL";
    public static final String PDF_EXTENSION = "pdf";

    public static final String MESSAGE_SERVICE_EXCEL_NAME = "excel.filename.services";
    public static final String MESSAGE_SERVER_EXCEL_NAME = "excel.filename.servers";
    public static final String MESSAGE_MIDDLEWARE_EXCEL_NAME = "excel.filename.middlewares";
    public static final String MESSAGE_DATABASE_EXCEL_NAME = "excel.filename.databases";
    public static final String MESSAGE_APPLICATION_EXCEL_NAME = "excel.filename.applications";

    public static final int EXCEL_HEADER_FIRST_ROW_INDEX = 0;
    public static final int EXCEL_HEADER_SECOND_ROW_INDEX = 1;
    public static final int EXCEL_BODY_FIRST_ROW_INDEX = 1;
    public static final int EXCEL_BODY_SECOND_ROW_INDEX = 2;
    public static final int EXCEL_COLUMN_FIRST_ROW_INDEX = 0;

    public static final String INVENTORY_FILE_UPLOAD_DIR = "/inventory";
    public static final String APPLICATION_FILE_UPLOAD_DIR = "/application";
    public static final String MIGRATION_FILE_UPLOAD_DIR = "/migration";

    public static final String INVENTORY_EXCEL_TEMPLATE = "RoRo-Inventory-Template.xlsx";

    public static final String DISCOVERED_SERVER = "DiscoveredServer";
    public static final String DISCOVERED_SERVER_TABLE = "DiscoveredServerTable";
    public static final String DISCOVERED_DATABASE = "DiscoveredDatabase";
    public static final String DISCOVERED_DATABASE_TABLE = "DiscoveredDatabaseTable";
    public static final String ASSESSMENT_TABLE = "AssessmentJobsTable";
    public static final String MIGRATION_TABLE = "MigrationJobsTable";

    public static final String INVENTORY = "Inventory";

    public static final String PUBLIC_AGENCY_REPORT = "Assessment_Report_Public_Agency_Form";
    public static final String RORO_REPORT = "Assessment_Report_RoRo_Form";

    public static final String LOCALE_KOREAN_LANGUAGE = "ko";
    public static final String LOCALE_ENGLISH_LANGUAGE = "en";

    public static final String YES = "Y";
    public static final String NO = "N";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String UPLOAD_STATUS_TYPE_CODE_SUCCESS = "SUCC";
    public static final String UPLOAD_STATUS_TYPE_CODE_FAIL = "FAIL";

    /**
     * TODO
     * {@link io.playce.roro.common.code.Domain1001} 로 대체 필요
     */
    public static final String INVENTORY_TYPE_CODE_SERVER = "SVR";
    public static final String INVENTORY_TYPE_CODE_MIDDLEWARE = "MW";
    public static final String INVENTORY_TYPE_CODE_APPLICATION = "APP";
    public static final String INVENTORY_TYPE_CODE_DATABASE = "DBMS";

    public static final String SERVICE_TYPE_CODE = "SERV";

    // Default Service Common Code
    public static final String DEFAULT_SERVICE_NAME = "Default Service";
    public static final String DEFAULT_SERVICE_BUSINESS_CATEGORY_CODE = "DEFAULT-SERV";
    public static final String DEFAULT_SERVICE_BUSINESS_CATEGORY_NAME = "DEFAULT_SERVICE";
    public static final String DEFAULT_SERVICE_CUSTOMER_SERVICE_CODE = "SERV-001";
    public static final String DEFAULT_SERVICE_CUSTOMER_SERVICE_NAME = "DEFAULT_SERVICE";

    // excel download resource name
    public static final String EXCEL_RESOURCE_SERVICE_NAME = "Service";
    public static final String EXCEL_RESOURCE_SERVER_NAME = "Server";
    public static final String EXCEL_RESOURCE_MIDDLEWARE_NAME = "Middleware";
    public static final String EXCEL_RESOURCE_APPLICATION_NAME = "Application";
    public static final String EXCEL_RESOURCE_DATABASE_NAME = "Database";

    // Cloud Readiness Assessment Result
    public static final String CLOUD_READINESS_MIGRATION_RISK = "Migration Risk";
    public static final String CLOUD_READINESS_BUSINESS_FITNESS = "Business Fitness";
    public static final String CLOUD_READINESS_TECHNICAL_FITNESS = "Technical Fitness";
    public static final String CLOUD_READINESS_MIGRATION_FITNESS = "Migration Fitness";
    public static final String CLOUD_READINESS_EXCEL_REPORT_NAME = "Cloud_Readiness_Survey_Result";
    public static final String CLOUD_READINESS_EXCEL_TEMPLATE_NAME = "Cloud_Readiness_Survey_Template";
    public static final String HOST_SCAN_EXCEL_TEMPLATE_NAME = "Discovered_Ip_Address";

    // Insights
    public static final String INSIGHTS_EXCEL_REPORT_NAME = "Insights";
    public static final String INSIGHTS_PDF_GUIDE_NAME = "Insights_Reference_Guide";

    // Third Party
    public static final String THIRD_PARTY_EXCEL_REPORT_NAME = "Third-Party-Solutions";

    // Replace HOST to X-FORWARDED-HOST
    public static final String ORIGIN_HOST = "ORIGIN_HOST";
    public static final String RORO_HOST = "RORO_HOST";

    public static final String RORO_LOG_NAME = "RoRo_Support_Zip";

    public static final String EXCEL_PW_MSG = "{EDIT_WHEN_CHANGED}";
}
