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
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.common.service.UserService;
import io.playce.roro.api.domain.inventory.service.ReportService;
import io.playce.roro.api.domain.project.service.ProjectService;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.dto.inventory.process.InventoryProcessResponse;
import io.playce.roro.common.dto.inventory.report.*;
import io.swagger.v3.oas.annotations.Operation;
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

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.playce.roro.api.common.CommonConstants.*;
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
public class ReportController {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private final UserService userService;
    private final ReportService reportService;
    private final ProjectService projectService;

    @Operation(summary = "사용자의 카테고리 별 패턴 목록 조회", description = "사용자의 카테고리 별 패턴 목록을 조회한다.\n" +
            "- inventoryTypeCode (필수) : SERV / SVR / MW / APP / DBMS 중 선택")
    @ApiResponse(responseCode = "200")
    @GetMapping("/pattern")
    public ResponseEntity<?> getUserConfig(@PathVariable Long projectId,
                                           @RequestParam String inventoryTypeCode) {
        SettingType type;

        if (CommonConstants.SERVICE_TYPE_CODE.equals(inventoryTypeCode)) {
            type = SettingType.SERVICE_REPORT_PATTERN;
        } else if (Domain1001.SVR.name().equals(inventoryTypeCode)) {
            type = SettingType.SERVER_REPORT_PATTERN;
        } else if (Domain1001.MW.name().equals(inventoryTypeCode)) {
            type = SettingType.MIDDLEWARE_REPORT_PATTERN;
        } else if (Domain1001.APP.name().equals(inventoryTypeCode)) {
            type = SettingType.APPLICATION_REPORT_PATTERN;
        } else if (Domain1001.DBMS.name().equals(inventoryTypeCode)) {
            type = SettingType.DATABASE_REPORT_PATTERN;
        } else {
            return new ResponseEntity<>("Unrecognized Category", HttpStatus.BAD_REQUEST);
        }

        String value = userService.getUserConfig(projectId, WebUtil.getUserId(), type);

        List<Pattern> patterns = new ArrayList<>();

        if (StringUtils.isNotEmpty(value)) {
            String[] values = value.split(",");

            for (String v : values) {
                Pattern p = Pattern.valueOf(v);
                patterns.add(p);
            }
        }

        // pattern이 설정 되어있지 않은 경우, default값을 설정해준다.
        if (patterns.size() == 0) {
            patterns = reportService.getDefaultPatterns(patterns);
        }

        return ResponseEntity.ok(patterns);
    }

