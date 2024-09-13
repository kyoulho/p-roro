/*
 * Copyright 2023 The playce-roro-v3 Project.
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
 * Jihyun Park      6월 15, 2023            First Draft.
 */
package io.playce.roro.api.domain.topology.controller;

import io.playce.roro.api.domain.topology.service.ExternalConnectionLabelService;
import io.playce.roro.common.dto.topology.ExternalConnectionLabelRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Jihyun Park
 * @version 1.0
 */
@RestController
@RequestMapping("/api/projects/{projectId}/topology")
@RequiredArgsConstructor
public class ExternalConnectionLabelController {

    private final ExternalConnectionLabelService externalConnectionLabelService;

    @Operation(summary = "Application 하드코딩된 IP 라벨 저장", description = "Application Hard-coded IP Address/Port 노드의 라벨을 저장한다.")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("external-connection-label")
    public ResponseEntity<?> saveExternalConnectionLabel(@PathVariable Long projectId, @Valid @RequestBody ExternalConnectionLabelRequest externalConnectionLabelRequest) {
        externalConnectionLabelRequest.setProjectId(projectId);
        externalConnectionLabelService.saveExternalConnectionLabel(externalConnectionLabelRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Application 하드코딩된 IP 라벨 삭제", description = "Application Hard-coded IP Address/Port 노드의 라벨을 삭제한다.")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("external-connection-label/{ip}")
    public ResponseEntity<?> deleteExternalConnectionLabel(@PathVariable Long projectId, @PathVariable String ip) {
        externalConnectionLabelService.deleteExternalConnectionLabel(projectId, ip);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
