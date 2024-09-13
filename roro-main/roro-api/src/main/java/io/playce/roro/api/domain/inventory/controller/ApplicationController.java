/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       11월 04, 2021            First Draft.
 */
package io.playce.roro.api.domain.inventory.controller;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.i18n.LocaleMessageConvert;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.inventory.service.ApplicationService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.inventory.application.*;
import io.playce.roro.common.dto.inventory.middleware.InstanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

import static io.playce.roro.api.common.CommonConstants.EXCEL_RESOURCE_APPLICATION_NAME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/projects/{projectId}/inventory", produces = APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final LocaleMessageConvert localeMessageConvert;
    private final ProjectService projectService;

    /**
     * <pre>
     * 등록된 애플리케이션 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param serviceId
     * @param serverId
     *
     * @return
     */
    @Operation(summary = "애플리케이션 목록 조회", description = "등록된 애플리케이션 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApplicationResponse.class))))
    @GetMapping(value = "/applications")
    public ResponseEntity<?> getApplications(@PathVariable Long projectId,
                                             @RequestParam(value = "serviceId", required = false) Long serviceId,
                                             @RequestParam(value = "serverId", required = false) Long serverId) {
        List<ApplicationResponse> applicationList = applicationService.getApplications(projectId, serviceId, serverId);
        return ResponseEntity.ok(applicationList);
    }

    /**
     * <pre>
     * 애플리케이션 상세정보를 조회한다.
     * </pre>
     *
     * @param projectId
     * @param applicationId
     *
     * @return
     */
    @Operation(summary = "애플리케이션 상세 조회", description = "애플리케이션 상세정보를 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ApplicationDetailResponse.class)))
    @GetMapping(value = "/applications/{applicationId}")
    public ResponseEntity<?> getApplication(@PathVariable Long projectId, @PathVariable Long applicationId) {
        ApplicationResponse application = applicationService.getApplication(projectId, applicationId);
        return ResponseEntity.ok(application);
    }

    /**
     * <pre>
     * 애플리케이션 Datasource를 조회한다.
     * </pre>
     *
     * @param projectId
     * @param applicationId
     *
     * @return
     */
    @Operation(summary = "애플리케이션 Datasource 조회", description = "애플리케이션 Datasource를 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApplicationDatasourceResponse.class))))
    @GetMapping(value = "/applications/{applicationId}/datasources")
    public ResponseEntity<?> getDatasources(@PathVariable Long projectId, @PathVariable Long applicationId) {
        List<ApplicationDatasourceResponse> applicationDatasourceResponse = applicationService.getDatasources(projectId, applicationId);
        return ResponseEntity.ok(applicationDatasourceResponse);
    }


    /**
     * <pre>
     * 신규 애플리케이션을 등록한다.
     * </pre>
     *
     * @param projectId
     * @param applicationRequest
     * @param analyzeFile
     *
     * @return
     */
    @Operation(summary = "애플리케이션 등록", description = "신규 애플리케이션 정보를 등록한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ApplicationSimpleResponse.class)))
    @PostMapping(value = "/applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createApplication(@PathVariable Long projectId,
                                                    @RequestPart("application") ApplicationRequest applicationRequest,
                                                    @RequestPart(value = "analyzeFile", required = false) MultipartFile analyzeFile,
                                                    @RequestPart(value = "keyFile", required = false) MultipartFile keyFile) throws Exception {

        Assert.notEmpty(applicationRequest.getServiceIds(), "Service IDs can not be empty.");
        Assert.notNull(applicationRequest.getServerInventoryId(), "Server ID can not be null.");

        ApplicationSimpleResponse response = applicationService.createApplication(projectId, applicationRequest, analyzeFile, keyFile);
        return ResponseEntity.ok(response);
    }

    /**
     * <pre>
     * 애플리케이션 정보를 수정한다.
     * </pre>
     *
     * @param projectId
     * @param applicationId
     * @param applicationRequest
     * @param analyzeFile
     *
     * @return
     *
     * @throws Exception
     */
    @Operation(summary = "애플리케이션 수정", description = "해당 애플리케이션 정보를 수정한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ApplicationSimpleResponse.class)))
    @PostMapping(value = "/applications/{applicationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> modifyApplication(@PathVariable Long projectId, @PathVariable Long applicationId,
                                               @RequestPart("application") ApplicationRequest applicationRequest,
                                               @RequestPart(value = "analyzeFile", required = false) MultipartFile analyzeFile,
                                               @RequestPart(value = "keyFile", required = false) MultipartFile keyFile) throws Exception {

        Assert.notEmpty(applicationRequest.getServiceIds(), "Service IDs can not be empty.");
        Assert.notNull(applicationRequest.getServerInventoryId(), "Server ID can not be null.");

        ApplicationSimpleResponse response = applicationService.modifyApplication(projectId, applicationId, applicationRequest, analyzeFile, keyFile);
        return ResponseEntity.ok(response);
    }

    /**
     * <pre>
     * 애플리케이션을 삭제한다.
     * </pre>
     *
     * @param projectId
     * @param applicationId
     *
     * @return
     *
     * @throws Exception
     */
    @Operation(summary = "애플리케이션 삭제", description = "해당 애플리케이션을 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(value = "/applications/{applicationId}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long projectId, @PathVariable Long applicationId,
                                               @RequestParam(required = false) boolean isPreventAutoDiscovery) throws Exception {
        applicationService.deleteApplication(projectId, applicationId, isPreventAutoDiscovery);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * 애플리케이션 목록 Excel을 다운로드한다.
     * </pre>
     *
     * @param projectId
     *
     * @return
     */
    @Operation(summary = "애플리케이션 목록 Excel 다운로드", description = "조회한 애플리케이션 목록을 Excel로 다운로드한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/applications/excel")
    public ResponseEntity<InputStreamResource> excelDownload(@PathVariable Long projectId,
                                                             @RequestParam(value = "serviceId", required = false) Long serviceId,
                                                             @RequestParam(value = "serverId", required = false) Long serverId) {
        ByteArrayInputStream in = applicationService.getApplicationListExcel(projectId, serviceId, serverId);

        String filename = ExcelUtil.generateExcelFileName(EXCEL_RESOURCE_APPLICATION_NAME);
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(CommonConstants.MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }

    /**
     * <pre>
     * 애플리케이션 자동등록 방지 필드를 업데이트한다.
     * </pre>
     *
     * @param projectId
     * @param applicationId
     * @param autoRegisterProtectionYn
     *
     * @return
     */
    @Operation(summary = "애플리케이션 자동등록 방지 필드 업데이트", description = "애플리케이션 자동등록 방지 필드를 업데이트한다.")
    @ApiResponse(responseCode = "200")
    @PatchMapping(value = "/applications/{applicationId}/auto-register")
    public ResponseEntity<?> modifyApplication(@PathVariable Long projectId, @PathVariable Long applicationId,
                                               @RequestParam("autoRegisterProtectionYn") String autoRegisterProtectionYn) {
        applicationService.setAutoRegisterProtection(projectId, applicationId, autoRegisterProtectionYn);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "애플리케이션 상세 미들웨어 인스턴스 목록 조회", description = "애플리케이션 상세 미들웨어 인스턴스 목록 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/applications/{applicationId}/middlewares")
    public ResponseEntity<?> middlewareInstances(@PathVariable Long projectId, @PathVariable Long applicationId) {
        List<InstanceResponse> middlewareList = applicationService.getMiddlewareInstanceList(projectId, applicationId);
        return ResponseEntity.ok(middlewareList);
    }

    @Operation(summary = "애플리케이션 외부 연결 조회", description = "애플리케이션 외부 연결을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ApplicationExternalConnectionResponse.class)))
    @GetMapping(value = "/applications/{applicationId}/external-connections")
    @ResponseStatus(HttpStatus.OK)
    public List<ApplicationExternalConnectionResponse> getExternalConnection(@PathVariable Long projectId, @PathVariable Long applicationId) {
        return applicationService.getApplicationExternalConnections(projectId, applicationId);
    }
}
//end of ApplicationController.java