    @Operation(summary = "사용자의 카테고리 별 패턴 목록 저장", description = "사용자의 카테고리 별 패턴 목록을 저장한다.\n" +
            "- inventoryTypeCode (필수) : SERV / SVR / MW / APP / DBMS 중 선택 \n" +
            "- pattern (필수) : 사용자가 지정한 파일 명 패턴 목록 (아래 내용에서 확인) \n" +
            " - TYPE,\n ID,\n NAME,\n SCANNED_DATE,\n PROJECT_NAME,\n SERVICE_NAME,\n SERVER_NAME,\n SERVICE_BUSINESS_CODE,\n SERVICE_BUSINESS_CATEGORY,\n SERVER_IP_ADDRESS,\n" +
            "SERVER_PORT,\n SERVER_USERNAME,\n MIDDLEWARE_TYPE,\n MIDDLEWARE_VENDOR,\n MIDDLEWARE_ENGINE_NAME,\n MIDDLEWARE_ENGINE_VERSION,\n APPLICATION_TYPE,\n" +
            "DATABASE_ENGINE_NAME,\n DATABASE_PORT,\n DATABASE_SERVICE_NAME,\n DATABASE_USERNAME")
    @ApiResponse(responseCode = "201")
    @PostMapping(value = "/pattern", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> savePattern(@PathVariable Long projectId,
                                         @Valid @RequestBody PatternDto patternDto) {
        String inventoryTypeCode = patternDto.getInventoryTypeCode();

        List<Pattern> patterns = patternDto.getPatterns();

        SettingType type;

        if (CommonConstants.SERVICE_TYPE_CODE.equals(inventoryTypeCode)) {
            type = SettingType.SERVICE_REPORT_PATTERN;
        } else if (Domain1001.SVR.name().equals(inventoryTypeCode)) {
            type = SettingType.SERVER_REPORT_PATTERN;
        } else if (Domain1001.MW.name().equals(inventoryTypeCode)) {
            type = SettingType.MIDDLEWARE_REPORT_PATTERN;
        } else if (Domain1001.APP.name().equals(inventoryTypeCode)) {
            type = SettingType.APPLICATION_REPORT_PATTERN;
        } else if (Domain1001.DBMS.name().equals(inventoryTypeCode)) {
            type = SettingType.DATABASE_REPORT_PATTERN;
        } else {
            return new ResponseEntity<>("Unrecognized Category", HttpStatus.BAD_REQUEST);
        }

        StringBuilder sb = new StringBuilder();

        for (Pattern pattern : patterns) {
            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(pattern.name());
        }

        userService.saveUserConfig(projectId, WebUtil.getUserId(), type, sb.toString());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("inventoryTypeCode", inventoryTypeCode);
        resultMap.put("pattern", patterns);

        return ResponseEntity.ok(resultMap);
    }

    @Operation(summary = "레포트 통합 다운로드", description = "레포트 파일을 다운로드한다.\n" +
            "- inventoryTypeCode (필수) : SERV / SVR / MW / APP / DBMS 중 선택\n" +
            "- serviceId (옵션) : 서비스 내의 서버, 미들웨어, 애플리케이션, 데이터베이스를 선택하는 경우\n" +
            "- serverInventoryId (옵션) : 서버 내의 미들웨어, 애플리케이션, 데이터베이스를 선택하는 경우\n" +
            "- inventoryIds (옵션) : 서비스, 서버, 미들웨어, 애플리케이션, 데이터베이스의 ID 목록, 지정되지 않으면 전체 (',' 구분자)\n" +
            "- inventoryProcessId (옵션) : 레포트 대상 Inventory Process ID\n" +
            "- fileType (필수) : EXCEL / JSON 중 선택")
    @ApiResponse(responseCode = "200")
    @GetMapping("/report")
    public ResponseEntity<?> reportDownload(@PathVariable Long projectId, @RequestParam String inventoryTypeCode,
                                            @RequestParam(required = false) Long serviceId, @RequestParam(required = false) Long serverInventoryId,
                                            @RequestParam(required = false) List<Long> inventoryIds, @RequestParam(required = false) Long inventoryProcessId,
                                            @RequestParam String fileType) throws Exception {

        List<Pattern> patterns = getPatterns(projectId, inventoryTypeCode);
        log.debug("InventoryTypeCode : [{}], Service ID : [{}], Server ID : [{}], InventoryProcess ID : [{}], File Type : [{}], patterns : [{}]"
                , inventoryTypeCode, serviceId, serverInventoryId, inventoryProcessId, fileType, patterns);

        ByteArrayInputStream bais = null;
        InputStreamResource resource = null;
        HttpHeaders headers = null;
        String fileName;

        try {
            if (inventoryIds != null && inventoryIds.size() == 1) {
                if (CommonConstants.SERVICE_TYPE_CODE.equals(inventoryTypeCode)) {
                    if (FileType.EXCEL.name().equals(fileType)) {
                        ByteArrayInputStream in = reportService.serviceReport(projectId, inventoryIds.get(0));

                        fileName = reportService.getFileName(inventoryTypeCode, inventoryIds.get(0), patterns, fileType, new Date());
                        String encodedFileName = FileUtil.getEncodeFileName(fileName);

                        headers = new HttpHeaders();
                        headers.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
                        headers.setContentLength(in.available());
                        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

                        resource = new InputStreamResource(in);
                    } else {
                        throw new RoRoApiException(ErrorCode.INVENTORY_SERVICE_JSON_NOT_SUPPORT);
                    }

                } else {
                    InventoryProcessResponse inventoryProcessResult;

                    if (inventoryProcessId != null) {
                        inventoryProcessResult = reportService.getInventoryProcess(inventoryTypeCode, inventoryIds.get(0), inventoryProcessId);
                    } else {
                        inventoryProcessResult = reportService.getInventoryProcess(inventoryTypeCode, inventoryIds.get(0));
                    }

                    if (inventoryProcessResult == null) {
                        throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE, "The COMPLETE inventory process does not exist.");
                    }

                    headers = new HttpHeaders();
                    File file = null;
                    try {
                        if (FileType.EXCEL.name().equals(fileType)) {
                            file = new File(inventoryProcessResult.getInventoryProcessResultExcelPath());
                            headers.setContentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
                        } else if (FileType.JSON.name().equals(fileType)) {
                            file = new File(inventoryProcessResult.getInventoryProcessResultJsonPath());
                            headers.setContentType(MediaType.parseMediaType(MEDIA_TYPE_JSON));
                        }
                    } catch (Exception e) {
                        // ignore
                    } finally {
                        if (file == null || !file.exists()) {
                            log.warn("Inventory Process Result file({}) does not exists.", file == null ? "N/A" : file.getAbsolutePath());
                            throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE, "The file does not exist.");
                        }
                    }

                    fileName = reportService.getFileName(inventoryTypeCode, inventoryIds.get(0), patterns, fileType, inventoryProcessResult.getInventoryProcessStartDatetime());
                    String encodedFileName = FileUtil.getEncodeFileName(fileName);

                    headers.setContentLength(file.length());
                    headers.set(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);
                    log.debug("inventoryTypeCode service 외 : header content-disposition: {}", headers.get(HttpHeaders.CONTENT_DISPOSITION));

                    resource = new InputStreamResource(new FileInputStream(file));
                }
            } else {
                // 여러 inventory 선택 시 zip 으로 묶어 내려준다.
                bais = reportService.getCompressed(projectId, inventoryTypeCode, serviceId, serverInventoryId, inventoryIds, patterns, fileType);

                fileName = projectService.getProjectName(projectId) + "_" + Domain1001.valueOf(inventoryTypeCode).fullname() + "_assessment_" + DATE_FORMAT.format(new Date()) + ".zip";
                String encodedFileName = FileUtil.getEncodeFileName(fileName);

                headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(MEDIA_TYPE_ZIP));
                headers.setContentLength(bais.available());
                headers.set(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);
                log.debug("id not exist : header content-disposition: {}", headers.get(HttpHeaders.CONTENT_DISPOSITION));

                resource = new InputStreamResource(bais);
            }
        } finally {
            if (bais != null) {
                bais.close();
            }
        }

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @Operation(summary = "레포트 파일명 조회", description = "레포트 파일명을 조회한다.\n" +
            "- inventoryTypeCode (필수) : SERV / SVR / MW / APP / DBMS 중 선택\n" +
            "- inventoryId (필수) : 서비스, 서버, 미들웨어, 애플리케이션, 데이터베이스의 인벤토리 ID\n" +
            "- inventoryProcessId (옵션) : 레포트 대상 Inventory Process ID\n" +
            "- fileType (필수) : EXCEL / JSON 중 선택")
    @ApiResponse(responseCode = "200")
    @GetMapping("/filename")
    public ResponseEntity<?> getFilename(@PathVariable Long projectId, @RequestParam String inventoryTypeCode,
                                         @RequestParam Long inventoryId, @RequestParam(required = false) Long inventoryProcessId,
                                         @RequestParam String fileType) {
        List<Pattern> patterns = getPatterns(projectId, inventoryTypeCode);

        log.debug("InventoryTypeCode : [{}], Inventory ID : [{}], InventoryProcess ID : [{}], File Type : [{}], patterns : [{}]"
                , inventoryTypeCode, inventoryId, inventoryProcessId, fileType, patterns);

        InventoryProcessResponse inventoryProcess = null;
        String fileName;

        if (!CommonConstants.SERVICE_TYPE_CODE.equals(inventoryTypeCode)) {
            if (inventoryProcessId != null) {
                inventoryProcess = reportService.getInventoryProcess(inventoryTypeCode, inventoryId, inventoryProcessId);
            } else {
                inventoryProcess = reportService.getInventoryProcess(inventoryTypeCode, inventoryId);
            }
        }

        if (inventoryProcess != null) {
            fileName = reportService.getFileName(inventoryTypeCode, inventoryId, patterns, fileType, inventoryProcess.getInventoryProcessStartDatetime());
        } else {
            fileName = reportService.getFileName(inventoryTypeCode, inventoryId, patterns, fileType, new Date());
        }

        Map<String, String> fileMap = new HashMap<>();
        fileMap.put("fileName", fileName);

        return ResponseEntity.ok(fileMap);
    }

    /**
     * 패턴 정보 조회
     * Gets Patterns
     */
    private List<Pattern> getPatterns(Long projectId, String inventoryTypeCode) {
        SettingType type = null;

        if (CommonConstants.SERVICE_TYPE_CODE.equals(inventoryTypeCode)) {
            type = SettingType.SERVICE_REPORT_PATTERN;
        } else if (Domain1001.SVR.name().equals(inventoryTypeCode)) {
            type = SettingType.SERVER_REPORT_PATTERN;
        } else if (Domain1001.MW.name().equals(inventoryTypeCode)) {
            type = SettingType.MIDDLEWARE_REPORT_PATTERN;
        } else if (Domain1001.APP.name().equals(inventoryTypeCode)) {
            type = SettingType.APPLICATION_REPORT_PATTERN;
        } else if (Domain1001.DBMS.name().equals(inventoryTypeCode)) {
            type = SettingType.DATABASE_REPORT_PATTERN;
        }

        String value = userService.getUserConfig(projectId, WebUtil.getUserId(), type);

        List<Pattern> patterns = new ArrayList<>();

        if (StringUtils.isNotEmpty(value)) {
            String[] values = value.split(",");

            for (String v : values) {
                Pattern p = Pattern.valueOf(v);
                patterns.add(p);
            }
        }

        // pattern이 설정 되어있지 않은 경우, default값을 설정해준다.
        if (patterns.size() == 0) {
            patterns = reportService.getDefaultPatterns(patterns);
        }

        return patterns;
    }

    @Operation(summary = "Windows middleware 엑셀 다운로드", description = "Windows Middleware 엑셀 댜운로드 (비공식 기능)")
    @GetMapping("/windows/middleware")
    public ResponseEntity<InputStreamResource> windowsMiddlewareDownload(@PathVariable Long projectId) {
        ByteArrayInputStream in = reportService.generateWindowsMiddlewareExcel(projectId);

        String filename = "Windows_Middleware_" + DATE_FORMAT.format(new Date()) + "." + EXCEL_EXTENSION_XLSX;
        String encodedFileName = FileUtil.getEncodeFileName(projectService.getProjectName(projectId) + "_" + filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(CommonConstants.MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "사용자 정의 레포트 시트 목록 조회", description = "사용자 정의 레포트의 시트 목록을 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/custom-report/sheet")
    public List<ExcelSheetDto> getCustomExcelSheetList(@PathVariable Long projectId, @RequestParam Long inventoryId, @RequestParam String inventoryTypeCode, @RequestParam() Long inventoryProcessId) {
        return reportService.getCustomExcelSheetList(inventoryTypeCode, inventoryId, inventoryProcessId);
    }

    @Operation(summary = "사용자 정의 레포트 시트 제외", description = "사용자 정의 레포트에서 제외될 시트를 저장한다")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/custom-report/sheet")
    public void excludeCustomExcelSheet(@PathVariable Long projectId, @RequestParam Long inventoryId, @RequestBody List<String> sheetNames) {
        reportService.excludeCustomExcelSheet(inventoryId, sheetNames);
    }

    @Operation(summary = "사용자 정의 레포트 다운로드", description = "사용자 정의 레포트를 다운로드한다.")
    @GetMapping("/custom-report")
    public ResponseEntity<InputStreamResource> getCustomExcelDownload(@PathVariable Long projectId, @RequestParam Long inventoryId, @RequestParam String inventoryTypeCode, @RequestParam() Long inventoryProcessId) {
        ByteArrayInputStream in = reportService.getCustomExcelStream(inventoryTypeCode, inventoryId, inventoryProcessId);

        List<Pattern> patterns = getPatterns(projectId, inventoryTypeCode);
        String filename = reportService.getFileName(inventoryTypeCode, inventoryId, patterns, EXCEL_FILE_TYPE, new Date());
        String encodedFileName = FileUtil.getEncodeFileName(filename);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(CommonConstants.MEDIA_TYPE_EXCEL));
        headers.setContentLength(in.available());
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(in), headers, HttpStatus.OK);
    }

}
//end of ReportController.java
