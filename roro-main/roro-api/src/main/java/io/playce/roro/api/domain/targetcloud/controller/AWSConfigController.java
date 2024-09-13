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

import io.playce.roro.api.domain.targetcloud.service.AWSConfigService;
import io.playce.roro.mig.aws.model.AWSConfigDto;
import io.playce.roro.mig.aws.model.InstanceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping(value = "/api/projects/{projectId}/target-cloud/credentials/{credentialId}/aws", produces = APPLICATION_JSON_VALUE)
public class AWSConfigController {

    private final AWSConfigService awsConfigService;

    /**
     * <pre>
     * Gets regions.
     * </pre>
     *
     * @return the regions
     */
    @Operation(summary = "AWS Region 목록 조회.", description = "AWS Region 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AWSConfigDto.Region.class))))
    @GetMapping(path = "/regions")
    public ResponseEntity<?> getRegionList(@PathVariable Long projectId, @PathVariable Long credentialId) {
        awsConfigService.getRegionList(projectId, credentialId);
        return ResponseEntity.ok(AWSConfigDto.Regions.getRegionList());
    }

    /**
     * @param credentialId
     * @param region
     * @param id
     * @param name
     * @param visibility
     *
     * @return
     */
    @Operation(summary = "AWS AMI 목록 조회", description = "AWS AMI 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AWSConfigDto.ImageResponse.class))))
    @GetMapping(path = "/ami")
    public ResponseEntity<?> getImageList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                          @RequestParam(value = "region") String region,
                                          @Parameter(name = "id", description = "AMI search filter by id") @RequestParam(value = "id", required = false) String id,
                                          @Parameter(name = "name", description = "AMI search filter by name") @RequestParam(value = "name", required = false) String name,
                                          @Parameter(name = "visibility", description = "Visibility Type (self / private / public) - Default is self") @RequestParam(value = "visibility", required = false) String visibility) {
        return ResponseEntity.ok(awsConfigService.getImageList(projectId, credentialId, region, id, name, visibility));
    }

    /**
     * Gets Keypair Names.
     *
     * @return the instance types
     */
    @Operation(summary = "AWS Keypair 목록 조회", description = "AWS Keypair 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    @GetMapping(path = "/key-pairs")
    public ResponseEntity<?> getKeyPairs(@PathVariable Long projectId, @PathVariable Long credentialId,
                                         @RequestParam(value = "region") String region) {
        return ResponseEntity.ok(awsConfigService.getKeyPairs(projectId, credentialId, region));
    }

    /**
     * <pre>
     * 해당 지역의 AvailablityZone 목록을 조회한다.
     * </pre>
     *
     * @param credentialId the credential id
     * @param region       the region
     *
     * @return availablity zones
     */
    @Operation(summary = "AWS Region의 availability zone 목록 조회.", description = "AWS Region의 availability zone 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    @GetMapping(path = "/zone")
    public ResponseEntity<?> getAvailabilityZoneList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                     @RequestParam(value = "region") String region) {
        return ResponseEntity.ok(awsConfigService.getAvailabilityZoneList(projectId, credentialId, region));
    }

    /**
     * <pre>
     * AWS Region의 Instance Type 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param region
     * @param subnetId
     *
     * @return
     */
    @Operation(summary = "AWS Instance Type 목록 조회", description = "AWS Region의 Instance Type 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = InstanceType.class))))
    @GetMapping(path = "/instance-types")
    public ResponseEntity<?> getInstanceTypes(@PathVariable Long projectId, @PathVariable Long credentialId,
                                              @RequestParam(value = "region") String region,
                                              @RequestParam(value = "subnetId", required = false) String subnetId) {
        return ResponseEntity.ok(awsConfigService.getInstanceTypes(projectId, credentialId, region, subnetId));
    }

    /**
     * <pre>
     * 주어진 region에 등록된 VPC 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param region
     * @param search
     *
     * @return
     */
    @Operation(summary = "AWS Region의 vpc 목록 조회.", description = "AWS Region의 vpc 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AWSConfigDto.VpcResponse.class))))
    @GetMapping(path = "/vpc")
    public ResponseEntity<?> getVpcList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                        @RequestParam(value = "region") String region,
                                        @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(awsConfigService.getVpcList(projectId, credentialId, region, search));
    }

    /**
     * <pre>
     * AWS Region의 vpc 생성
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param vpcRequest
     *
     * @return
     */
    @Operation(summary = "AWS Region의 vpc 생성", description = "AWS Region의 vpc를 생성한다.")
    @ApiResponse(responseCode = "201")
    @PostMapping(path = "/vpc", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createVpc(@PathVariable Long projectId, @PathVariable Long credentialId,
                                       @RequestBody AWSConfigDto.VpcCreateRequest vpcRequest) {
        awsConfigService.createVpc(projectId, credentialId, vpcRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * <pre>
     * AWS Region의 vpc 수정
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param vpcId
     * @param vpcRequest
     *
     * @return
     */
    @Operation(summary = "AWS Region의 vpc 수정", description = "AWS Region의 vpc를 수정한다.")
    @ApiResponse(responseCode = "204")
    @PutMapping(path = "/vpc/{vpcId}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateVpc(@PathVariable Long projectId, @PathVariable Long credentialId,
                                       @PathVariable String vpcId, @RequestBody AWSConfigDto.VpcUpdateRequest vpcRequest) {
        awsConfigService.updateVpc(projectId, credentialId, vpcId, vpcRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * AWS Region의 vpc 삭제
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param vpcId
     * @param region
     *
     * @return
     */
    @Operation(summary = "AWS Region의 vpc 삭제", description = "AWS Region의 vpc를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(path = "/vpc/{vpcId}")
    public ResponseEntity<?> deleteVpc(@PathVariable Long projectId, @PathVariable Long credentialId,
                                       @PathVariable String vpcId,
                                       @RequestParam(value = "region") String region) {
        awsConfigService.deleteVpc(projectId, credentialId, vpcId, region);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * 주어진 region에 등록된 Subnet 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param region
     * @param search
     *
     * @return
     */
    @Operation(summary = "AWS Region의 subnet 목록 조회", description = "AWS Region의 subnet 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AWSConfigDto.SubnetResponse.class))))
    @GetMapping(path = "/subnet")
    public ResponseEntity<?> getSubnetList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                           @RequestParam(value = "region") String region,
                                           @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(awsConfigService.getSubnetList(projectId, credentialId, region, search));
    }

    /**
     * <pre>
     * AWS Region의 subnet을 생성한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param subnetRequest
     *
     * @return
     */
    @Operation(summary = "AWS Region에 subnet 생성", description = "AWS Region의 subnet을 생성한다.")
    @ApiResponse(responseCode = "201")
    @PostMapping(path = "/subnet", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createSubnet(@PathVariable Long projectId, @PathVariable Long credentialId,
                                          @RequestBody AWSConfigDto.SubnetCreateRequest subnetRequest) {
        awsConfigService.createSubnet(projectId, credentialId, subnetRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * <pre>
     * AWS Region의 subnet을 수정한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param subnetId
     * @param subnetRequest
     *
     * @return
     */
    @Operation(summary = "AWS Region의 subnet 수정", description = "AWS Region의 subnet을 수정한다.")
    @ApiResponse(responseCode = "204")
    @PutMapping(path = "/subnet/{subnetId}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateSubnet(@PathVariable Long projectId, @PathVariable Long credentialId,
                                          @PathVariable String subnetId, @RequestBody AWSConfigDto.SubnetUpdateRequest subnetRequest) {
        awsConfigService.updateSubnet(projectId, credentialId, subnetId, subnetRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * AWS Region의 subnet을 삭제한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param subnetId
     * @param region
     *
     * @return
     */
    @Operation(summary = "AWS Region의 subnet 삭제", description = "AWS Region의 subnet을 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(path = "/subnet/{subnetId}")
    public ResponseEntity<?> deleteSubnet(@PathVariable Long projectId, @PathVariable Long credentialId,
                                          @PathVariable String subnetId,
                                          @RequestParam(value = "region") String region) {
        awsConfigService.deleteSubnet(projectId, credentialId, subnetId, region);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * AWS Region의 security group 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param region
     * @param search
     *
     * @return
     */
    @Operation(summary = "AWS Region의 security group 목록 조회.", description = "AWS Region의 security group 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AWSConfigDto.SecurityGroupResponse.class))))
    @GetMapping(path = "/sg")
    public ResponseEntity<?> getSecurityGroupList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                  @RequestParam(value = "region") String region,
                                                  @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(awsConfigService.getSecurityGroupList(projectId, credentialId, region, search));
    }

    /**
     * <pre>
     * AWS Region에 security group을 생성한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param securityGroupCreateRequest
     *
     * @return
     */
    @Operation(summary = "AWS Region에 security group 생성", description = "AWS Region에 security group을 생성한다.")
    @ApiResponse(responseCode = "201")
    @PostMapping(path = "/sg", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createSecurityGroup(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                 @RequestBody AWSConfigDto.SecurityGroupCreateRequest securityGroupCreateRequest) {
        awsConfigService.createSecurityGroup(projectId, credentialId, securityGroupCreateRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * <pre>
     * AWS Region의 security group name을 수정한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param groupId
     * @param securityGroupUpdateRequest
     *
     * @return
     */
    @Operation(summary = "AWS Region의 security group name 수정", description = "Name cannot be edited after creation. (Name 태그만 수정 가능)")
    @ApiResponse(responseCode = "204")
    @PutMapping(path = "/sg/{groupId}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateSecurityGroup(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                 @PathVariable String groupId, @RequestBody AWSConfigDto.SecurityGroupUpdateRequest securityGroupUpdateRequest) {
        awsConfigService.updateSecurityGroup(projectId, credentialId, groupId, securityGroupUpdateRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * AWS Region의 security group을 삭제한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param groupId
     * @param region
     *
     * @return
     */
    @Operation(summary = "AWS Region의 security group 삭제", description = "AWS Region의 security group을 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(path = "/sg/{groupId}")
    public ResponseEntity<?> deleteSecurityGroup(@PathVariable Long projectId, @PathVariable Long credentialId,
                                                 @PathVariable String groupId, @RequestParam(value = "region") String region) {
        awsConfigService.deleteSecurityGroup(projectId, credentialId, groupId, region);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * <pre>
     * AWS Security group의 rule 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param groupId
     * @param region
     *
     * @return
     */
    @Operation(summary = "AWS Security group의 rule 목록 조회", description = "AWS Security group의 rule 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AWSConfigDto.Permission.class))))
    @GetMapping(path = "/sg/{groupId}/rule")
    public ResponseEntity<?> getPermissionList(@PathVariable Long projectId, @PathVariable Long credentialId,
                                               @PathVariable(value = "groupId") String groupId, @RequestParam(value = "region") String region) {
        return ResponseEntity.ok(awsConfigService.getPermissionList(projectId, credentialId, groupId, region));
    }

    /**
     * <pre>
     * AWS Security group의 rule을 설정한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param groupId
     * @param permissionRequest
     *
     * @return
     */
    @Operation(summary = "AWS Security group의 rule 설정", description = "AWS Security group의 rule을 설정한다.")
    @ApiResponse(responseCode = "201")
    @PostMapping(path = "/sg/{groupId}/rule", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPermissions(@PathVariable Long projectId, @PathVariable Long credentialId,
                                               @PathVariable(value = "groupId") String groupId, @RequestBody AWSConfigDto.PermissionRequest permissionRequest) {
        awsConfigService.createPermissions(projectId, credentialId, groupId, permissionRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
//end of AWSConfigController.java