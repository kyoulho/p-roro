/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Jaeeon Bae       5월 19, 2022            First Draft.
 */
package io.playce.roro.api.domain.cloudreadiness.controller;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.cloudreadiness.service.CloudReadinessService;
import io.playce.roro.api.domain.inventory.service.ServiceService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.cloudreadiness.*;
import io.playce.roro.common.dto.inventory.service.ServiceDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.utils.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.playce.roro.api.common.CommonConstants.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/api")
public class CloudReadinessController {

    private final CloudReadinessService cloudReadinessService;
    private final ProjectService projectService;
    private final ServiceService serviceService;

    @Operation(summary = "클라우드 전환 진단 평가 질문 조회", description = "클라우드 전환 진단 평가 질문을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/cloud-readiness/surveys/{surveyId}/questions")
    public ResponseEntity<?> questions(@PathVariable Long surveyId) {
        CloudReadinessQuestionResponse questions = cloudReadinessService.getQuestions(surveyId);
        return ResponseEntity.ok(questions);
    }

    @Operation(summary = "클라우드 전환 진단 평가 목록 조회", description = "클라우드 전환 진단 평가 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/projects/{projectId}/cloud-readiness/answers")
    public ResponseEntity<?> getSurveyList(@PathVariable Long projectId) {
        List<CloudReadiness> cloudReadinesses = cloudReadinessService.getSurveyList(projectId);

        return ResponseEntity.ok(cloudReadinesses);
    }

    @Operation(summary = "클라우드 전환 진단 평가 답변 저장", description = "클라우드 전환 진단 평가 답변을 저장한다.")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/cloud-readiness/surveys/{surveyId}")
    public ResponseEntity<?> save(@PathVariable Long surveyId, @RequestParam Long serviceId, @RequestBody List<CloudReadinessAnswer> answers) {
        cloudReadinessService.saveAnswers(surveyId, serviceId, answers);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "서비스 상세의 클라우드 전환 진단 평가 상세", description = "서비스 상세의 클라우드 전환 진단 평가 상세를 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/projects/{projectId}/cloud-readiness/surveys/{serviceId}")
    public ResponseEntity<?> getSurveyDetail(@PathVariable Long projectId, @PathVariable Long serviceId) {
        CloudReadinessDetail cloudReadinessDetail = cloudReadinessService.getCloudReadinessDetail(projectId, serviceId);
        return ResponseEntity.ok(cloudReadinessDetail);
    }

    @Operation(summary = "클라우드 전환 진단 평가 결과 목록 조회", description = "클라우드 전환 진단 평가 결과 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/projects/{projectId}/cloud-readiness/surveys")
    public ResponseEntity<?> getResults(@PathVariable Long projectId, @RequestParam String serviceIds) {
        List<CloudReadinessCategoryResult> cloudReadinessResultLit = cloudReadinessService.getCloudReadinessResultList(projectId, serviceIds);
        return ResponseEntity.ok(cloudReadinessResultLit);
    }

    @Operation(summary = "클라우드 전환 진단 평가 결과 레포트 다운로드", description = "클라우드 전환 진단 평가 결과 레포트를 다운로드한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/projects/{projectId}/cloud-readiness/surveys/report")
    public ResponseEntity<?> getReport(@PathVariable Long projectId, @RequestParam(required = false) String serviceIds, @RequestParam String fileType) {
        // 엑셀을 제외한 나머지 파일 타입은 현재 지원되지 않음.
        if (!EXCEL_FILE_TYPE.equalsIgnoreCase(fileType)) {
            throw new RoRoApiException(ErrorCode.INVALID_FILE_TYPE);
        }

        ByteArrayOutputStream out = cloudReadinessService.getCloudReadinessReport(projectId, serviceIds, fileType);

        String fileName = null;
        if (StringUtils.isEmpty(serviceIds) || serviceIds.split(",").length > 1) {
            fileName = projectService.getProjectName(projectId);
        } else {
            ServiceDetailResponse serviceDetailResponse = serviceService.getService(projectId, Long.parseLong(serviceIds));
            fileName = projectService.getProjectName(projectId) + "_" + serviceDetailResponse.getServiceName();
        }

        String filename = ExcelUtil.generateExcelFileName(fileName + "_" + CLOUD_READINESS_EXCEL_REPORT_NAME);
        String encodedFileName = FileUtil.getEncodeFileName(filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(CommonConstants.MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())), responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "클라우드 전환 진단 평가 템플릿 다운로드", description = "클라우드 전환 진단 평가 템플릿 파일를 다운로드한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/projects/{projectId}/cloud-readiness/surveys/template")
    public ResponseEntity<?> getTemplate(@PathVariable Long projectId) {
        ByteArrayOutputStream out = cloudReadinessService.getCloudReadinessTemplate(projectId);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_"
                + CLOUD_READINESS_EXCEL_TEMPLATE_NAME + "_" + format.format(new Date()) + "." + EXCEL_EXTENSION_XLSX);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(CommonConstants.MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())), responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "클라우드 전환 진단 평가 업로드", description = "클라우드 전환 진단 평가를 파일로 업로드한다.")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/projects/{projectId}/cloud-readiness/surveys/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@PathVariable Long projectId, @RequestPart(value = "templateFile", required = false) MultipartFile templateFile) {
        List<CloudReadinessUploadSuccess> successList = null;
        List<CloudReadinessUploadFail> validationList = new ArrayList<>();
        boolean isSuccess = false;

        if (templateFile != null && templateFile.getSize() > 0) {
            String sourceFileName = UUID.randomUUID() + "." + FilenameUtils.getExtension(templateFile.getOriginalFilename());

            try {
                // 파일 확장자 (xlsx, xls) 체크
                if (!(EXCEL_EXTENSION_XLSX.equals(FilenameUtils.getExtension(sourceFileName))
                        || EXCEL_EXTENSION_XLS.equals(FilenameUtils.getExtension(sourceFileName)))) {
                    throw new RoRoApiException(ErrorCode.INVALID_FILE_TYPE, "Cloud-Readiness Upload file type doesn't support.");
                }
                String sourceFilePath = FileUtil.saveFile(templateFile, sourceFileName, INVENTORY_FILE_UPLOAD_DIR);
                log.debug("[{}] file saved to [{}]", templateFile.getOriginalFilename(), sourceFilePath);
                File file = new File(sourceFilePath);
                XSSFWorkbook workbook = (XSSFWorkbook) ExcelUtil.getWorkbook(new FileInputStream(file), file.getName());

                if (workbook.getSheet("Step 01_Business Factors") == null || workbook.getSheet("Step 02_Technical Factors") == null) {
                    throw new RoRoApiException(ErrorCode.INVALID_FILE_TYPE);
                }

                // parse of cloud readiness excel template
                Map<Long, List<CloudReadinessAnswer>> surveyQustionAnswerMap = cloudReadinessService.parseCloudReadiness(workbook, validationList);

                // validate cloud readiness
                cloudReadinessService.validateCloudReadiness(surveyQustionAnswerMap, validationList, projectId);

                // 유효성 체크 항목이 없으면 Upload 실행
                if (CollectionUtils.isEmpty(validationList)) {
                    successList = cloudReadinessService.uploadCloudReadiness(surveyQustionAnswerMap);
                    isSuccess = true;
                }
            } catch (Exception e) {
                log.debug("Unhandled exception occurred while upload cloud-readiness.", e);
            }
        }

        return isSuccess ? ResponseEntity.ok(successList) : ResponseEntity.badRequest().body(validationList);
    }
}