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

import io.playce.roro.api.common.i18n.LocaleMessageConvert;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.inventory.service.MiddlewareService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.inventory.database.DeployDatasourceList;
import io.playce.roro.common.dto.inventory.middleware.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

import static io.playce.roro.api.common.CommonConstants.EXCEL_RESOURCE_MIDDLEWARE_NAME;
import static io.playce.roro.api.common.CommonConstants.MEDIA_TYPE_EXCEL;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@RestController
@RequestMapping(value = "/api/projects/{projectId}/inventory")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class MiddlewareController {

    private final MiddlewareService middlewareService;
    private final LocaleMessageConvert localeMessageConvert;
    private final ProjectService projectService;

    @Operation(summary = "미들웨어 목록 조회", description = "등록된 미들웨어 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/middlewares")
    public ResponseEntity<?> getMiddlewares(@PathVariable Long projectId,
                                            @RequestParam(required = false) Long serviceId,
                                            @RequestParam(required = false) Long serverId) {
        List<MiddlewareResponse> middlewareList = middlewareService.getMiddlewares(projectId, serviceId, serverId);
        return ResponseEntity.ok(middlewareList);
    }

    @Operation(summary = "미들웨어 등록", description = "미들웨어를 등록한다.")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/middlewares", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMiddleware(@PathVariable Long projectId,
                                              @RequestPart("middleware") MiddlewareRequest middlewareRequest,
                                              @RequestPart(value = "keyFile", required = false) MultipartFile keyFile) {
        MiddlewareSimpleResponse response = middlewareService.createMiddleware(projectId, middlewareRequest, keyFile);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "미들웨어 수정", description = "등록된 미들웨어를 수정한다.")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/middlewares/{middlewareInventoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> modifyMiddleware(@PathVariable Long projectId,
                                              @PathVariable Long middlewareInventoryId,
                                              @RequestPart("middleware") MiddlewareRequest middlewareRequest,
                                              @RequestPart(value = "keyFile", required = false) MultipartFile keyFile) {
        MiddlewareSimpleResponse response = middlewareService.modifyMiddleware(projectId, middlewareInventoryId, middlewareRequest, keyFile);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "미들웨어 삭제", description = "등록된 미들웨어를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/middlewares/{middlewareInventoryId}")
    public ResponseEntity<?> removeMiddleware(@PathVariable Long projectId,
                                              @PathVariable Long middlewareInventoryId,
                                              @RequestParam(required = false) boolean isPreventAutoDiscovery) {
        middlewareService.removeMiddleware(projectId, middlewareInventoryId, isPreventAutoDiscovery);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "미들웨어 상세 조회", description = "미들웨어 상세 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/middlewares/{middlewareInventoryId}")
    public ResponseEntity<?> middlewareDetail(@PathVariable Long projectId,
                                              @PathVariable Long middlewareInventoryId) {
        MiddlewareDetailResponse middlewareDetail = middlewareService.getMiddlewareDetail(projectId, middlewareInventoryId);
        return ResponseEntity.ok(middlewareDetail);
    }

    @Operation(summary = "미들웨어 목록 Excel 다운로드", description = "미들웨어 목록을 Excel로 다운로드 한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/middlewares/excel")
    public ResponseEntity<InputStreamResource> excelDownload(@PathVariable Long projectId,
                                                             @RequestParam(value = "serviceId", required = false) Long serviceId,
                                                             @RequestParam(value = "serverId", required = false) Long serverId) {
        ByteArrayInputStream in = middlewareService.getMiddlewareListExcel(projectId, serviceId, serverId);

        String filename = ExcelUtil.generateExcelFileName(EXCEL_RESOURCE_MIDDLEWARE_NAME);
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "미들웨어 인스턴스 목록 조회", description = "등록된 미들웨어의 인스턴스 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/middlewares/{middlewareInventoryId}/instances")
    public ResponseEntity<?> getMiddlewareInstances(@PathVariable Long projectId,
                                                    @PathVariable Long middlewareInventoryId) {
        List<InstanceResponse> instanceList = middlewareService.getInstances(projectId, middlewareInventoryId);
        return ResponseEntity.ok(instanceList);
    }

    @Operation(summary = "미들웨어 인스턴스 상세 조회", description = "미들웨어 인스턴스 상세를 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/middlewares/{middlewareInventoryId}/instances/{middlewareInstanceId}")
    public ResponseEntity<?> middlewareInstanceDetail(@PathVariable Long projectId,
                                                      @PathVariable Long middlewareInventoryId,
                                                      @PathVariable Long middlewareInstanceId) {
        InstanceDetailResponse instanceDetail = middlewareService.getMiddlewareInstanceDetail(projectId, middlewareInventoryId, middlewareInstanceId);
        return ResponseEntity.ok(instanceDetail);
    }

    @Operation(summary = "미들웨어 인스턴스 삭제", description = "등록된 미들웨어 인스턴스를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/middlewares/instances/{middlewareInstanceId}")
    public ResponseEntity<?> removeMiddlewareInstance(@PathVariable Long projectId,
                                                      @PathVariable Long middlewareInstanceId) {
        middlewareService.removeMiddlewareInstance(projectId, middlewareInstanceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "인스턴스에 배포된 애플리케이션 목록 조회", description = "미들웨어 인스턴스에 배포된 애플리케이션 목록 조회")
    @ApiResponse(responseCode = "200")
    @GetMapping("/middlewares/instances/{middlewareInstanceId}/applications")
    public ResponseEntity<?> getMiddlewareInstanceApplications(@PathVariable Long projectId,
                                                               @PathVariable Long middlewareInstanceId) {
        List<DeployApplicationList> deployApplicationListList = middlewareService.getDeployApplicationList(projectId, middlewareInstanceId);
        return ResponseEntity.ok(deployApplicationListList);
    }

    @Operation(summary = "인스턴스에 설정된 데이터베이스 목록 조회", description = "미들웨어 인스턴스에 설정된 데이터베이스 목록 조회")
    @ApiResponse(responseCode = "200")
    @GetMapping("/middlewares/instances/{middlewareInstanceId}/databases")
    public ResponseEntity<?> getMiddlewareInstanceDatasources(@PathVariable Long projectId,
                                                              @PathVariable Long middlewareInstanceId) {
        List<DeployDatasourceList> deployDatasourceList = middlewareService.getDeployDatasourceList(projectId, middlewareInstanceId);
        return ResponseEntity.ok(deployDatasourceList);
    }

    @Operation(summary = "미들웨어 엔진에 속한 애플리케이션 조회", description = "미들웨어 엔진에 속한 애플리케이션 목록 조회 - 서비스 Tree에 사용")
    @ApiResponse(responseCode = "200")
    @GetMapping("/middlewares/{middlewareInventoryId}/applications")
    public ResponseEntity<?> getMiddlewareEngineApplications(@PathVariable Long projectId, @PathVariable Long middlewareInventoryId) {
        List<DeployApplicationList> deployApplicationList = middlewareService.getMiddlewareDeployApplicationList(projectId, middlewareInventoryId);
        return ResponseEntity.ok(deployApplicationList);
    }

    @Operation(summary = "미들웨어 엔진에 속한 데이터소스 조회", description = "미들웨어 엔진에 속한 데이터소스 목록 조회 - 서비스 Tree에 사용")
    @ApiResponse(responseCode = "200")
    @GetMapping("/middlewares/{middlewareInventoryId}/databases")
    public ResponseEntity<?> getMiddlewareEngineDatabases(@PathVariable Long projectId, @PathVariable Long middlewareInventoryId) {
        List<DeployDatasourceList> deployDatasourceList = middlewareService.getMiddlewareDeployDatasourceList(projectId, middlewareInventoryId);
        return ResponseEntity.ok(deployDatasourceList);
    }
}
//end of MiddlewareController.java
