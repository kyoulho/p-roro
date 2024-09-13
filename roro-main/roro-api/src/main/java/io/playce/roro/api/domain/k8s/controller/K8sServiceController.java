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

import io.playce.roro.api.domain.k8s.service.K8sServiceService;
import io.playce.roro.common.dto.k8s.K8sServiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/k8s-clusters/{k8sClusterId}/scan/{clusterScanId}/services")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class K8sServiceController {
    private final K8sServiceService k8sServiceService;

    @GetMapping()
    @Operation(summary = "서비스 목록 조회", description = "서비스 목록을 조회 한다.")
    @ResponseStatus(HttpStatus.OK)
    public List<K8sServiceResponse> getNodes(@PathVariable Long projectId, @PathVariable Long k8sClusterId, @PathVariable Long clusterScanId) {
        return k8sServiceService.getServices(projectId, k8sClusterId, clusterScanId);
    }
}