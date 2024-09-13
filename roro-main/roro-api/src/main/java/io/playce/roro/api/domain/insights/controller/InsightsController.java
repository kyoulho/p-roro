/*
 * Copyright 2023 The playce-roro-v3 Project.
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
 * SangCheon Park   Jan 11, 2023		    First Draft.
 */
package io.playce.roro.api.domain.insights.controller;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.common.aop.SubscriptionManager;
import io.playce.roro.api.domain.insights.service.InsightsService;
import io.playce.roro.api.domain.inventory.service.ServiceService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.insights.BillboardDetailResponse;
import io.playce.roro.common.dto.insights.BillboardResponse;
import io.playce.roro.common.dto.insights.InsightListDto;
import io.playce.roro.common.dto.insights.LifecycleResponse;
import io.playce.roro.common.dto.inventory.service.ServiceDetailResponse;
import io.playce.roro.common.dto.subscription.Subscription;
import io.playce.roro.common.dto.subscription.SubscriptionStausType;
import io.playce.roro.common.dto.subscription.SubscriptionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static io.playce.roro.api.common.CommonConstants.INSIGHTS_EXCEL_REPORT_NAME;
import static io.playce.roro.api.common.CommonConstants.INSIGHTS_PDF_GUIDE_NAME;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@RestController
@RequestMapping(value = "/api/projects/{projectId}/insights")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class InsightsController {

    private final InsightsService insightsService;
    private final ProjectService projectService;
    private final ServiceService serviceService;

    @Operation(summary = "Insights - 목록 조회", description = "Insights -  목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = InsightListDto.class))))
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/list")
    public InsightListDto getInsights(@PathVariable Long projectId, @RequestParam Integer within, @RequestParam(required = false) String serviceIds) {
        return insightsService.getInsights(projectId, within, serviceIds, false);
    }

    @Operation(summary = "InsightDto 조회", description = "등록된 인벤토리의 eol,eos 정보를 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LifecycleResponse.class)))
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public LifecycleResponse getLifecycle(@PathVariable Long projectId, @RequestParam Long inventoryId, @RequestParam(required = false) String type) {
        return insightsService.getLifecycleResponse(projectId, inventoryId, type);
    }

    /**
     * <pre>
     * Insights 레포트 다운로드
     * </pre>
     *
     * @param projectId  the project id
     * @param within     the within
     * @param serviceIds the service ids
     *
     * @return response entity
     */
    @Operation(summary = "Insights 레포트 다운로드", description = "Insights 레포트를 다운로드한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/report")
    public ResponseEntity<?> getReport(@PathVariable Long projectId,
                                       @RequestParam Integer within,
                                       @RequestParam(required = false) String serviceIds) {
        ByteArrayOutputStream out = insightsService.getInsightsReport(projectId, within, serviceIds);

        String fileName;
        if (StringUtils.isEmpty(serviceIds) || serviceIds.split(",").length > 1) {
            fileName = projectService.getProjectName(projectId);
        } else {
            ServiceDetailResponse serviceDetailResponse = serviceService.getService(projectId, Long.parseLong(serviceIds));
            fileName = projectService.getProjectName(projectId) + "_" + serviceDetailResponse.getServiceName();
        }

        String filename = ExcelUtil.generateExcelFileName(fileName + "_" + INSIGHTS_EXCEL_REPORT_NAME);
        String encodedFileName = FileUtil.getEncodeFileName(filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(CommonConstants.MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())), responseHeaders, HttpStatus.OK);
    }

    /**
     * <pre>
     * Insights 가이드 다운로드
     * </pre>
     *
     * @param projectId the project id
     *
     * @return response entity
     */
    @Operation(summary = "Insights 가이드 다운로드", description = "Insights 가이드 문서를 다운로드한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/guide-download")
    public ResponseEntity<?> guideDownload(@PathVariable Long projectId) {
        Subscription subscription = SubscriptionManager.getSubscription();
        if (subscription.getType().equals(SubscriptionType.TRIAL) || !subscription.getSubscriptionStausType().equals(SubscriptionStausType.SUBSCRIPTION_VALID)) {
            throw new RoRoApiException(ErrorCode.SUBSCRIPTION_NOT_ALLOWED2);
        }

        String fileName = projectService.getProjectName(projectId);

        String filename = ExcelUtil.generatePdfFileName(fileName + "_" + INSIGHTS_PDF_GUIDE_NAME);
        String encodedFileName = FileUtil.getEncodeFileName(filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_PDF);
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);
        return new ResponseEntity<>(new InputStreamResource(getClass().getResourceAsStream("/template/Insights_Reference_Guide.pdf")), responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "Insights 전광판 조회", description = "Insights 전광판을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillboardResponse.class)))
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/billboard")
    public BillboardResponse getInsightsBillboard(@PathVariable Long projectId,
                                                  @RequestParam Integer within,
                                                  @RequestParam(required = false) String serviceIds) {
        return insightsService.getBillboard(projectId, within, serviceIds);
    }

    @Operation(summary = "Insights 전광판 상세 조회", description = "Insights 전광판 상세정보를 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillboardDetailResponse.class)))
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/billboard-details")
    public BillboardDetailResponse getInsightsBillboardDetail(@PathVariable Long projectId) {
        return insightsService.getBillboardDetail(projectId);
    }
}
//end of InsightsController.java