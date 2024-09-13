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
 * SangCheon Park   Feb 10, 2022		    First Draft.
 */
package io.playce.roro.api.domain.targetcloud.controller;

import io.playce.roro.api.domain.targetcloud.service.GCPConfigService;
import io.playce.roro.mig.gcp.model.GCPConfigDto;
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/api/projects/{projectId}/target-cloud/credentials/{credentialId}/gcp", produces = APPLICATION_JSON_VALUE)
public class GCPConfigController {

    private final GCPConfigService gcpConfigService;

    /**
     * <pre>
     * GCP ProjectId 목록 조회
     * </pre>
     *
     * @param projectId
     * @param credentialId
     *
     * @return
     */
    @Operation(summary = "GCP ProjectId 목록 조회.", description = "GCP ProjectId 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GCPConfigDto.Projects.class)))
    @GetMapping(path = "/projects")
    public ResponseEntity<?> getProjectList(@PathVariable Long projectId, @PathVariable Long credentialId) {
        return ResponseEntity.ok(gcpConfigService.getProjectList(projectId, credentialId));
    }

    /**
     * <pre>
     * Machine Image 목록을 조회한다.
     * </pre>
     *
     * @param credentialId
     * @param projectId
     *
     * @return
     */
    @Operation(summary = "Machine Image 목록 조회", description = "지정된 프로젝트에 포함 된 머신 이미지 목록을 검색합니다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GCPConfigDto.MachineImageResponse.class))))
    @GetMapping(value = "/machine-images")
    public ResponseEntity<?> getMachineImageList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                 @RequestParam(value = "gcpProjectId") String gcpProjectId,
                                                 @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(gcpConfigService.getMachineImageList(projectId, credentialId, gcpProjectId, search));
    }

    /**
     * <pre>
     * GCP Region 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     *
     * @return
     */
    @Operation(summary = "GCP Region 목록 조회.", description = "GCP Region 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(path = "/regions")
    public ResponseEntity<?> getRegionList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                           @RequestParam(value = "gcpProjectId") String gcpProjectId) {
        return ResponseEntity.ok(gcpConfigService.getRegionList(projectId, credentialId, gcpProjectId));
    }

    /**
     * <pre>
     * GCP Region의 availability zone 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param region
     *
     * @return
     */
    @Operation(summary = "GCP Region의 availability zone 목록 조회.", description = "GCP Region의 availability zone 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(path = "/zone")
    public ResponseEntity<?> getAvailableZoneList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                  @RequestParam(value = "gcpProjectId") String gcpProjectId,
                                                  @RequestParam(value = "region", required = false) String region) {
        return ResponseEntity.ok(gcpConfigService.getZoneList(projectId, credentialId, gcpProjectId, region));
    }

    /**
     * <pre>
     * Zone 에서 이용가능한 Machine 유형을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param zone
     *
     * @return
     */
    @Operation(summary = "선택한 Zone 에서 이용가능한 Machine 유형 조회", description = "Zone 에서 이용가능한 Machine 유형 조회")
    @ApiResponse(responseCode = "200")
    @GetMapping(path = "/machine-types")
    public ResponseEntity<?> getAvailableMachineType(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                     @RequestParam(value = "gcpProjectId") String gcpProjectId,
                                                     @RequestParam(value = "zone") String zone) {
        return ResponseEntity.ok(gcpConfigService.getMachineTypeList(projectId, credentialId, gcpProjectId, zone));
    }

    /**
     * <pre>
     * Network 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param search
     *
     * @return
     */
    @Operation(summary = "Network 목록 조회", description = "Network 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(path = "/network")
    public ResponseEntity<?> getNetworkList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                            @RequestParam(value = "gcpProjectId") String gcpProjectId,
                                            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(gcpConfigService.getNetworkList(projectId, credentialId, gcpProjectId, search));
    }

    /**
     * <pre>
     * 새로운 Network를 추가한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param networkRequest
     *
     * @return
     */
    @Operation(summary = "Network 생성", description = "Network 생성한다.")
    @ApiResponse(responseCode = "201")
    @PostMapping(path = "/network")
    public ResponseEntity<?> createNetwork(@PathVariable Long projectId, @PathVariable Long credentialId,
                                           @RequestBody GCPConfigDto.NetworkCreateRequest networkRequest) {
        gcpConfigService.createNetwork(projectId, credentialId, networkRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * <pre>
     * 선택한 Network를 수정한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param networkId
     * @param networkRequest
     *
     * @return
     */
    @Operation(summary = "Network 수정", description = "Network를 수정한다.")
    @ApiResponse(responseCode = "204")
    @PutMapping(path = "/network/{networkId}")
    public ResponseEntity<?> updateNetwork(@PathVariable Long projectId, @PathVariable Long credentialId, @PathVariable String networkId,
                                           @RequestBody GCPConfigDto.NetworkUpdateRequest networkRequest) {
        gcpConfigService.updateNetwork(projectId, credentialId, networkId, networkRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * Network를 삭제한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param networkId
     * @param gcpProjectId
     *
     * @return
     */
    @Operation(summary = "Network 삭제", description = "Network를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(path = "/network/{networkId}")
    public ResponseEntity<?> deleteNetwork(@PathVariable Long projectId, @PathVariable Long credentialId, @PathVariable String networkId,
                                           @RequestParam(value = "gcpProjectId") String gcpProjectId) {
        gcpConfigService.deleteNetwork(projectId, credentialId, networkId, gcpProjectId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * 선택된 Network의 subnet 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param region
     * @param search
     *
     * @return
     */
    @Operation(summary = "Subnet 목록 조회", description = "선택한 Region의 subnet 목록 조회한다")
    @ApiResponse(responseCode = "200")
    @GetMapping(path = "/subnet")
    public ResponseEntity<?> getSubnetList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                           @RequestParam(value = "gcpProjectId") String gcpProjectId,
                                           @RequestParam(value = "region", required = false) String region,
                                           @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(gcpConfigService.getSubnetList(projectId, credentialId, gcpProjectId, region, search));
    }

    /**
     * <pre>
     * 선택된 Network에 Subnet을 추가한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param createRequest
     *
     * @return
     */
    @Operation(summary = "Subnet 생성", description = "Subnet을 생성한다")
    @ApiResponse(responseCode = "201")
    @PostMapping(path = "/subnet")
    public ResponseEntity<?> createSubnet(@PathVariable Long projectId, @PathVariable Long credentialId,
                                          @RequestBody GCPConfigDto.SubnetWorkCreateRequest createRequest) {
        gcpConfigService.createSubnet(projectId, credentialId, createRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * <pre>
     * 선택한 Subnet을 수정한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param subnetId
     * @param updateRequest
     *
     * @return
     */
    @Operation(summary = "Subnet 수정", description = "Subnet을 수정한다.")
    @ApiResponse(responseCode = "204")
    @PutMapping(path = "/subnet/{subnetId}")
    public ResponseEntity<?> updateSubnet(@PathVariable Long projectId, @PathVariable Long credentialId, @PathVariable String subnetId,
                                          @RequestBody GCPConfigDto.SubnetWorkUpdateRequest updateRequest) {
        gcpConfigService.updateSubnet(projectId, credentialId, subnetId, updateRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * 선택한 Subnet을 삭제한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param subnetId
     * @param gcpProjectId
     * @param region
     *
     * @return
     */
    @Operation(summary = "Subnet 삭제", description = "Subnet을 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(path = "/subnet/{subnetId}")
    public ResponseEntity<?> deleteSubnet(@PathVariable Long projectId, @PathVariable Long credentialId, @PathVariable String subnetId,
                                          @RequestParam(value = "gcpProjectId") String gcpProjectId,
                                          @RequestParam(value = "region") String region) {
        gcpConfigService.deleteSubnet(projectId, credentialId, subnetId, gcpProjectId, region);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * Firewall 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param search
     *
     * @return
     */
    @Operation(summary = "Firewall 목록 조회", description = "Firewall 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(path = "/firewall")
    public ResponseEntity<?> getFirewallRuleList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                 @RequestParam(value = "gcpProjectId") String gcpProjectId,
                                                 @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(gcpConfigService.getFirewallRuleList(projectId, credentialId, gcpProjectId, search));
    }

    /**
     * <pre>
     * 사용 가능한 Firewall Tag목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param networkId
     *
     * @return
     */
    @Operation(summary = "Firewall Tag 목록 조회", description = "Firewall Tag 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(path = "/firewall/tags")
    public ResponseEntity<?> getFirewallRuleTagList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                    @RequestParam(value = "gcpProjectId") String gcpProjectId,
                                                    @RequestParam(value = "networkId") String networkId) {
        return ResponseEntity.ok(gcpConfigService.getFirewallRuleTagList(projectId, credentialId, gcpProjectId, networkId));
    }

    /**
     * <pre>
     * Firewall 생성.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param createRequest
     *
     * @return
     */
    @Operation(summary = "Firewall 생성", description = "Firewall을 생성한다.")
    @ApiResponse(responseCode = "201")
    @PostMapping(path = "/firewall")
    public ResponseEntity<?> createFirewallRule(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                @RequestBody GCPConfigDto.FirewallCreateRequest createRequest) {
        gcpConfigService.createFirewallRule(projectId, credentialId, createRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * <pre>
     * Firewall 수정.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param firewallId
     * @param updateRequest
     *
     * @return
     */
    @Operation(summary = "Firewall 수정", description = "Firewall을 수정한다.")
    @ApiResponse(responseCode = "204")
    @PutMapping(path = "/firewall/{firewallId}")
    public ResponseEntity<?> modifyFirewallRule(@PathVariable Long projectId, @PathVariable Long credentialId, @PathVariable String firewallId,
                                                @RequestBody GCPConfigDto.FirewallUpdateRequest updateRequest) {
        gcpConfigService.updateFirewallRule(projectId, credentialId, firewallId, updateRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * Firewall 삭제한다.
     * </pre>
     *
     * @return response entity
     */
    @Operation(summary = "Firewall 삭제", description = "Firewall 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(path = "/firewall/{firewallId}")
    public ResponseEntity<?> deleteFirewallRule(@PathVariable Long projectId, @PathVariable Long credentialId, @PathVariable String firewallId,
                                                @RequestParam(value = "gcpProjectId") String gcpProjectId) {
        gcpConfigService.deleteFirewallRule(projectId, credentialId, firewallId, gcpProjectId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
//end of GCPConfigController.java