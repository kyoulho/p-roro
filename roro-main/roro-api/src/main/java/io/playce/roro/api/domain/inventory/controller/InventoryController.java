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
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.inventory.service.ExportToExcelService;
import io.playce.roro.api.domain.inventory.service.InventoryService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.dto.inventory.inventory.InventoryUploadFail;
import io.playce.roro.common.dto.inventory.inventory.InventoryUploadSuccess;
import io.playce.roro.common.dto.inventory.inventory.UploadInventoryResponse;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.excel.template.ExcelTemplateService;
import io.playce.roro.excel.template.vo.SheetMap;
import io.playce.roro.jpa.entity.UploadInventory;
import io.playce.roro.jpa.repository.UploadInventoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.playce.roro.api.common.CommonConstants.*;

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
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Transactional
public class InventoryController {

    private final ProjectService projectService;
    private final InventoryService inventoryService;
    private final ExcelTemplateService excelTemplateService;
    private final ExportToExcelService exportToExcelService;
    private final UploadInventoryRepository uploadInventoryRepository;

    @Operation(summary = "Upload 인벤토리 목록 조회", description = "Upload 한 인벤토리 목록을 조회힌다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/inventory")
    public ResponseEntity<?> inventories(@PathVariable long projectId) {
        return ResponseEntity.ok(inventoryService.getUploadInventoryList(projectId));
    }

    @Operation(summary = "Inventory count 조회", description = "Inventory count 를 조회한다. \n " +
            "- inventoryTypeCode : [SVR | DBMS]")
    @ApiResponse(responseCode = "200")
    @GetMapping("/count")
    public ResponseEntity<?> inventoryCount(@PathVariable long projectId,
                                            @RequestParam String inventoryTypeCode) {
        return ResponseEntity.ok(inventoryService.getInventoryCount(projectId, inventoryTypeCode));
    }

    @Operation(summary = "Upload 한 인벤토리 파일 다운로드", description = "Upload 한 인벤토리 파일을 다운로드 한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/upload-templates/{uploadInventoryId}/download")
    public ResponseEntity<InputStreamResource> attachments(@PathVariable long projectId,
                                                           @PathVariable long uploadInventoryId) throws Exception {
        UploadInventoryResponse inventoryResponse = inventoryService.getInventory(projectId, uploadInventoryId);
        InputStreamResource resource = null;
        HttpHeaders responseHeader = new HttpHeaders();

        if (inventoryResponse != null) {
            String downloadFile = inventoryResponse.getFilePath();
            String fileName = FileUtil.getEncodeFileName(inventoryResponse.getFileName());

            File file = new File(downloadFile);
            responseHeader.setContentType(MediaType.parseMediaType(CommonConstants.MEDIA_TYPE_EXCEL));
            responseHeader.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment;filename=" + fileName + ";filename*=UTF-8''" + fileName);
            resource = new InputStreamResource(new FileInputStream(file));
        } else {
            throw new RoRoApiException(ErrorCode.RESOURCE_NOT_FOUND, "File does not exist.");
        }

        return new ResponseEntity<>(resource, responseHeader, HttpStatus.OK);
    }

