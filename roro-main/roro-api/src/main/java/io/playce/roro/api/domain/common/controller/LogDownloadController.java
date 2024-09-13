package io.playce.roro.api.domain.common.controller;

import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.common.service.LogDownloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Date;

import static io.playce.roro.api.common.CommonConstants.RORO_LOG_NAME;
import static io.playce.roro.api.domain.inventory.controller.ReportController.DATE_FORMAT;

@RestController
@RequestMapping(value = "/api/common")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class LogDownloadController {

    private final LogDownloadService logDownloadService;

    @Operation(summary = "Log 파일 다운로드", description = "Log 파일을 다운로드 한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/log/download")
    public ResponseEntity<?> logDownload() throws IOException {
        ByteArrayInputStream bis = logDownloadService.getLogZipFile();

        String filename = RORO_LOG_NAME + "_" + DATE_FORMAT.format(new Date()) + ".zip";
        String encodedFileName = FileUtil.getEncodeFileName(filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE));
        responseHeaders.setContentLength(bis.available());
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(bis), responseHeaders, HttpStatus.OK);
    }

}
