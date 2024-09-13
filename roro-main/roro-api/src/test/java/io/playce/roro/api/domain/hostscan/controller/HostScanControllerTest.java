package io.playce.roro.api.domain.hostscan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.dto.hostscan.DiscoveredHostDto;
import io.playce.roro.common.dto.hostscan.HostScanRequest;
import io.playce.roro.jpa.entity.DiscoveredHost;
import io.playce.roro.jpa.entity.HostScanHistory;
import io.playce.roro.jpa.repository.DiscoveredHostRepository;
import io.playce.roro.jpa.repository.HostScanHistoryRepository;
import io.playce.roro.mybatis.domain.hostscan.HostScanMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static io.playce.roro.api.common.CommonConstants.MEDIA_TYPE_EXCEL;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(
        properties = {"spring.datasource.url=jdbc:log4jdbc:mariadb://localhost:3306/roro"}
        , webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
class HostScanControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private HostScanHistoryRepository hostScanHistoryRepository;
    @MockBean
    private DiscoveredHostRepository discoveredHostRepository;

    @MockBean
    private HostScanMapper hostScanMapper;

    final String accessToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJSb1JvIFVzZXIgSW5mby4iLCJpc3MiOiJodHRwczovL3d3dy5wbGF5LWNlLmlvIiwidXNlciI6eyJ1c2VySWQiOjEsInVzZXJMb2dpbklkIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImFkbWluIiwidXNlck5hbWVLb3JlYW4iOiLqtIDrpqzsnpAiLCJ1c2VyTmFtZUVuZ2xpc2giOiJBZG1pbiIsInVzZXJFbWFpbCI6ImFkbWluQG9zY2kua3IifSwicm9sZXMiOlsiUk9MRV9BRE1JTiJdLCJpYXQiOjE2NzMyMjYyODEsImV4cCI6MTc2NzgzNDI4MX0.lT6znRkSfQKkH4MDmr_TEfQ_2ZNFKCAzTEuy7gfaujoL0XIs1bV5zAjZLmn5RnCxrKSlWrG7nyNnHfkiZUpmBQ";

    @Test
    @DisplayName("Bad Request 에러 테스트")
    void hostScanWithBadRequest1() throws Exception {
        String cidr = "127.0.0.1/15";
        HostScanRequest hostScanRequest = new HostScanRequest();
        hostScanRequest.setCidr(cidr);
        mockMvc.perform(post("/api/projects/{projectId}/host-scan", 1)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(hostScanRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Not Found 에러 테스트")
    void hostScanWithBadRequest2() throws Exception {
        //given
        Long projectId = 1L;
        Long scanHistoryId = 1L;
        given(hostScanHistoryRepository.findByProjectIdAndScanHistoryId(projectId, scanHistoryId))
                .willReturn(Optional.empty());

        mockMvc.perform(get("/api/projects/{projectId}/host-scan/{scanHistoryId}", projectId, scanHistoryId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("스캔 요청시 응답 테스트")
    void hostScan() throws Exception {
        // given
        String cidr = "127.0.0.1/32";
        Long projectId = 1L;
        HostScanHistory hostScanHistory = HostScanHistory.builder()
                .projectId(projectId)
                .cidr(cidr)
                .scanStartDateTime(new Date())
                .build();

        doReturn(hostScanHistory)
                .when(hostScanHistoryRepository)
                .save(any(HostScanHistory.class));

        doReturn(Optional.of(hostScanHistory))
                .when(hostScanHistoryRepository)
                .findByProjectIdAndScanHistoryId(anyLong(), any());

        doReturn(DiscoveredHost.builder().build())
                .when(discoveredHostRepository)
                .save(any(DiscoveredHost.class));

        // when
        HostScanRequest hostScanRequest = new HostScanRequest();
        hostScanRequest.setCidr(cidr);


        mockMvc.perform(post("/api/projects/{projectId}/host-scan", projectId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(hostScanRequest)))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("발견된 호스트 조회 테스트")
    void discoveredHosts() throws Exception {
        // given
        String ip = "127.0.0.1";
        String subnet = "32";
        String cidr = ip + "/" + subnet;
        String osName = "linux";
        Long projectId = 1L;
        Long scanHistoryId = 1L;
        // 스캔 내역 세팅
        HostScanHistory hostScanHistory = HostScanHistory.builder()
                .projectId(projectId)
                .cidr(cidr)
                .build();
        given(hostScanHistoryRepository.save(any()))
                .willReturn(hostScanHistory);
        // 스캔 결과 세팅
        List<DiscoveredHostDto> hosts = new ArrayList<>();
        DiscoveredHostDto host = DiscoveredHostDto.builder().ipAddress(ip).osName(osName).build();
        hosts.add(host);

        given(hostScanHistoryRepository.findByProjectIdAndScanHistoryId(projectId, scanHistoryId))
                .willReturn(Optional.of(hostScanHistory));
        given(hostScanMapper.selectDiscoveredHostAndRegisteredServers(projectId, scanHistoryId))
                .willReturn(hosts);

        // when
        mockMvc.perform(get("/api/projects/{projectId}/host-scan/{scanHistoryId}", projectId, scanHistoryId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].ipAddress", is(ip)))
                .andExpect(jsonPath("$.[0].osName", is(osName)));
    }

    @Test
    @DisplayName("발견된 호스트 엑셀파일 테스트")
    void discoveredHostsToExcel() throws Exception {
        // given
        String ip = "127.0.0.1";
        String subnet = "32";
        String cidr = ip + "/" + subnet;
        String osName = "linux";
        Long projectId = 1L;
        Long scanHistoryId = 1L;
        // 스캔 내역 세팅
        HostScanHistory hostScanHistory = HostScanHistory.builder()
                .projectId(projectId)
                .cidr(cidr)
                .build();
        given(hostScanHistoryRepository.save(any()))
                .willReturn(hostScanHistory);
        // 스캔 결과 세팅
        List<DiscoveredHostDto> hosts = new ArrayList<>();
        DiscoveredHostDto host = DiscoveredHostDto.builder().ipAddress(ip).osName(osName).build();
        hosts.add(host);

        given(hostScanHistoryRepository.findByProjectIdAndScanHistoryId(projectId, scanHistoryId))
                .willReturn(Optional.of(hostScanHistory));
        given(hostScanMapper.selectDiscoveredHostAndRegisteredServers(projectId, scanHistoryId))
                .willReturn(hosts);

        // when
        mockMvc.perform(get("/api/projects/{projectId}/host-scan/{scanHistoryId}/excel", projectId, scanHistoryId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken))
                //then
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType(MEDIA_TYPE_EXCEL)));
    }
}