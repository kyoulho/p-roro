package io.playce.roro.api.domain.common.controller;

import io.playce.roro.api.domain.common.service.SettingService;
import io.playce.roro.common.dto.common.setting.SettingRequest;
import io.playce.roro.common.dto.common.setting.SettingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/api/common", produces = APPLICATION_JSON_VALUE)
public class SettingController {

    private final SettingService settingService;

    @Operation(summary = "글로벌 환경 세팅 조회", description = "글로벌 환경 세팅을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping(value = "/settings")
    public ResponseEntity<?> getSettings() {
        List<SettingResponse> settingResponses = settingService.getSettings();

        return ResponseEntity.ok(settingResponses);
    }

    @Operation(summary = "글로벌 환경 세팅 수정", description = "글로벌 환경 세팅을 수정한다.")
    @ApiResponse(responseCode = "200")
    @PutMapping(value = "/settings")
    public ResponseEntity<?> settings(@RequestBody List<SettingRequest> settingRequests) {

        settingService.modifySettingPropertyValue(settingRequests);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
