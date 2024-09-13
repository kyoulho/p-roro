package io.playce.roro.api.domain.inventory.controller;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.inventory.service.DiscoveredThirdPartyService;
import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.inventory.server.ServerDetailResponse;
import io.playce.roro.common.dto.inventory.thirdparty.DiscoveredThirdPartyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static io.playce.roro.api.common.CommonConstants.THIRD_PARTY_EXCEL_REPORT_NAME;

@RestController
@RequestMapping(value = "/api/projects/{projectId}")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DiscoveredThirdPartyController {

    private final ProjectService projectService;
    private final ServerService serverService;
    private final DiscoveredThirdPartyService discoveredThirdPartyService;

    @Operation(summary = "프로젝트 Overview 서드파티 솔루션 목록 조회", description = "발견된 서드파티 솔루션 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DiscoveredThirdPartyResponse.class))))
    @GetMapping("/third-parties")
    public ResponseEntity<?> getProjectThirdParties(@PathVariable Long projectId,
                                                    @RequestParam(name = "serviceIds", required = false) String serviceIds) {
        List<DiscoveredThirdPartyResponse> discoveredThirdPartyResponses = discoveredThirdPartyService.getProjectThirdParties(projectId, serviceIds);

        return ResponseEntity.ok(discoveredThirdPartyResponses);
    }

    @Operation(summary = "서버 Overview 서드파티 솔루션 목록 조회", description = "발견된 서드파티 솔루션 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DiscoveredThirdPartyResponse.class))))
    @GetMapping("/inventory/servers/{serverId}/third-parties")
    public ResponseEntity<?> getServerProjectThirdParties(@PathVariable Long projectId,
                                                          @PathVariable Long serverId) {
        List<DiscoveredThirdPartyResponse> discoveredThirdPartyResponses = discoveredThirdPartyService.getServerThirdParties(projectId, serverId);

        return ResponseEntity.ok(discoveredThirdPartyResponses);
    }

    @Operation(summary = "서드파티 솔루션 목록 Excel Export", description = "발견된 서드파티 솔루션 목록을 Excel로 export한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/third-parties/excel")
    public ResponseEntity<?> excelExport(@PathVariable Long projectId,
                                         @RequestParam(name = "serviceIds", required = false) String serviceIds,
                                         @RequestParam(name = "serverId", required = false) Long serverId) {
        if (StringUtils.isNotEmpty(serviceIds) && serverId != null) {
            throw new RoRoApiException(ErrorCode.THIRD_PARTY_INVALID_PARAM);
        }

        ByteArrayOutputStream out = discoveredThirdPartyService.excelExport(projectId, serviceIds, serverId);
        String fileName = null;
        if (serverId == null) {
            fileName = projectService.getProjectName(projectId);
        } else {
            ServerDetailResponse serverDetailResponse = serverService.getServer(projectId, serverId);

            if (serverDetailResponse == null) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND);
            }

            fileName = projectService.getProjectName(projectId) + "_" + serverDetailResponse.getServerInventoryName();
        }

        String filename = ExcelUtil.generateExcelFileName(fileName + "_" + THIRD_PARTY_EXCEL_REPORT_NAME);
        String encodedFileName = FileUtil.getEncodeFileName(filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(CommonConstants.MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())), responseHeaders, HttpStatus.OK);
    }
}
