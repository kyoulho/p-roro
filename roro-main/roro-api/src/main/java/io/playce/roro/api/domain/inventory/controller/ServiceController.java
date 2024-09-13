/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       11월 04, 2021            First Draft.
 */
package io.playce.roro.api.domain.inventory.controller;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.i18n.LocaleMessageConvert;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.inventory.service.ServiceService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.inventory.service.ServiceCreateRequest;
import io.playce.roro.common.dto.inventory.service.ServiceDatasourceResponse;
import io.playce.roro.common.dto.inventory.service.ServiceDetailResponse;
import io.playce.roro.common.dto.inventory.service.ServiceResponse;
import io.playce.roro.jpa.entity.ServiceMaster;
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

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import static io.playce.roro.api.common.CommonConstants.EXCEL_RESOURCE_SERVICE_NAME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@RestController
@RequestMapping(value = "/api/projects/{projectId}/inventory")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceService serviceService;
    private final ProjectService projectService;
    private final LocaleMessageConvert localeMessageConvert;

    @Operation(summary = "서비스 목록 조회")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceResponse.class))))
    @GetMapping(path = "/services")
    public ResponseEntity<?> services(@PathVariable long projectId) {
        return ResponseEntity.ok(serviceService.getServiceList(projectId));
    }

    @Operation(summary = "서비스 상세 조회")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ServiceDetailResponse.class)))
    @GetMapping(path = "/services/{serviceId}")
    public ResponseEntity<?> service(@PathVariable long projectId,
                                     @PathVariable long serviceId) {
        return ResponseEntity.ok(serviceService.getService(projectId, serviceId));
    }

    @Operation(summary = "서비스 등록")
    @ApiResponse(responseCode = "201")
    @PostMapping(path = "/services", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createService(@PathVariable long projectId,
                                           @Valid @RequestBody ServiceCreateRequest service) {
        ServiceMaster s = serviceService.createService(projectId, service);

        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put("serviceId", s.getServiceId());
        serviceMap.put("serviceName", s.getServiceName());

        return new ResponseEntity<>(serviceMap, HttpStatus.CREATED);
    }

    @Operation(summary = "서비스 수정")
    @ApiResponse(responseCode = "200")
    @PutMapping(path = "/services/{serviceId}")
    public ResponseEntity<?> modifyService(@PathVariable long projectId,
                                           @PathVariable long serviceId,
                                           @Valid @RequestBody ServiceCreateRequest serviceCreateRequest) {
        ServiceMaster s = serviceService.modifyService(projectId, serviceId, serviceCreateRequest);

        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put("serviceId", s.getServiceId());
        serviceMap.put("serviceName", s.getServiceName());

        return ResponseEntity.ok(serviceMap);
    }

    @Operation(summary = "서비스 삭제")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(path = "/services/{serviceId}")
    public ResponseEntity<?> deleteService(@PathVariable long projectId,
                                           @PathVariable long serviceId) {
        serviceService.deleteService(projectId, serviceId);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "서비스 목록 Excel 다운로드")
    @ApiResponse(responseCode = "200")
    @GetMapping(path = "services/excel")
    public ResponseEntity<?> serviceExcelDownload(@PathVariable long projectId) {
        ByteArrayInputStream in = serviceService.getServiceListInputStream(projectId);

        String filename = ExcelUtil.generateExcelFileName(EXCEL_RESOURCE_SERVICE_NAME);
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(CommonConstants.MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);
        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "서비스 내 Datasource 목록 조회")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ServiceDatasourceResponse.class)))
    @GetMapping(path = "/services/{serviceId}/datasources")
    public ResponseEntity<?> getDatasources(@PathVariable long projectId, @PathVariable long serviceId) {
        return ResponseEntity.ok(serviceService.getDatasources(projectId, serviceId));
    }

}
//end of ServiceController.java
