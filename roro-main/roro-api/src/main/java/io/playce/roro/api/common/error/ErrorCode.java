/*
 * Copyright 2019 The Playce-RoRo Project.
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
 * Author            Date                  Description
 * ---------------  ----------------      ------------
 * Jeongho Baek      10월 14, 2020          First Draft.
 */
package io.playce.roro.api.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.stream.Stream;

/**
 * <pre>
 *   Error Code는 한 곳에서 정리하도록 한다. 흩어져서 처리하는 경우 중복되는 경우가 있을 수 있다.
 * </pre>
 *
 * @author Jeongho Baek
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통 에러
    BAD_REQUEST("COMMON_001", "error.common.badRequest", ""),
    UNAUTHORIZED("COMMON_002", "error.common.unauthorized", ""),
    FORBIDDEN("COMMON_003", "error.common.forbidden", ""),
    METHOD_NOT_ALLOWED("COMMON_004", "error.common.methodNotAllowed", ""),
    UNSUPPORTED_MEDIA_TYPE("COMMON_005", "error.common.unsupportedMediaType", ""),
    INTERNAL_SERVER_ERROR("COMMON_006", "error.common.internalServerError", ""),
    UNKNOWN_ERROR("COMMON_007", "error.common.unknownError", ""),
    INVALID_INPUT_VALUE("COMMON_008", "error.common.invalidInputValue", ""),
    RESOURCE_NOT_FOUND("COMMON_009", "error.common.resourceNotFound", ""),
    RESOURCE_IN_USE("COMMON_010", "error.common.resourceInUse", ""),
    INVALID_FILE_TYPE("COMMON_011", "error.common.invalidFileType", ""),
    FAIL_UPLOAD_FILE("COMMON_012", "error.common.fileUploadFail", ""),
    FAIL_DOWNLOAD_FILE("COMMON_013", "error.common.fileDownloadFail", ""),
    CONSTRAINT_VIOLATION("COMMON_014", "error.common.constraintViolation", ""),
    ILLEGAL_ARGUMENT("COMMON_015", "error.common.illegalArgument", ""),
    EXCEL_CREATE_FAILED("COMMON_016", "error.common.excelCreateFailed", ""),

    FAIL_SAVE_FILE("COMMON_017", "error.common.saveFile", ""),

    // 인증
    USER_NOT_FOUND("AUTH_001", "error.auth.userNotFound", ""),
    PASSWORD_INCORRECT("AUTH_002", "error.auth.passwordIncorrect", ""),
    ACCOUNT_LOCKED("AUTH_003", "error.auth.accountLocked", ""),
    AUTH_HEADER_BLANK("AUTH_004", "error.auth.authHeaderBlank", ""),
    INVALID_AUTH_HEADER_SIZE("AUTH_005", "error.auth.invalidAuthHeaderSize", ""),
    INVALID_JWT_SIGN("AUTH_006", "error.auth.invalidJwtSignature", ""),
    INVALID_JWT_FORM("AUTH_007", "error.auth.invalidJwtForm", ""),
    EXPIRED_JWT("AUTH_008", "error.auth.expiredJwt", ""),
    UNSUPPORTED_JWT("AUTH_009", "error.auth.unsupportedJwt", ""),
    JWT_EMPTY_CLAIMS("AUTH_010", "error.auth.jwtEmptyClaims", ""),
    NOT_TRUSTED_TOKEN_SET("AUTH_011", "error.auth.notTrustedTokenSet", ""),
    NOT_PERMITTED_REFRESH_TOKEN("AUTH_012", "error.auth.notPermittedToken", ""),
    NOT_PERMITTED_ACCESS_TOKEN("AUTH_013", "error.auth.notPermittedAccessToken", ""),

    // Inventory
    INVENTORY_SERVICE_NOT_FOUND("INV_001", "error.inventory.service.notFound", ""),
    INVENTORY_SERVICE_DELETED_FAIL("INV_002", "error.inventory.service.deletedFail", ""),
    INVENTORY_SERVICE_DUPLICATE_NAME("INV_003", "error.inventory.service.duplicate", ""),
    INVENTORY_SERVER_DELETED_FAIL("INV_004", "error.inventory.server.deletedFail", ""),
    INVENTORY_SERVER_NOT_FOUND("INV_005", "error.inventory.server.notFound", ""),
    INVENTORY_SERVER_DUPLICATE_IP_PORT("INV_006", "error.inventory.server.duplicate", ""),
    INVENTORY_MIDDLEWARE_NOT_FOUND("INV_007", "error.inventory.middleware.notFound", ""),
    INVENTORY_MIDDLEWARE_DUPLICATE("INV_008", "error.inventory.middleware.duplicate", ""),
    INVENTORY_APPLICATION_NOT_FOUND("INV_009", "error.inventory.application.notFound", ""),
    INVENTORY_APPLICATION_DUPLICATE("INV_010", "error.inventory.application.duplicate", ""),
    INVENTORY_DATABASE_NOT_FOUND("INV_011", "error.inventory.database.notFound", ""),
    INVENTORY_SERVER_SERVICE_REQUIRED("INV_012", "error.inventory.server.required", ""),
    INVENTORY_DATABASE_DUPLICATE_PORT("INV_013", "error.inventory.database.duplicate", ""),
    INVENTORY_SERVICE_DEFAULT_DELETE_FAIL("INV_014", "error.inventory.service.defaultDeleteFail", ""),
    INVENTORY_SERVER_INVALID_KEY_FILE("INV_015", "error.inventory.server.invalid.keyFile", ""),
    INVENTORY_SERVER_INVALID_KEY_FILE_SIZE("INV_016", "error.inventory.server.invalid.keyFileSize", ""),
    INVENTORY_TOPOLOGY_UNSUPPORTED_TYPE("INV_017", "error.inventory.topology.unsupportedType", ""),
    INVENTORY_CUSTOMER_CODE_DUPLICATE("INV_018", "error.inventory.customer.code.duplicate", ""),
    INVENTORY_SERVICE_JSON_NOT_SUPPORT("INV_019", "error.inventory.service.json.notSupport", ""),
    INVENTORY_SERVER_ROOT_PASS_REQUIRED("INV_020", "error.inventory.server.rootPassword.required", ""),
    INVENTORY_SERVER_CRON_EXPRESSION_NOT_VALID("INV_021", "error.inventory.server.cronExpression.notValid", ""),
    INVENTORY_INVALID_USER_PASSWORD("INV_022", "error.inventory.invalid.userPassword", ""),
    INVENTORY_DATABASE_NOT_MODIFY("INV_023", "error.inventory.database.notModify", ""),
    INVENTORY_SERVER_NOT_MODIFY("INV_024", "error.inventory.server.notModify", ""),


    // Assessment
    ASSESSMENT_NOT_FOUND("ASSESS_001", "error.assessment.notFound", ""),
    ASSESSMENT_CANCEL_INVALID_STATUS("ASSESS_002", "error.assessment.cancel.invalidStatus", ""),
    ASSESSMENT_DELETE_INVALID_STATUS("ASSESS_003", "error.assessment.delete.invalidStatus", ""),
    ASSESSMENT_DUPLICATED("ASSESS_004", "error.assessment.duplicated", ""),
    ASSESSMENT_ZIP_FAILED("ASSESS_005", "error.assessment.zipFailed", ""),
    ASSESSMENT_FILTER_NOT_FOUND("ASSESS_006", "error.assessment.filter.notFound", ""),
    ASSESSMENT_RESULT_UPLOAD_NOT_SUPPORT("ASSESS_007", "error.assessment.result.uploadNotSupport", ""),

    // Manual upload assessment
    MANUAL_ASSESSMENT_INVALID_FILE("M_ASSESS_001", "error.manual.assessment.invalidFile", ""),
    MANUAL_ASSESSMENT_PARSING_FAILED("M_ASSESS_002", "error.manual.assessment.parsingFailed", ""),
    MANUAL_ASSESSMENT_PROCESSING_FAILED("M_ASSESS_003", "error.manual.assessment.processingFailed", ""),


    // Target Cloud
    TC_AWS_AUTH_FAIL("TC_AWS_001", "error.tc.aws.authFail", ""),
    TC_AWS_EC2_ERROR("TC_AWS_002", "error.tc.aws.ec2.error", ""),
    TC_GCP_AUTH_FAIL("TC_GCP_001", "error.tc.gcp.authFail", ""),
    TC_GCP_GCE_ERROR("TC_GCP_002", "error.tc.gcp.gce.error", ""),

    TC_UNSUPPORTED_TYPE("TC_001", "error.tc.unsupportedType", ""),
    TC_CREDENTIAL_NOT_FOUND("TC_002", "error.tc.credentialNotFound", ""),
    TC_KEY_FILE_NOT_FOUND("TC_003", "error.tc.keyFileNotFound", ""),
    TC_CREDENTIAL_IN_USE("TC_004", "error.tc.credentialInUse", ""),
    TC_INVALID_KEY_FILE("TC_005", "error.tc.invalidKeyFile", ""),
    TC_INVALID_CREDENTIAL("TC_006", "error.tc.invalidCredential", ""),

    // Migration
    MIGRATION_WINDOWS_NOT_SUPPORTED("MIG_001", "error.migration.windowsNotSupported", ""),
    MIGRATION_ASSESSMENT_NOT_FOUND("MIG_002", "error.migration.assessmentNotFound", ""),
    MIGRATION_PRE_CONFIG_NOT_FOUND("MIG_003", "error.migration.preConfigNotFound", ""),
    MIGRATION_SERVER_NOT_FOUND("MIG_004", "error.inventory.server.notFound", ""),
    MIGRATION_CREDENTIAL_NOT_FOUND("MIG_005", "error.tc.credentialNotFound", ""),
    MIGRATION_PROJECT_ID_NOT_FOUND("MIG_006", "error.migration.projectIdNotFound", ""),
    MIGRATION_PRE_CONFIG_IN_USE("MIG_007", "error.migration.preConfigInUse", ""),
    MIGRATION_CANCEL_INVALID_STATUS("MIG_008", "error.migration.cancel.invalidStatus", ""),
    MIGRATION_INVALID_IP_ADDRESS("MIG_009", "error.migration.ip.invalid", ""),
    MIGRATION_SUBNET_NOT_FOUND("MIG_010", "error.migration.subnet.notFound", ""),
    MIGRATION_IMAGE_ID_NOT_DEFINED("MIG_011", "error.migration.imageIdNotDefined", ""),
    MIGRATION_NOT_DEFINED_IP_ADDRESS("MIG_012", "error.migration.ip.notDefined", ""),
    MIGRATION_DUPLICATED("MIG_013", "error.migration.duplicated", ""),
    MIGRATION_NOT_FOUND("MIG_014", "error.migration.notFound", ""),
    MIGRATION_DELETE_INVALID_STATUS("MIG_015", "error.migration.delete.invalidStatus", ""),

    // Package
    PACKAGE_NOT_FOUND("PGK_001", "error.package.notFound", ""),

    // Subscription
    SUBSCRIPTION_NOT_FOUND("SUBSCRIPTION_001", "error.subscription.notFound", ""),
    SUBSCRIPTION_INVALID("SUBSCRIPTION_002", "error.subscription.invalid", ""),
    SUBSCRIPTION_SIGNATURE_NOTMATCH("SUBSCRIPTION_003", "error.subscription.signatureNotMatch", ""),
    SUBSCRIPTION_EXPIRED("SUBSCRIPTION_004", "error.subscription.expired", ""),
    SUBSCRIPTION_COUNT_EXCEEDED("SUBSCRIPTION_005", "error.subscription.countExceeded", ""),
    SUBSCRIPTION_NOT_ALLOWED1("SUBSCRIPTION_006", "error.subscription.notAllowed1", ""),
    SUBSCRIPTION_NOT_ALLOWED2("SUBSCRIPTION_007", "error.subscription.notAllowed2", ""),

    //NetworkFilter
    NETWORKFILTER_DUPLICATED_NAME("NETWORKFILTER_001", "error.networkfilter.duplicated", ""),

    // Cloud-Readiness
    CLOUD_READINESS_INVALID("CR_001", "error.cloudreadiness.invalid", ""),
    CLOUD_READINESS_NOT_FOUND("CR_002", "error.cloudreadiness.notFound", ""),
    CLOUD_READINESS_SCAN_INCOMPLETE("CR_003", "error.cloudreadiness.scanIncomplete", ""),

    // host-scan
    HOST_SCAN_DELETE_FAIL("HOST_001", "error.host.scan.deleteFail", ""),
    HOST_SCAN_INCOMPLETE_SCAN("HOST_002", "error.host.scan.incompleteScan", ""),
    HOST_SCAN_BELOW_BCLASS("HOST_003", "error.host.scan.belowBClass", ""),

    // Third Party
    THIRD_PARTY_SOLUTION_DUPLICATED_NAME("TR_001", "error.third.party.duplicated", ""),
    THIRD_PARTY_INVALID_PARAM("TR_002", "error.third.party.invalidParam", ""),

    // Project
    PROJECT_DUPLICATED_NAME("PROJECT_001", "error.project.duplicated", ""),

    // k8s
    K8s_CLUSTER_NAME_DUPLICATED("K8S_001", "error.k8s.cluster.duplicated", "")
    ;

    private final String code;

    private final String message;

    @Setter
    private String convertedMessage;

    public static Stream<ErrorCode> stream() {
        return Stream.of(ErrorCode.values());
    }

}
//end of ErrorCode.java