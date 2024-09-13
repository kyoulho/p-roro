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
 * Dong-Heon Han    Mar 15, 2022		First Draft.
 */

package io.playce.roro.api.domain.topology.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.api.domain.topology.component.ExternalConnectionWork;
import io.playce.roro.api.domain.topology.service.TopologyService;
import io.playce.roro.common.dto.topology.TopologyNodeResponse;
import io.playce.roro.common.dto.topology.TopologyPortmapResponse;
import io.playce.roro.common.dto.topology.TopologyServerResponse;
import io.playce.roro.common.dto.topology.TrafficResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@RestController
@RequestMapping("/api/projects/{projectId}/topology/{type}/{typeId}")
@RequiredArgsConstructor
public class TopologyController {
    private final TopologyService service;
    private final ExternalConnectionWork externalConnectionWork;

    @Operation(summary = "Topology 서버목록 조회", description = "Topology 서버목록을 조회한다.")
    @GetMapping("servers")
    @ResponseStatus(HttpStatus.OK)
    public List<TopologyServerResponse> getList(@PathVariable Long projectId, @PathVariable String type, @PathVariable Long typeId) {
        return service.getServerList(projectId, type, typeId);
    }

    @Operation(summary = "Topology 포트맵 조회", description = "Topology 포트맵을 조회한다.")
    @GetMapping("portmap")
    @ResponseStatus(HttpStatus.OK)
    public List<TopologyPortmapResponse> getPortList(@PathVariable Long projectId, @PathVariable String type, @PathVariable Long typeId) {
        return service.getPortList(projectId, type, typeId);
    }

    @Operation(summary = "Topology 노드 조회", description = "Topology 노드를 조회한다.")
    @GetMapping("nodes")
    @ResponseStatus(HttpStatus.OK)
    public TopologyNodeResponse getNodes(@PathVariable Long projectId, @PathVariable String type, @PathVariable Long typeId) {
        if(type.equals("APP")) {
            return service.getAppTopology(projectId, type, typeId);
        }
        return service.getNodeList(projectId, type, typeId);
    }

    @Operation(summary = "Topology Traffic 조회", description = "Topology Traffic을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/traffic")
    public ResponseEntity<?> getTraffic(@PathVariable Long projectId, @PathVariable String type, @PathVariable Long typeId) {
        TrafficResponse trafficResponse = service.getTraffic(projectId, type, typeId);

        return new ResponseEntity<>(trafficResponse, HttpStatus.OK);
    }

    @Operation(summary = "Topology 저장된 Node포지션 정보조회", description = "Topology 저장된 Node포지션 정보를 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("node-position")
    public JsonNode getNodePosition(@PathVariable Long projectId, @PathVariable String type, @PathVariable Long typeId) {
        return service.getNodePosition(projectId, type, typeId);
    }

    @Operation(summary = "Topology Node포지션 정보저장", description = "Topology Node포지션 정보를 저장한다.")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("node-position")
    public void saveNodePosition(@PathVariable Long projectId, @PathVariable String type, @PathVariable Long typeId, @RequestBody JsonNode content) {
        service.saveNodePosition(projectId, type, typeId, content);
    }

    @Operation(summary = "Application Topology External IP Node 목록조회", description = "Application Topology External IP Node 목록을 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("excluded-external-connection")
    public List<String> getExcludedExternalConnections(@PathVariable Long projectId, @PathVariable String type, @PathVariable Long typeId) {
        return externalConnectionWork.getExcludedExternalConnections(projectId, type, typeId);
    }

    @Operation(summary = "Application Topology External IP 제외", description = "Application Topology External IP를 제외한다.")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("exclude-external-connection")
    public void excludeExcludedExternalConnections(@PathVariable Long projectId, @PathVariable String type, @PathVariable Long typeId, @RequestBody List<String> content) {
        externalConnectionWork.excludeExcludedExternalConnections(projectId, content);
    }

    @Operation(summary = "Application Topology External IP 복원", description = "Application Topology External IP를 복원한다.")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("restore-external-connection")
    public void restoreExcludedExternalConnections(@PathVariable Long projectId, @PathVariable String type, @PathVariable Long typeId, @RequestBody List<String> content) {
        externalConnectionWork.restoreExcludedExternalConnections(projectId, content);
    }
}