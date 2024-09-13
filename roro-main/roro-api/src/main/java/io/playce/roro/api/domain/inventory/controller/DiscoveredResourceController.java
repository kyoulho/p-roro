/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Hoon Oh       2월 24, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.controller;

import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.inventory.service.DiscoveredResourceService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.discovered.*;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.List;

import static io.playce.roro.api.common.CommonConstants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@RestController
@RequestMapping(value = "/api/projects/{projectId}/discovered", produces = APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DiscoveredResourceController {

    private final DiscoveredResourceService discoveredResourceService;
    private final ProjectService projectService;

    @Operation(summary = "미등록 서버 목록 조회", description = "미등록된 서버 목록을 조회한다.")
    @GetMapping("/servers")
    public ResponseEntity<?> discoveredServers(@PathVariable Long projectId,
                                               @ModelAttribute PageDiscoveredRequestDto pageDiscoveredRequestDto) {
        DiscoveredServerListResponse discoveredServerList
                = discoveredResourceService.getDiscoveredServerList(projectId, pageDiscoveredRequestDto);
        return ResponseEntity.ok(discoveredServerList);
    }

    @Operation(summary = "미등록 서버 상세 조회", description = "미등록 서버 상세정보를 조회한다.")
    @GetMapping("/servers/{discoveredInstanceId}")
    public ResponseEntity<?> getDiscoveredServerDetail(@PathVariable Long projectId, @PathVariable Long discoveredInstanceId) {
        DiscoveredServerDetailResponse discoveredServerDetail = discoveredResourceService.getDiscoveredServerDetail(projectId, discoveredInstanceId);
        return ResponseEntity.ok(discoveredServerDetail);
    }

    @Operation(summary = "미등록 서버 export", description = "미등록 서버 export 한다.")
    @GetMapping("/servers/excel")
    public ResponseEntity<?> discoveredServerExcel(@PathVariable Long projectId) {
        ByteArrayInputStream in = discoveredResourceService.getDiscoveredServerExcel(projectId);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + DISCOVERED_SERVER + "_" + format.format(new Date()) + ".xlsx");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "미등록 서버 목록 CSV 다운로드", description = "미등록된 서버 목록 CSV로 다운로드 한다.")
    @GetMapping("/servers/csv-download")
    public ResponseEntity<?> discoveredSvrCsvDownload(@PathVariable Long projectId,
                                                      @ModelAttribute PageDiscoveredRequestDto pageDiscoveredRequestDto) {
        ByteArrayInputStream in = discoveredResourceService.getDiscoveredSvrCsvDownload(projectId, pageDiscoveredRequestDto);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + DISCOVERED_SERVER_TABLE + "_" + format.format(new Date()) + ".csv");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType("text/csv"));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "미등록 데이터베이스 목록 조회", description = "미등록된 데이터베이스 목록을 조회한다.")
    @GetMapping("/databases")
    public ResponseEntity<?> discoveredDatabases(@PathVariable Long projectId) throws InterruptedException {
        List<DiscoveredDatabaseListResponse> discoveredDatabaseList = discoveredResourceService.getDiscoveredDatabaseList(projectId);
        return ResponseEntity.ok(discoveredDatabaseList);
    }

    @Operation(summary = "미등록 데이터베이스 상세 조회", description = "미등록 데이터베이스 상세정보를 조회한다.")
    @GetMapping("/databases/{discoveredInstanceId}")
    public ResponseEntity<?> getDiscoveredDatabaseDetail(@PathVariable Long projectId, @PathVariable Long discoveredInstanceId) {
        DiscoveredDatabaseDetailResponse discoveredDatabaseDetail = discoveredResourceService.getDiscoveredDatabaseDetail(projectId, discoveredInstanceId);
        return ResponseEntity.ok(discoveredDatabaseDetail);
    }

    @Operation(summary = "미등록 데이터베이스 export", description = "미등록 데이터베이스 export 한다.")
    @GetMapping("/databases/excel")
    public ResponseEntity<?> discoveredDatabaseExcel(@PathVariable Long projectId) {
        ByteArrayInputStream in = discoveredResourceService.getDiscoveredDatabaseExcel(projectId);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + DISCOVERED_DATABASE + "_" + format.format(new Date()) + ".xlsx");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }
}
//end of DiscoveredResourceController.java