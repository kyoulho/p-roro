package io.playce.roro.api.domain.k8s.controller;

import io.playce.roro.api.domain.k8s.service.ClusterService;
import io.playce.roro.common.dto.k8s.ClusterRequest;
import io.playce.roro.common.dto.k8s.ClusterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RequestMapping(value = "api/projects/{projectId}/k8s-clusters", produces = APPLICATION_JSON_VALUE)
public class ClusterController {

    private final ClusterService clusterService;

    @ApiResponse(responseCode = "200")
    @Operation(summary = "클러스터 등록", description = "클러스터를 등록 한다.")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void createCluster(@PathVariable(name = "projectId") Long projectId, @RequestBody ClusterRequest clusterRequest) {
        clusterService.createCluster(projectId, clusterRequest);
    }

    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClusterResponse.class))))
    @Operation(summary = "클러스터 목록조회", description = "클러스터 목록을 조회 한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<ClusterResponse> getClusters(@PathVariable(name = "projectId") Long projectId) {
        return clusterService.getClusterList(projectId);
    }

    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClusterResponse.class))))
    @Operation(summary = "클러스터 조회", description = "클러스터를 조회 한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{k8sClusterId}")
    public ClusterResponse getCluster(@PathVariable(name = "projectId") Long projectId, @PathVariable(name = "k8sClusterId") Long k8sClusterId) {
        return clusterService.getCluster(projectId, k8sClusterId);
    }

    @ApiResponse(responseCode = "200")
    @Operation(summary = "클러스터 삭제", description = "클러스터를 삭제 한다.")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/{k8sClusterId}")
    public void deleteCluster(@PathVariable(name = "projectId") Long projectId, @PathVariable(name = "k8sClusterId") Long k8sClusterId) {
        clusterService.deleteCluster(projectId, k8sClusterId);
    }

    @ApiResponse(responseCode = "200")
    @Operation(summary = "클러스터 정보 수정", description = "클러스터 정보를 수정 한다.")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{k8sClusterId}", consumes = APPLICATION_JSON_VALUE)
    public void modifyCluster(@PathVariable(name = "projectId") Long projectId, @PathVariable(name = "k8sClusterId") Long k8sClusterId, @RequestBody ClusterRequest clusterRequest) {
        clusterService.modifyCluster(projectId, k8sClusterId, clusterRequest);
    }
}
