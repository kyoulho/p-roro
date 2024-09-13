package io.playce.roro.api.domain.inventory.controller;

import io.playce.roro.api.domain.inventory.service.ServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/projects/inventory", produces = APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MonitoringController {

    private final ServerService serverService;

    @Operation(summary = "서버 모니터링 전체 중지", description = "서버 모니터링을 중지한다.")
    @PutMapping(path = "/servers/monitoring-stop")
    public ResponseEntity<?> serverMonitoringStop(@RequestParam(name = "projectIds") String projectIds) {
        serverService.stopMonitoringProject(projectIds);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
