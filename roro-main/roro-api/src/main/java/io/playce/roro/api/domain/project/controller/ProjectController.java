/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * SangCheon Park   Dec 08, 2021		    First Draft.
 */
package io.playce.roro.api.domain.project.controller;

import io.playce.roro.api.domain.common.service.ProductLifecycleRulesService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.project.ProjectRequest;
import io.playce.roro.common.dto.project.ProjectResponse;
import io.playce.roro.common.dto.project.ProjectSimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping(value = "/api/projects", produces = APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * <pre>
     * 프로젝트 목록 조회
     * </pre>
     *
     * @return
     */
    @Operation(summary = "프로젝트 목록 조회", description = "등록된 프로젝트 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectResponse.class))))
    @GetMapping
    public ResponseEntity<?> getProjectList() {
        List<ProjectResponse> projectResponseList = projectService.getProjectList();
        return ResponseEntity.ok(projectResponseList);
    }

    /**
     * <pre>
     * 프로젝트 상세 조회
     * </pre>
     *
     * @return
     */
    @Operation(summary = "프로젝트 상세 조회", description = "등록된 프로젝트의 상세 정보를 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ProjectResponse.class)))
    @GetMapping(value = "/{projectId}")
    public ResponseEntity<?> getProject(@PathVariable Long projectId) {
        ProjectResponse projectResponse = projectService.getProject(projectId);
        return ResponseEntity.ok(projectResponse);
    }

    /**
     * <pre>
     * 프로젝트 등록
     * </pre>
     *
     * @param projectRequest
     * @return
     */
    @Operation(summary = "프로젝트 등록", description = "신규 프로젝트를 등록한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ProjectSimpleResponse.class)))
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectRequest projectRequest) {
        ProjectSimpleResponse projectSimpleResponse = projectService.createProject(projectRequest);
        return ResponseEntity.ok(projectSimpleResponse);
    }

    /**
     * <pre>
     * 프로젝트 수정
     * </pre>
     *
     * @param projectId
     * @param projectRequest
     * @return
     */
    @Operation(summary = "프로젝트 수정", description = "해당 프로젝트 정보를 수정한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ProjectSimpleResponse.class)))
    @PatchMapping(value = "/{projectId}")
    public ResponseEntity<?> modifyProject(@PathVariable Long projectId, @RequestBody ProjectRequest projectRequest) {
        ProjectSimpleResponse projectSimpleResponse = projectService.modifyProject(projectId, projectRequest);
        return ResponseEntity.ok(projectSimpleResponse);
    }

    /**
     * <pre>
     * 프로젝트 삭제
     * </pre>
     *
     * @param projectId
     * @return
     */
    @Operation(summary = "프로젝트 삭제", description = "해당 프로젝트를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(value = "/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
//end of ProjectController.java