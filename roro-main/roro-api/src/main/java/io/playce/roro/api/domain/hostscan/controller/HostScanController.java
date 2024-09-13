package io.playce.roro.api.domain.hostscan.controller;

import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.hostscan.service.HostScanService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.hostscan.DiscoveredHostDto;
import io.playce.roro.common.dto.hostscan.HostScanHistoryDto;
import io.playce.roro.common.dto.hostscan.HostScanRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static io.playce.roro.api.common.CommonConstants.HOST_SCAN_EXCEL_TEMPLATE_NAME;
import static io.playce.roro.api.common.CommonConstants.MEDIA_TYPE_EXCEL;

/**
 * <pre>
 *
 * </pre>
 *
 * @author JinHyun Kyun
 * @version 3.0
 */
@RestController
@RequestMapping(value = "/api/projects/{projectId}/host-scan")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class HostScanController {
    private final HostScanService hostScanService;
    private final ProjectService projectService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "스캔 내역 목록 조회", description = "스캔 내역을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HostScanHistoryDto.class))))
    public List<HostScanHistoryDto> hostScanHistories(@PathVariable("projectId") Long projectId) {
        return hostScanService.getHostScanHistories(projectId);
    }

    @GetMapping("/{scanHistoryId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "스캔 내역 상세 조회", description = "스캔 결과 발견된 호스트 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DiscoveredHostDto.class))))
    public List<DiscoveredHostDto> discoveredHosts(@PathVariable("projectId") Long projectId, @PathVariable("scanHistoryId") Long scanHistoryId) {
        return hostScanService.getDiscoveredHosts(projectId, scanHistoryId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "스캔 요청", description = "입력 받은 CIDR 을 스캔한다.")
    @ApiResponse(responseCode = "200")
    public void hostScan(@PathVariable("projectId") Long projectId, @RequestBody HostScanRequest request) {
        hostScanService.scan(projectId, request.getCidr());
    }

    @DeleteMapping("/{scanHistoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "스캔 내역 건별 삭제", description = "하나의 스캔 내역을 삭제한다.")
    @ApiResponse(responseCode = "204")
    public void removeHostScanHistory(@PathVariable("projectId") Long projectId, @PathVariable Long scanHistoryId) {
        hostScanService.removeHostScanHistory(projectId, scanHistoryId);
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "스캔 내역 전체 삭제", description = "스캔 내역 전체를 삭제한다.")
    @ApiResponse(responseCode = "204")
    public void removeHostScanHistories(@PathVariable("projectId") Long projectId) {
        hostScanService.removeHostScanHistories(projectId);
    }

    @GetMapping("/{scanHistoryId}/excel")
    @Operation(summary = "스캔 결과 목록이 포함된 Excel template 다운로드", description = "스캔 결과 발견된 호스트 목록을 excel 로 내보낸다.")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MEDIA_TYPE_EXCEL))
    public ResponseEntity<?> discoveredHostsToExcel(@PathVariable Long projectId, @PathVariable Long scanHistoryId) {
        // excel 파일의 inputStream 을 가져온다.
        ByteArrayInputStream in = hostScanService.getDiscoveredHostExcel(projectId, scanHistoryId);
        // 파일 이름 설정
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + HOST_SCAN_EXCEL_TEMPLATE_NAME + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx");
        // 헤더 설정
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }


}
