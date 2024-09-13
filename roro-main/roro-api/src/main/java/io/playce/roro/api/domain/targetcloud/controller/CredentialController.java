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
 * SangCheon Park   Feb 09, 2022		    First Draft.
 */
package io.playce.roro.api.domain.targetcloud.controller;

import io.playce.roro.api.domain.targetcloud.service.CredentialService;
import io.playce.roro.common.dto.targetcloud.CredentialDto.CredentialRequest;
import io.playce.roro.common.dto.targetcloud.CredentialDto.CredentialResponse;
import io.playce.roro.common.dto.targetcloud.CredentialDto.CredentialSimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/projects/{projectId}/target-cloud", produces = APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
public class CredentialController {

    private final CredentialService credentialService;

    /**
     * <pre>
     * 등록된 Credential 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialTypeCode
     *
     * @return
     */
    @Operation(summary = "Credential 목록 조회", description = "등록된 Credential 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CredentialResponse.class))))
    @GetMapping(value = "/credentials")
    public ResponseEntity<?> getCredentials(@PathVariable Long projectId,
                                            @RequestParam(value = "credentialTypeCode") String credentialTypeCode) {
        List<CredentialResponse> credentialList = credentialService.getCredentials(projectId, credentialTypeCode);
        return ResponseEntity.ok(credentialList);
    }

    /**
     * <pre>
     * Credential 상세 정보를 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     *
     * @return
     */
    @Operation(summary = "Credential 상세 조회", description = "Credential 상세 정보를 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CredentialResponse.class)))
    @GetMapping(value = "/credentials/{credentialId}")
    public ResponseEntity<?> getCredential(@PathVariable Long projectId,
                                           @PathVariable Long credentialId) {
        CredentialResponse credential = credentialService.getCredential(projectId, credentialId);
        return ResponseEntity.ok(credential);
    }

    /**
     * <pre>
     * 신규 Credential을 등록한다.
     * </pre>
     *
     * @param projectId
     * @param credentialRequest
     * @param keyFile
     *
     * @return
     */
    @Operation(summary = "Credential 등록", description = "신규 Credential 정보를 등록한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CredentialSimpleResponse.class)))
    @PostMapping(value = "/credentials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createCredential(@PathVariable Long projectId,
                                                   @RequestPart("credential") CredentialRequest credentialRequest,
                                                   @RequestPart(value = "keyFile", required = false) MultipartFile keyFile) {
        CredentialSimpleResponse response = credentialService.createCredential(projectId, credentialRequest, keyFile);
        return ResponseEntity.ok(response);
    }

    /**
     * <pre>
     * Credential 정보를 수정한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param credentialRequest
     * @param keyFile
     *
     * @return
     */
    @Operation(summary = "Credential 수정", description = "Credential 정보를 수정한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CredentialSimpleResponse.class)))
    @PostMapping(value = "/credentials/{credentialId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> modifyCredential(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                   @RequestPart("credential") CredentialRequest credentialRequest,
                                                   @RequestPart(value = "keyFile", required = false) MultipartFile keyFile) {
        CredentialSimpleResponse response = credentialService.modifyCredential(projectId, credentialId, credentialRequest, keyFile);
        return ResponseEntity.ok(response);
    }

    /**
     * <pre>
     * Credential 상세 정보를 삭제한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     *
     * @return
     */
    @Operation(summary = "Credential 삭제", description = "Credential 정보를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(value = "/credentials/{credentialId}")
    public ResponseEntity<?> deleteCredential(@PathVariable Long projectId,
                                              @PathVariable Long credentialId) {
        credentialService.deleteCredential(projectId, credentialId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
//end of CredentialController.java