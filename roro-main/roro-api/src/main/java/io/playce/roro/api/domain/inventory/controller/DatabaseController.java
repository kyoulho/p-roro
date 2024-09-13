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
import io.playce.roro.api.domain.inventory.service.DatabaseService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.inventory.database.*;
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

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static io.playce.roro.api.common.CommonConstants.EXCEL_RESOURCE_DATABASE_NAME;
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
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DatabaseController {

    private final DatabaseService databaseService;
    private final ProjectService projectService;
    private final LocaleMessageConvert localeMessageConvert;

    @Operation(summary = "데이터베이스 목록 조회", description = "데이터베이스 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/databases")
    public ResponseEntity<?> getDatabases(@PathVariable Long projectId,
                                          @RequestParam(required = false) Long serviceId,
                                          @RequestParam(required = false) Long serverId) {
        List<DatabaseEngineListResponseDto> result = databaseService.getDatabaseEngines(projectId, serviceId, serverId);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "데이터베이스 인스턴스 목록 조회", description = "데이터베이스 인스턴스 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/databases/{databaseInventoryId}/instances")
    public ResponseEntity<?> getDatabaseInstances(@PathVariable Long projectId, @PathVariable Long databaseInventoryId) {
        List<DatabaseInstanceListResponseDto> result = databaseService.getDatabaseInstances(projectId, databaseInventoryId);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @Operation(summary = "데이터베이스 상세 조회", description = "데이터베이스 상세 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/databases/{databaseInventoryId}")
    public ResponseEntity<?> getDatabase(@PathVariable Long projectId, @PathVariable Long databaseInventoryId) {
        DatabaseEngineResponseDto result = databaseService.getDatabaseEngine(projectId, databaseInventoryId);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @Operation(summary = "데이터베이스 등록", description = "데이터베이스를 등록한다.")
    @ApiResponse(responseCode = "201")
    @PostMapping("/databases")
    public ResponseEntity<?> createDatabase(@PathVariable Long projectId,
                                            @RequestBody DatabaseRequest databaseRequest) {
        Map<String, Object> databaseMap = databaseService.createDatabase(projectId, databaseRequest);

        return new ResponseEntity<>(databaseMap, HttpStatus.CREATED);
    }

    @Operation(summary = "데이터베이스 수정", description = "데이터베이스를 수정한다.")
    @ApiResponse(responseCode = "204")
    @PutMapping("/databases/{databaseInventoryId}")
    public ResponseEntity<?> modifyDatabase(@PathVariable Long projectId,
                                            @PathVariable Long databaseInventoryId,
                                            @RequestBody DatabaseRequest databaseRequest) {
        Map<String, Object> databaseMap = databaseService.modifyDatabase(projectId, databaseInventoryId, databaseRequest);

        return new ResponseEntity<>(databaseMap, HttpStatus.OK);
    }

    @Operation(summary = "데이터베이스 삭제", description = "데이터베이스를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/databases/{databaseInventoryId}")
    public ResponseEntity<?> removeDatabase(@PathVariable Long projectId, @PathVariable Long databaseInventoryId) {
        databaseService.removeDatabase(projectId, databaseInventoryId);

        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "데이터베이스 인스턴스 상세 조회", description = "데이터베이스 인스턴스를 상세 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/databases/{databaseInventoryId}/instances/{databaseInstanceId}")
    public ResponseEntity<?> getDatabaseInstance(@PathVariable Long projectId,
                                                 @PathVariable Long databaseInventoryId,
                                                 @PathVariable Long databaseInstanceId) {
        DatabaseInstanceResponseDto result = databaseService.getDatabaseInstance(projectId, databaseInventoryId, databaseInstanceId);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "데이터베이스 인스턴스 미들웨어 목록 조회", description = "데이터베이스 인스턴스를 사용하고 있는 미들웨어를 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/databases/{databaseInventoryId}/instances/{databaseInstanceId}/middlewares")
    public ResponseEntity<?> getDatabaseInstanceMiddleware(@PathVariable Long projectId,
                                                           @PathVariable Long databaseInventoryId,
                                                           @PathVariable Long databaseInstanceId) {
        List<DatabaseInstanceMiddlewareResponseDto> databaseInstanceMiddlewareResponse =
                databaseService.getDatabaseInstanceMiddleware(projectId, databaseInventoryId, databaseInstanceId);

        return new ResponseEntity<>(databaseInstanceMiddlewareResponse, HttpStatus.OK);
    }

    // Todo 구현해야 됨.
    @Operation(summary = "데이터베이스 인스턴스 애플리케이션 상세 조회", description = "데이터베이스 인스턴스를 사용하고 있는 애플리케이션을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/databases/{databaseInventoryId}/instances/{databaseInstanceId}/applications")
    public ResponseEntity<?> getDatabaseInstanceApplication(@PathVariable Long projectId,
                                                            @PathVariable Long databaseInventoryId,
                                                            @PathVariable Long databaseInstanceId) {
        List<DatabaseInstanceApplicationResponseDto> databaseInstanceApplicationResponse =
                databaseService.getDatabaseInstanceApplications(projectId, databaseInventoryId, databaseInstanceId);

        return new ResponseEntity<>(databaseInstanceApplicationResponse, HttpStatus.OK);
    }

    @Operation(summary = "데이터베이스 인스턴스 삭제", description = "데이터베이스 인스턴스를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/databases/instances/{databaseInstanceId}")
    public ResponseEntity<?> removeDatabaseInstance(@PathVariable Long projectId,
                                                    @PathVariable Long databaseInstanceId) {
        databaseService.removeDatabaseInstance(projectId, databaseInstanceId);

        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "데이터베이스 목록 Excel 다운로드", description = "데이터베이스 목록을 Excel로 다운로드 한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/databases/excel")
    public ResponseEntity<InputStreamResource> excelDownload(@PathVariable Long projectId,
                                                             @RequestParam(value = "serviceId", required = false) Long serviceId,
                                                             @RequestParam(value = "serverId", required = false) Long serverId) {
        ByteArrayInputStream in = databaseService.getDatabaseListExcel(projectId, serviceId, serverId);

        String filename = ExcelUtil.generateExcelFileName(EXCEL_RESOURCE_DATABASE_NAME);
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }

}
//end of DatabaseController.java
