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
 * Dong-Heon Han    Jul 27, 2023		First Draft.
 */

package io.playce.roro.api.domain.k8s.controller;

import io.playce.roro.api.domain.k8s.service.PodService;
import io.playce.roro.common.dto.k8s.PodResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/k8s-clusters/{k8sClusterId}/scan/{clusterScanId}/pods")
@SecurityRequirement(name = "bearerAuth")
public class PodController {
    private final PodService podService;

    @GetMapping
    @Operation(summary = "파드 목록 조회", description = "파드 목록을 조회 한다.")
    @ResponseStatus(HttpStatus.OK)
    public List<PodResponse> getPods(@PathVariable Long projectId, @PathVariable Long k8sClusterId, @PathVariable Long clusterScanId) {
        return podService.getPods(projectId, k8sClusterId, clusterScanId);
    }
}