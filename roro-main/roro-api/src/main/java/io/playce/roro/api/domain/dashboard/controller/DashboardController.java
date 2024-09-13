package io.playce.roro.api.domain.dashboard.controller;

import io.playce.roro.api.domain.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/projects/{projectId}")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Dashboard 및 Widget 설정 조회", description = "Dashboard 및 Widget 설정을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/dashboard")
    public ResponseEntity<?> getConfigContent(@PathVariable Long projectId) {
        String configContents = dashboardService.getDashboard(projectId);

        return new ResponseEntity<>(configContents, HttpStatus.OK);
    }

    @Operation(summary = "Dashboard 및 Widget 등록 및 수정", description = "Dashboard 및 Widget 설정을 등록 및 수정을 한다.")
    @ApiResponse(responseCode = "201")
    @PostMapping("/dashboard")
    public ResponseEntity<?> createConfigContents(@PathVariable Long projectId,
                                                  @RequestBody String configContents) {
        dashboardService.createConfigContents(projectId, configContents);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
