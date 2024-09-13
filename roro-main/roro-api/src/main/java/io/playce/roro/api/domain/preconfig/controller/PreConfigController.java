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
 * SangCheon Park   Jan 21, 2022		    First Draft.
 */
package io.playce.roro.api.domain.preconfig.controller;

import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.preconfig.service.PreConfigService;
import io.playce.roro.common.dto.preconfig.PreConfigDto;
import io.playce.roro.common.dto.preconfig.PreConfigRequest;
import io.playce.roro.common.dto.preconfig.PreConfigResponse;
import io.playce.roro.common.dto.preconfig.PreConfigSimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@RestController
@RequestMapping(value = "/api/projects/{projectId}/inventory", produces = APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PreConfigController {

    private final PreConfigService preConfigService;

    /**
     * <pre>
     * 등록된 preConfig 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param serverId
     *
     * @return
     */
    @Operation(summary = "preConfig 목록 조회", description = "등록된 preConfig 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PreConfigResponse.class))))
    @GetMapping(value = "/servers/{serverId}/pre-configs")
    public ResponseEntity<?> getPreConfigs(@PathVariable Long projectId,
                                           @PathVariable Long serverId) {
        List<PreConfigResponse> preConfigList = preConfigService.getPreConfigs(projectId, serverId);
        return ResponseEntity.ok(preConfigList);
    }

    /**
     * <pre>
     * preConfig 상세정보를 조회한다.
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param preConfigId
     *
     * @return
     */
    @Operation(summary = "preConfig 상세 조회", description = "preConfig 상세정보를 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PreConfigResponse.class)))
    @GetMapping(value = "/servers/{serverId}/pre-configs/{preConfigId}")
    public ResponseEntity<?> getPreConfig(@PathVariable Long projectId,
                                          @PathVariable Long serverId,
                                          @PathVariable Long preConfigId) {
        PreConfigResponse preConfig = preConfigService.getPreConfig(projectId, serverId, preConfigId);
        return ResponseEntity.ok(preConfig);
    }

    /**
     * <pre>
     * 신규 preConfig를 등록한다.
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param preConfigRequest
     * @param keyFile
     *
     * @return
     */
    @Operation(summary = "preConfig 등록", description = "신규 preConfig 정보를 등록한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PreConfigSimpleResponse.class)))
    @PostMapping(value = "/servers/{serverId}/pre-configs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPreConfig(@PathVariable Long projectId,
                                             @PathVariable Long serverId,
                                             @RequestPart(value = "preConfig") PreConfigRequest preConfigRequest,
                                             @RequestPart(value = "keyFile", required = false) MultipartFile keyFile) {
        PreConfigSimpleResponse response = preConfigService.createPreConfig(projectId, serverId, preConfigRequest, keyFile);
        return ResponseEntity.ok(response);
    }

    /**
     * <pre>
     * preConfig를 수정한다.
     * </pre>
     *
     * @param serverId
     * @param preConfigId
     * @param preConfigRequest
     * @param keyFile
     *
     * @return
     */
    @Operation(summary = "preConfig 수정", description = "해당 preConfig 정보를 수정한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PreConfigSimpleResponse.class)))
    @PostMapping(path = "/servers/{serverId}/pre-configs/{preConfigId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> modifyPreConfig(@PathVariable Long projectId, @PathVariable Long serverId, @PathVariable Long preConfigId,
                                             @RequestPart(value = "preConfig") PreConfigRequest preConfigRequest,
                                             @RequestPart(value = "keyFile", required = false) MultipartFile keyFile) {
        PreConfigSimpleResponse response = preConfigService.modifyPreConfig(projectId, serverId, preConfigId, preConfigRequest, keyFile);
        return ResponseEntity.ok(response);
    }

    /**
     * <pre>
     * preConfig를 삭제한다.
     * </pre>
     *
     * @param serverId
     * @param preConfigId
     *
     * @return
     */
    @Operation(summary = "preConfig 삭제", description = "해당 preConfig 정보를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(path = "/servers/{serverId}/pre-configs/{preConfigId}")
    public ResponseEntity<?> deletePreConfig(@PathVariable Long projectId, @PathVariable long serverId, @PathVariable long preConfigId) {
        preConfigService.deletePreConfig(projectId, serverId, preConfigId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * Key File 다운로드
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param preConfigId
     * @param keyFileName
     *
     * @return
     *
     * @throws IOException
     */
    @Operation(summary = "Key File 다운로드")
    @ApiResponse(responseCode = "200")
    @GetMapping(path = "/servers/{serverId}/pre-configs/{preConfigId}/file-download")
    public ResponseEntity<?> getKeyFile(@PathVariable Long projectId, @PathVariable Long serverId, @PathVariable Long preConfigId,
                                        @RequestParam("keyFileName") String keyFileName) throws IOException {
        File keyFile = preConfigService.getKeyFile(projectId, serverId, preConfigId);

        String encodedFileName = FileUtil.getEncodeFileName(keyFileName);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(new Tika().detect(keyFile)));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(keyFile));

        return new ResponseEntity<>(resource, responseHeaders, HttpStatus.OK);
    }

    /**
     * <pre>
     * 서버 사용자 및 그룹 조회
     * </pre>
     *
     * @param projectId
     * @param serverId
     *
     * @return
     */
    @Operation(summary = "서버 사용자 및 그룹 조회")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PreConfigDto.UserGroups.class)))
    @GetMapping(path = "/servers/{serverId}/pre-configs/user-groups")
    public ResponseEntity<?> getUserGroups(@PathVariable Long projectId, @PathVariable Long serverId) {
        PreConfigDto.UserGroups userGroups = preConfigService.getUserGroups(projectId, serverId);
        return ResponseEntity.ok(userGroups);
    }

    /**
     * <pre>
     * Profile 조회
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param username
     *
     * @return
     */
    @Operation(summary = "Profile 조회")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PreConfigDto.Profile.class)))
    @GetMapping(path = "/servers/{serverId}/pre-configs/users/{username}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable Long projectId, @PathVariable Long serverId, @PathVariable String username) {
        PreConfigDto.Profile profile = preConfigService.getUserProfile(projectId, serverId, username);
        return ResponseEntity.ok(profile);
    }

    /**
     * <pre>
     * Crontab 조회
     * </pre>
     *
     * @param projectId
     * @param serverId
     *
     * @return
     */
    @Operation(summary = "Crontab 조회")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PreConfigDto.Crontab.class))))
    @GetMapping(path = "/servers/{serverId}/pre-configs/crontabs")
    public ResponseEntity<?> getCrontabList(@PathVariable Long projectId, @PathVariable Long serverId) {
        List<PreConfigDto.Crontab> crontabList = preConfigService.getCrontabList(projectId, serverId);
        return ResponseEntity.ok(crontabList);
    }

    /**
     * <pre>
     * Pre-Configuration을 하기 위해 path 하위의 디렉토리/파일/심볼릭링크 목록을 가져온다.
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param path
     *
     * @return
     */
    @Operation(summary = "Pre-Configuration을 하기 위해 path 하위의 디렉토리/파일/심볼릭링크 목록을 가져온다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PreConfigDto.File.class))))
    @GetMapping(path = "/servers/{serverId}/pre-configs/files")
    public ResponseEntity<?> getFileList(@PathVariable Long projectId, @PathVariable Long serverId, @RequestParam String path) {
        List<PreConfigDto.File> fileList = preConfigService.getFileList(projectId, serverId, path);
        return ResponseEntity.ok(fileList);
    }
}
//end of PreConfigController.java