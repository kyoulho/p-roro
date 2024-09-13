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
package io.playce.roro.api.domain.network.controller;

import io.playce.roro.api.domain.network.service.NetworkFiltersService;
import io.playce.roro.common.dto.network.NetworkFilterCreateRequest;
import io.playce.roro.common.dto.network.NetworkFilterResponse;
import io.playce.roro.common.dto.network.NetworkFilterSimpleResponse;
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

import javax.validation.Valid;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/api/projects/{projectId}/network-filters", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class NetworkFilterController {

    private final NetworkFiltersService service;

    /**
     * Network filter 목록을 조회한다.
     *
     * @param projectId
     *
     * @return
     */
    @Operation(summary = "Network filter 목록", description = "Network filter 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = NetworkFilterResponse.class))))
    @GetMapping
    public ResponseEntity<?> getNetworkFilters(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.getNetworkFilters(projectId));
    }

    /**
     * Network filter 정보을 조회한다.
     *
     * @param projectId
     * @param networkFilterId
     *
     * @return
     */
    @Operation(summary = "Network filter 정보", description = "Network filter 정보을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = NetworkFilterResponse.class)))
    @GetMapping("/{networkFilterId}")
    public ResponseEntity<?> getNetworkFilter(@PathVariable Long projectId, @PathVariable Long networkFilterId) {
        return ResponseEntity.ok(service.getNetworkFilter(projectId, networkFilterId));
    }

    /**
     * Network filter를 등록한다.
     *
     * @param projectId
     * @param request
     *
     * @return
     */
    @Operation(summary = "Network filter 등록", description = "Network filter를 등록한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = NetworkFilterSimpleResponse.class)))
    @PostMapping
    public ResponseEntity<?> createNetworkFilter(@PathVariable Long projectId, @RequestBody @Valid NetworkFilterCreateRequest request) {
        return ResponseEntity.ok(service.createNetworkFilter(projectId, request));
    }

    /**
     * Network filter를 수정한다.
     *
     * @param projectId
     * @param networkFilterId
     * @param request
     *
     * @return
     */
    @Operation(summary = "Network filter 수정", description = "Network filter를 수정한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = NetworkFilterSimpleResponse.class)))
    @PutMapping("/{networkFilterId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> modifyNetworkFilter(@PathVariable Long projectId, @PathVariable Long networkFilterId,
                                                 @Valid @RequestBody NetworkFilterCreateRequest request) {
        return ResponseEntity.ok(service.modifyNetworkFilter(projectId, networkFilterId, request));
    }

    /**
     * Network filter를 삭제한다.
     *
     * @param projectId
     * @param networkFilterId
     */
    @Operation(summary = "Network filter 삭제", description = "Network filter를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/{networkFilterId}")
    public void deleteNetworkFilter(@PathVariable Long projectId, @PathVariable Long networkFilterId) {
        service.deleteNetworkFilter(projectId, networkFilterId);
    }
}
//end of NetworkFilterController.java
