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
 * SangCheon Park   Mar 10, 2022		    First Draft.
 */
package io.playce.roro.api.domain.migration.controller;

import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.migration.service.MigrationService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.migration.*;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import static io.playce.roro.api.common.CommonConstants.MIGRATION_TABLE;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@RestController
@RequestMapping(value = "/api/projects/{projectId}/migrations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class MigrationController {

    private final MigrationService migrationService;
    private final ProjectService projectService;

    /**
     * <pre>
     * Migration 요청
     * </pre>
     *
     * @param projectId           the project id
     * @param migrationRequestDto the migrationRequestDto
     *
     * @return response entity
     */
    @Operation(summary = "Migration 요청", description = "Migration을 요청한다.")
    @ApiResponse(responseCode = "200")
    @PostMapping
    public ResponseEntity<?> migration(@PathVariable("projectId") Long projectId, @RequestBody MigrationRequestDto migrationRequestDto) {
        MigrationResponseDto response = migrationService.createMigration(projectId, migrationRequestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * <pre>
     * Migration Task 서버 목록 조회
     * </pre>
     *
     * @param projectId the project id
     *
     * @return response entity
     */
    @Operation(summary = "Migration 목록 조회", description = "Migration Task 서버 목록 조회")
    @ApiResponse(responseCode = "200")
    @GetMapping
    public ResponseEntity<?> getMigrations(@PathVariable long projectId,
                                           @RequestParam(value = "inventoryId", required = false) Long inventoryId,
                                           PageMigrationRequestDto pageMigrationRequestDto) {
        MigrationProcessListResponseDto migrationJobListResponseList = migrationService.getMigrationServerList(projectId, inventoryId, pageMigrationRequestDto);

        return ResponseEntity.ok(migrationJobListResponseList);
    }

    /**
     * <pre>
     * Migration Task Cancel
     * </pre>
     *
     * @param projectId   the project id
     * @param migrationId the migrationId
     *
     * @return response entity
     */
    @Operation(summary = "Migration Task 취소", description = "Migration Task 취소")
    @ApiResponse(responseCode = "200")
    @PatchMapping(value = "/{migrationId}")
    public ResponseEntity<?> cancelMigration(@PathVariable long projectId, @PathVariable long migrationId) {
        migrationService.cancelMigrationTask(projectId, migrationId);
        return ResponseEntity.ok().build();
    }

    /**
     * <pre>
     * Migration Task Delete
     * </pre>
     *
     * @param projectId the project id
     *
     * @return response entity
     */
    @Operation(summary = "Migration Task 삭제", description = "Migration Task 서버 목록 조회")
    @ApiResponse(responseCode = "200")
    @DeleteMapping(value = "/{migrationId}")
    public ResponseEntity<?> deleteMigration(@PathVariable long projectId, @PathVariable long migrationId) {
        migrationService.removeMigrationTask(projectId, migrationId);
        return ResponseEntity.ok().build();
    }

    /**
     * <pre>
     * Migration Task Detail
     * </pre>
     *
     * @param projectId the project id
     *
     * @return response entity
     */
    @Operation(summary = "Migration Task 상세", description = "Migration Task 서버 상세 조회")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/{migrationId}")
    public ResponseEntity<?> getMigration(@PathVariable long projectId, @PathVariable long migrationId) {
        MigrationJobDetailResponseDto migrationJobDetailResponseDto = migrationService.getMigrationTask(projectId, migrationId);

        return ResponseEntity.ok(migrationJobDetailResponseDto);
    }

    @Operation(summary = "Migration 목록 CSV 다운로드", description = "Migration 목록 CSV로 다운로드 한다.")
    @GetMapping("/csv-download")
    public ResponseEntity<?> assessmentsCsvDownload(@PathVariable Long projectId, PageMigrationRequestDto pageMigrationRequestDto) {
        ByteArrayInputStream in = migrationService.getMigrationCsvDownload(projectId, pageMigrationRequestDto);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + MIGRATION_TABLE + "_" + format.format(new Date()) + ".csv");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType("text/csv"));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }

}
//end of MigrationController.java