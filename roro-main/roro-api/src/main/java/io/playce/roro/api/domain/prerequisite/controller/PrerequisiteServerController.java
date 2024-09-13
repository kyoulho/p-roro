package io.playce.roro.api.domain.prerequisite.controller;

import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.api.domain.prerequisite.service.PrerequisiteService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.dto.inventory.process.InventoryProcessRequest;
import io.playce.roro.common.dto.prerequisite.PrerequisiteDto;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.List;

import static io.playce.roro.api.common.CommonConstants.MEDIA_TYPE_EXCEL;

@RestController
@RequestMapping(path = "/api/projects/{projectId}/prerequisites", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class PrerequisiteServerController {
    private final PrerequisiteService service;
    private final InventoryProcessService inventoryProcessService;
    private final ProjectService projectService;

    @Operation(description = "전제조건환경분석 목록을 조회한다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PrerequisiteDto.PrerequisiteResponse> getPrerequisites(@PathVariable("projectId") Long projectId) {
        return service.getPrerequisites(projectId);
    }

    @Operation(description = "선택된 대상 Server의 전제조건환경을 요청한다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addPrerequisites(@PathVariable("projectId") Long projectId, @RequestBody InventoryProcessRequest request) {
        inventoryProcessService.addInventoryProcess(projectId, request, Domain1002.PREQ);
    }

    @Operation(description = "전제조건환경분석 이력을 조회한다.")
    @GetMapping("histories")
    @ResponseStatus(HttpStatus.OK)
    public List<PrerequisiteDto.PrerequisiteHistoryResponse> getPrerequisiteHistory(@PathVariable("projectId") Long projectId, @RequestParam("from") String from, @RequestParam("to") String to) {
        return service.getPrerequistieHistory(projectId, from, to);
    }

    @Operation(description = "전제조건환경분석 Server 결과를 export한다.")
    @GetMapping(value = "/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InputStreamResource> serverPrerequisteExport(@PathVariable("projectId") Long projectId) {
        ByteArrayInputStream in = service.getPrerequisitesExcel(projectId);
        String fileName = ExcelUtil.generateExcelFileName("Prerequisite");
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + fileName);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }
}
