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
package io.playce.roro.api.domain.assessment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.assessment.service.AssessmentService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.assessment.AssessmentRequestDto;
import io.playce.roro.common.dto.assessment.AssessmentResponseDto;
import io.playce.roro.common.dto.assessment.PageAssessmentRequestDto;
import io.playce.roro.common.dto.inventory.process.InventoryProcessDetailResponse;
import io.playce.roro.common.dto.inventory.process.InventoryProcessListResponse;
import io.playce.roro.common.dto.inventory.server.JavaProcessResponse;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareDto;
import io.playce.roro.api.domain.tracking.TrackingInfoService;
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

import static io.playce.roro.api.common.CommonConstants.ASSESSMENT_TABLE;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@RestController
@RequestMapping(value = "/api/projects/{projectId}/assessments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final ProjectService projectService;
    private final TrackingInfoService trackingInfoService;

    /**
     * Add assessment.
     *
     * @param projectId the project id
     * @param request   the request
     */
    // API Spec에 존재하지 않는 API로 테스트용인 것 같음
    // @Operation(description = "선택된 대상 Server의 Assessment를 요청한다.")
    // @PostMapping
    // @ResponseStatus(HttpStatus.CREATED)
    // public void addAssessment(@PathVariable("projectId") Long projectId, @RequestBody InventoryProcessRequest request) {
    //     inventoryProcessService.addInventoryProcess(projectId, request, Domain1002.SCAN);
    // }

    /**
     * <pre>
     * 서비스/서버/미들웨어/애플리케이션/데이터베이스 Assessment 요청
     * </pre>
     *
     * @param projectId            the project id
     * @param assessmentRequestDto the assessmentRequestDto
     * @return response entity
     */
    @Operation(summary = "서비스/서버/미들웨어/애플리케이션/데이터베이스 Assessment 요청",
            description = "서비스(SERV)/서버(SVR)/미들웨어(MW)/애플리케이션(APP)/데이터베이스(DBMS) Assessment를 요청한다.")
    @ApiResponse(responseCode = "200")
    @PostMapping
    public ResponseEntity<?> assessment(@PathVariable("projectId") Long projectId,
                                        @RequestBody AssessmentRequestDto assessmentRequestDto) {
        List<AssessmentResponseDto> response = assessmentService.createAssessments(projectId, assessmentRequestDto.getInventoryTypeCode(), assessmentRequestDto.getInventoryIds());
        return ResponseEntity.ok(response);
    }

    /**
     * <pre>
     * 기존에 완료된 또는 진행중인 예약된 Assessment Task 목록 조회
     * </pre>
     *
     * @param projectId            the project id
     * @param inventoryId          the inventory id
     * @param assessmentRequestDto the assessmentRequestDto
     * @return response entity
     */
    @Operation(summary = "Assessment 목록 조회", description = "완료/예약된 Assessment Task 목록 조회")
    @ApiResponse(responseCode = "200")
    @GetMapping
    public ResponseEntity<?> assessments(@PathVariable long projectId,
                                         @RequestParam(value = "inventoryId", required = false) Long inventoryId,
                                         @ModelAttribute PageAssessmentRequestDto assessmentRequestDto) {
        InventoryProcessListResponse response = assessmentService.getAssessment(projectId, inventoryId, assessmentRequestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Assessment 목록 CSV 다운로드", description = "Assessment 목록 CSV로 다운로드 한다.")
    @GetMapping("/csv-download")
    public ResponseEntity<?> assessmentsCsvDownload(@PathVariable Long projectId,
                                                    @ModelAttribute PageAssessmentRequestDto pageAssessmentRequestDto) {
        ByteArrayInputStream in = assessmentService.getAssessmentCsvDownload(projectId, pageAssessmentRequestDto);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + ASSESSMENT_TABLE + "_" + format.format(new Date()) + ".csv");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType("text/csv"));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }

    /**
     * Get assessment.
     *
     * @param projectId    the project id
     * @param assessmentId the assessmentId
     * @return the assessment
     */
    @Operation(summary = "Assessment 상세 조회", description = "id를 이용하여 Assessment를 상세 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/{assessmentId}")
    public ResponseEntity<?> getAssessment(@PathVariable long projectId, @PathVariable long assessmentId) {
        InventoryProcessDetailResponse assessment = assessmentService.getAssessment(projectId, assessmentId);
        return ResponseEntity.ok(assessment);
    }

    /**
     * <pre>
     * 서버의 Java 프로세스 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param assessmentId
     * @return
     */
    @Operation(summary = "서버의 Java 프로세스 목록 조회", description = "서버의 Java 프로세스 목록을 조회한다.")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = JavaProcessResponse.class))))
    @GetMapping("/{assessmentId}/java-process")
    public ResponseEntity<?> getJavaProcessList(@PathVariable Long projectId, @PathVariable Long assessmentId) {
        List<JavaProcessResponse> response = assessmentService.getJavaProcessList(projectId, assessmentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Assessment 상세 항목 조회", description = "Assessment를 상세 조회의 jsonMetas를 통해서 Split된 json을 불러온다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/{assessmentId}/detail")
    public ResponseEntity<?> getAssessmentDetail(@PathVariable long projectId, @PathVariable long assessmentId, @RequestParam("path") String path) {
        JsonNode jsonNode = assessmentService.getAssessmentDetail(assessmentId, path);
        return ResponseEntity.ok(jsonNode);
    }

    // /**
    //  * Download response entity.
    //  *
    //  * @param filePath the file path
    //  *
    //  * @return the response entity
    //  *
    //  * @throws IOException the io exception
    //  */
    // @Operation(summary = "Assessment 파일 다운로드", description = "Assessment 결과 등 범용 File Download API로, 파일 경로에는 파일명이 포함되어야 한다.")
    // @GetMapping(value = "/assessment/file-download")
    // public ResponseEntity<InputStreamResource> download(@Valid @RequestParam("filePath") String filePath) throws IOException {
    //     File file = assessmentService.getTargetFile(filePath);
    //
    //     String encodedFileName = FileUtil.getEncodeFileName(file.getName());
    //
    //     HttpHeaders responseHeaders = new HttpHeaders();
    //     responseHeaders.setContentType(MediaType.parseMediaType(new Tika().detect(file)));
    //     responseHeaders.setContentLength(file.length());
    //     responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
    //             "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);
    //
    //     InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
    //     return new ResponseEntity<>(resource, responseHeaders, HttpStatus.OK);
    // }

    /**
     * <pre>
     * Assessment Task Cancel
     * </pre>
     *
     * @param projectId    the project id
     * @param assessmentId the assessmentId
     * @return response entity
     */
    @Operation(summary = "Assessment 취소", description = "Assessment Task 취소")
    @ApiResponse(responseCode = "200")
    @PatchMapping(value = "/{assessmentId}")
    public ResponseEntity<?> stopAssessment(@PathVariable long projectId, @PathVariable Long assessmentId) {
        assessmentService.stopAssessment(projectId, assessmentId);
        return ResponseEntity.ok().build();
    }

    /**
     * <pre>
     * Assessment Task를 삭제한다.
     * </pre>
     *
     * @param projectId    the project id
     * @param assessmentId the assessmentId
     * @return response entity
     * @throws Exception the exception
     */
    @Operation(summary = "Assessment Task 삭제", description = "Assessment Task 삭제")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(value = "/{assessmentId}")
    public ResponseEntity<?> removeAssessment(@PathVariable Long projectId, @PathVariable Long assessmentId) {
        assessmentService.removeAssessment(projectId, assessmentId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Assessment 추적 정보 비교", description = "스캔 추적정보 비교")
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/compare")
    public List<TrackingInfoCompareDto> compareAssessment(@PathVariable Long projectId, @RequestParam Long selectedAssessmentId, @RequestParam Long compareAssessmentId) {
        return trackingInfoService.getTrackingInfoForCompare(selectedAssessmentId, compareAssessmentId);
    }


}
//end of AssessmentController.java