    @Operation(summary = "인벤토리 일괄 등록", description = "템플릿을 통해 bulk로 인벤토리를 등록한다.")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/upload-templates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadInventory(@PathVariable Long projectId,
                                             @RequestPart(value = "templateFile", required = false) MultipartFile templateFile) {
        List<InventoryUploadSuccess> successList = null;
        List<InventoryUploadFail> validationList = new ArrayList<>();
        boolean isSuccess = false;
        String sourceFilePath = null;
        XSSFWorkbook workbook = null;

        UploadInventory uploadInventory = new UploadInventory();

        if (templateFile != null && templateFile.getSize() > 0) {
            String sourceFileName = UUID.randomUUID() + "." + FilenameUtils.getExtension(templateFile.getOriginalFilename());

            try {
                // 파일 확장자 (xlsx, xls) 체크
                if (!(EXCEL_EXTENSION_XLSX.equals(FilenameUtils.getExtension(sourceFileName))
                        || EXCEL_EXTENSION_XLS.equals(FilenameUtils.getExtension(sourceFileName)))) {
                    throw new RoRoApiException(ErrorCode.INVALID_FILE_TYPE, "Inventory Upload file type doesn't support.");
                }
                sourceFilePath = FileUtil.saveFile(templateFile, sourceFileName, INVENTORY_FILE_UPLOAD_DIR);
                log.debug("[{}] file saved to [{}]", templateFile.getOriginalFilename(), sourceFilePath);
                File file = new File(sourceFilePath);

                workbook = (XSSFWorkbook) ExcelUtil.getWorkbook(new FileInputStream(file), file.getName());

                if (workbook.getSheet("guide") == null || workbook.getSheet("service") == null ||
                        workbook.getSheet("server") == null || workbook.getSheet("middleware") == null ||
                        workbook.getSheet("application") == null || workbook.getSheet("database") == null ||
                        workbook.getSheet("service-mapping") == null) {
                    workbook = null;
                    throw new RoRoApiException(ErrorCode.INVALID_FILE_TYPE);
                }

                // parse excel data
                SheetMap result;
                try {
                    result = excelTemplateService.parseWorkbook(workbook, validationList);
                } catch (Exception e) {
                    if (e instanceof RoRoException && e.getMessage().equals("INVALID_FILE_TYPE")) {
                        throw new RoRoApiException(ErrorCode.INVALID_FILE_TYPE);
                    } else {
                        throw e;
                    }
                }

                // validation check
                validationList = inventoryService.validateInventoryUpload(result, validationList, projectId);

                // 기본 데이터 설정
                uploadInventory.setProjectId(projectId);
                uploadInventory.setFileName(templateFile.getOriginalFilename());
                uploadInventory.setFilePath(file.getAbsolutePath());
                uploadInventory.setDeleteYn("N");
                uploadInventory.setRegistUserId(WebUtil.getUserId());
                uploadInventory.setRegistDatetime(new Date());
                uploadInventory.setModifyUserId(WebUtil.getUserId());
                uploadInventory.setModifyDatetime(new Date());

                if (!(validationList != null && !validationList.isEmpty())) {
                    // upload inventory
                    successList = inventoryService.uploadInventory(result, projectId, uploadInventory);
                    isSuccess = true;
                }
            } catch (Exception e) {
                log.debug("Unhandled exception occurred while upload inventory.", e);

                if (StringUtils.isNotEmpty(uploadInventory.getUploadProcessResultTxt())) {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();

                    if (uploadInventory.getServiceCount() == -1) {
                        inventoryUploadFail.setSheet("service");
                    }
                    if (uploadInventory.getServerCount() == -1) {
                        inventoryUploadFail.setSheet("server");
                    }
                    if (uploadInventory.getMiddlewareCount() == -1) {
                        inventoryUploadFail.setSheet("middleware");
                    }
                    if (uploadInventory.getApplicationCount() == -1) {
                        inventoryUploadFail.setSheet("application");
                    }
                    if (uploadInventory.getDbmsCount() == -1) {
                        inventoryUploadFail.setSheet("database");
                    }

                    uploadInventory.setServiceCount(0);
                    uploadInventory.setServerCount(0);
                    uploadInventory.setMiddlewareCount(0);
                    uploadInventory.setApplicationCount(0);
                    uploadInventory.setDbmsCount(0);
                    uploadInventory.setUploadStatusTypeCode(UPLOAD_STATUS_TYPE_CODE_FAIL);
                    uploadInventoryRepository.save(uploadInventory);

                    inventoryUploadFail.setFailDetail(uploadInventory.getUploadProcessResultTxt());

                    validationList = new ArrayList<>();
                    validationList.add(inventoryUploadFail);
                }

                // if (e instanceof RoRoApiException) {
                //     throw e;
                // }
            } finally {
                // https://cloud-osci.atlassian.net/browse/PCR-6139
                if (workbook != null && StringUtils.isNotEmpty(sourceFilePath)) {
                    Sheet sheet;
                    Row row;
                    Cell cell;

                    try (FileOutputStream outputStream = new FileOutputStream(sourceFilePath)) {
                        sheet = workbook.getSheet("server");
                        for (int i = 2; i < sheet.getPhysicalNumberOfRows(); i++) {
                            row = sheet.getRow(i);

                            if (row != null) {
                                // Password
                                cell = row.getCell(7);

                                if (cell != null) {
                                    cell.setCellValue("");
                                }

                                // Root Password
                                cell = row.getCell(11);

                                if (cell != null) {
                                    cell.setCellValue("");
                                }
                            }
                        }

                        sheet = workbook.getSheet("middleware");
                        for (int i = 2; i < sheet.getPhysicalNumberOfRows(); i++) {
                            row = sheet.getRow(i);

                            if (row != null) {
                                // Password
                                cell = row.getCell(6);

                                if (cell != null) {
                                    cell.setCellValue("");
                                }
                            }
                        }

                        sheet = workbook.getSheet("application");
                        for (int i = 2; i < sheet.getPhysicalNumberOfRows(); i++) {
                            row = sheet.getRow(i);

                            if (row != null) {
                                // Password
                                cell = row.getCell(6);

                                if (cell != null) {
                                    cell.setCellValue("");
                                }
                            }
                        }

                        sheet = workbook.getSheet("database");
                        for (int i = 2; i < sheet.getPhysicalNumberOfRows(); i++) {
                            row = sheet.getRow(i);

                            if (row != null) {
                                // Password
                                cell = row.getCell(11);

                                if (cell != null) {
                                    cell.setCellValue("");
                                }
                            }
                        }

                        workbook.write(outputStream);
                    } catch (Exception e) {
                        log.error("Unhandled exception occurred while upload inventory after remove password.", e);
                    }
                }
            }
        }

        return isSuccess ? ResponseEntity.ok(successList) : ResponseEntity.badRequest().body(validationList);
    }

    @Operation(summary = "인벤토리 Export to Excel", description = "인벤토리를 Export하여 엑셀로 다운로드 받는다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/export-excel")
    public ResponseEntity<InputStreamResource> exportToExcel(@PathVariable Long projectId) {
        ByteArrayInputStream in = exportToExcelService.exportToExcel(projectId);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + INVENTORY + "_" + format.format(new Date()) + ".xlsx");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "서비스 현황조사서 다운로드", description = "서비스 현황조사서를 엑셀로 다운로드 받는다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/public-agency-report")
    public ResponseEntity<InputStreamResource> getPublicAgencyReport(@PathVariable Long projectId) {
        ByteArrayOutputStream out = exportToExcelService.getPublicAgencyReport(projectId);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + PUBLIC_AGENCY_REPORT + "_" + format.format(new Date()) + ".xlsx");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())), responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "RoRo 프로젝트 레포트 다운로드", description = "RoRo 프로젝트 레포트를 엑셀로 다운로드 받는다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/roro-report")
    public ResponseEntity<InputStreamResource> getRoroReport(@PathVariable Long projectId) {
        ByteArrayOutputStream out = exportToExcelService.getRoroReport(projectId);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + RORO_REPORT + "_" + format.format(new Date()) + ".xlsx");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())), responseHeaders, HttpStatus.OK);
    }
}
//end of InventoryController.java
