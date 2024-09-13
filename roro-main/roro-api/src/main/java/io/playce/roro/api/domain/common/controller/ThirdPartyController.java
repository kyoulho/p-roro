package io.playce.roro.api.domain.common.controller;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.domain.common.service.ThirdPartyService;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionListResponse;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionRequest;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static io.playce.roro.api.common.CommonConstants.THIRD_PARTY_EXCEL_REPORT_NAME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/api/common", produces = APPLICATION_JSON_VALUE)
public class ThirdPartyController {

    private final ThirdPartyService thirdPartyService;

    @Operation(summary = "Third Party 목록 조회", description = "Third Party 목록을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/third-parties")
    public ResponseEntity<?> getThirdPartySolutions() {
        List<ThirdPartySolutionListResponse> thirdPartySolutions = thirdPartyService.getThirdPartySolutions();

        return ResponseEntity.ok(thirdPartySolutions);
    }

    @Operation(summary = "Third Party 상세 조회", description = "Third Party 상세 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/third-parties/{thirdPartySolutionId}")
    public ResponseEntity<?> getThirdPartySolution(@PathVariable Long thirdPartySolutionId) {
        ThirdPartySolutionResponse thirdPartySolutionResponse =
                thirdPartyService.getThirdPartySolution(thirdPartySolutionId);

        return ResponseEntity.ok(thirdPartySolutionResponse);
    }

    @Operation(summary = "Third Party 등록", description = "Third Party를 등록한다.")
    @ApiResponse(responseCode = "201")
    @PostMapping(value = "/third-party")
    public ResponseEntity<?> createThirdPartySolution(@RequestBody ThirdPartySolutionRequest thirdPartySolutionRequest) {
        Map<String, Object> createThirdPartyMap = thirdPartyService.createThirdPartySolution(thirdPartySolutionRequest);

        return new ResponseEntity<>(createThirdPartyMap, HttpStatus.CREATED);
    }

    @Operation(summary = "Third Party 수정", description = "Third Party를 수정한다.")
    @ApiResponse(responseCode = "204")
    @PutMapping(value = "/third-parties/{thirdPartySolutionId}")
    public ResponseEntity<?> modifyThirdPartySolution(@PathVariable Long thirdPartySolutionId,
                                                      @RequestBody ThirdPartySolutionRequest thirdPartySolutionRequest) {
        Map<String, Object> modifyThirdPartyMap = thirdPartyService.modifyThirdPartySolution(thirdPartySolutionId, thirdPartySolutionRequest);

        return new ResponseEntity<>(modifyThirdPartyMap, HttpStatus.OK);
    }

    @Operation(summary = "Third Party 삭제", description = "Third Party를 삭제한다.")
    @ApiResponse(responseCode = "204")
    @DeleteMapping(value = "/third-parties/{thirdPartySolutionId}")
    public ResponseEntity<?> removeThirdPartySolution(@PathVariable Long thirdPartySolutionId) {
        thirdPartyService.removeThirdPartySolution(thirdPartySolutionId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Third Party 목록 excel export", description = "Third Party 목록을 excel로 export한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/third-parties/excel")
    public ResponseEntity<?> excelExport() {
        ByteArrayOutputStream out = thirdPartyService.excelExport();

        String filename = ExcelUtil.generateExcelFileName("Settings" + "_" + THIRD_PARTY_EXCEL_REPORT_NAME);
        String encodedFileName = FileUtil.getEncodeFileName(filename);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(CommonConstants.MEDIA_TYPE_EXCEL));
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + encodedFileName + ";filename*=UTF-8''" + encodedFileName);

        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())), responseHeaders, HttpStatus.OK);
    }
}
