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

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.common.aop.SubscriptionManager;
import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.inventory.server.ServerDetailResponse;
import io.playce.roro.common.dto.inventory.server.ServerRequest;
import io.playce.roro.common.dto.inventory.server.ServerResponse;
import io.playce.roro.common.dto.inventory.server.ServerSimpleResponse;
import io.playce.roro.common.dto.subscription.Subscription;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.playce.roro.api.common.CommonConstants.EXCEL_RESOURCE_SERVER_NAME;
import static io.playce.roro.api.common.CommonConstants.MEDIA_TYPE_EXCEL;
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
@RequestMapping(value = "/api/projects/{projectId}/inventory", produces = APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ServerController {

    private final ServerService serverService;
    private final ProjectService projectService;

    /**
     * <pre>
     * 등록된 서버 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param serviceId
     * @param includePreconfig
     *
     * @return
     */
    @Operation(summary = "서버 목록 조회", description = "등록된 서버 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServerResponse.class))))
    @GetMapping("/servers")
    public ResponseEntity<?> getServers(@PathVariable Long projectId,
                                        @RequestParam(value = "serviceId", required = false) Long serviceId,
                                        @RequestParam(value = "includePreconfig", required = false) Boolean includePreconfig) {
        List<ServerResponse> serverList = serverService.getServers(projectId, serviceId, includePreconfig);
        return ResponseEntity.ok(serverList);
    }

    /**
     * <pre>
     * 서버 상세정보를 조회한다.
     * </pre>
     *
     * @param projectId
     * @param serverId
     *
     * @return
     */
    @Operation(summary = "서버 상세 조회", description = "서버 상세 정보를 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ServerDetailResponse.class)))
    @GetMapping("/servers/{serverId}")
    public ResponseEntity<?> getServer(@PathVariable Long projectId,
                                       @PathVariable Long serverId) {
        ServerDetailResponse server = serverService.getServer(projectId, serverId);
        return ResponseEntity.ok(server);
    }

    /**
     * <pre>
     * 신규 서버를 등록한다.
     * </pre>
     *
     * @param projectId
     * @param serverRequest
     * @param keyFile
     *
     * @return
     */
    @Operation(summary = "서버 등록", description = "서버 인벤토리를 등록한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ServerSimpleResponse.class)))
    @PostMapping(value = "/servers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createServer(@PathVariable Long projectId,
                                          @RequestPart("server") ServerRequest serverRequest,
                                          @RequestPart(value = "keyFile", required = false) MultipartFile keyFile) {
        Subscription subscription = serverService.getSubscriptionWithUsedCount();
        if (SubscriptionManager.getSubscription().getCount() <= subscription.getUsedCount()) {
            throw new RoRoApiException(ErrorCode.SUBSCRIPTION_COUNT_EXCEEDED, SubscriptionManager.getSubscription().getCount().toString());
        }

        ServerSimpleResponse response = serverService.createServer(projectId, serverRequest, keyFile);
        return ResponseEntity.ok(response);
    }

    /**
     * <pre>
     * 서버 정보를 수정한다.
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param serverRequest
     * @param keyFile
     *
     * @return
     */
    @Operation(summary = "서버 수정", description = "서버 인벤토리를 수정한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ServerSimpleResponse.class)))
    @PostMapping(value = "/servers/{serverId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> modifyServer(@PathVariable Long projectId,
                                          @PathVariable Long serverId,
                                          @RequestPart("server") ServerRequest serverRequest,
                                          @RequestPart(value = "keyFile", required = false) MultipartFile keyFile) {
        ServerSimpleResponse response = serverService.modifyServer(projectId, serverId, serverRequest, keyFile);
        return ResponseEntity.ok(response);
    }

    /**
     * <pre>
     * 서버를 삭제한다.
     * </pre>
     *
     * @param projectId
     * @param serverId
     *
     * @return
     */
    @Operation(summary = "서버 삭제", description = "서버 인벤토리를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/servers/{serverId}")
    public ResponseEntity<?> deleteServer(@PathVariable Long projectId, @PathVariable Long serverId) {
        serverService.deleteServer(projectId, serverId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * 서버 목록 Excel을 다운로드한다.
     * </pre>
     *
     * @param projectId
     * @param serviceId
     *
     * @return
     */
    @Operation(summary = "서버 목록 Excel 다운로드", description = "서버 목록을 Excel로 다운로드한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/servers/excel")
    public ResponseEntity<InputStreamResource> excelDownload(@PathVariable Long projectId,
                                                             @RequestParam(value = "serviceId", required = false) Long serviceId) {
        ByteArrayInputStream in = serverService.getServerListExcel(projectId, serviceId);

        String filename = ExcelUtil.generateExcelFileName(EXCEL_RESOURCE_SERVER_NAME);
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }

    /**
     * <pre>
     * Assessment 결과를 수동으로 등록한다.
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param assessmentFile
     *
     * @return
     */
    @Operation(summary = "Assessment 수동 등록", description = "Assessment 결과를 수동으로 등록한다.")
    @PostMapping(value = "/servers/{serverId}/assessment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> manualAssessmentUpload(@PathVariable Long projectId,
                                                    @PathVariable Long serverId,
                                                    @RequestPart("assessmentFile") MultipartFile assessmentFile) {
        Long inventoryProcessId = serverService.createInventoryProcessByFile(projectId, serverId, assessmentFile);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("inventoryProcessId", inventoryProcessId);

        return ResponseEntity.ok(resultMap);
    }

    @Operation(summary = "Disk Usage 조회", description = "Disk Usage를 조회한다.")
    @PatchMapping("/servers/{serverInventoryId}/disk-usage")
    public ResponseEntity<?> diskUsage(@PathVariable Long projectId,
                                       @PathVariable Long serverInventoryId) {
        Map<String, Object> diskInfo = serverService.getServerDiskUsage(projectId, serverInventoryId);
        return ResponseEntity.ok(diskInfo);
    }

}
//end of ServerController.java
