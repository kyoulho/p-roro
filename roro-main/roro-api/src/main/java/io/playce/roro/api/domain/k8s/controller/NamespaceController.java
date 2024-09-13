/*
 * Copyright 2023 The playce-roro Project.
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
 * Dong-Heon Han    Jul 26, 2023		First Draft.
 */

package io.playce.roro.api.domain.k8s.controller;

import io.playce.roro.api.domain.k8s.service.NamespaceService;
import io.playce.roro.common.dto.k8s.NamespaceResponse;
import io.playce.roro.common.dto.k8s.NamespaceTopologyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/k8s-clusters/{k8sClusterId}/scan/{clusterScanId}")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class NamespaceController {
    private final NamespaceService namespaceService;

    @GetMapping("/namespaces")
    @Operation(summary = "네임스페이스 목록 조회", description = "네임스페이스 목록을 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    public List<NamespaceResponse> getNamespaces(@PathVariable Long projectId, @PathVariable Long k8sClusterId, @PathVariable Long clusterScanId) {
        return namespaceService.getNamespaces(projectId, k8sClusterId, clusterScanId);
    }

    @GetMapping("/namespaces/topology")
    @Operation(summary = "네임스페이스 토폴로지 조회", description = "네임스페이스 토폴로지를 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    public NamespaceTopologyResponse getNamespaceTopology(@PathVariable Long projectId, @PathVariable Long k8sClusterId, @PathVariable Long clusterScanId) {
        return namespaceService.getNamespaceTopology(projectId, k8sClusterId, clusterScanId);
    }
}