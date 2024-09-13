package io.playce.roro.api.domain.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StatisticsControllerTest {

    @Autowired
    public WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity())  // Security 사용 시 등록
                .build();
    }

    final String accessToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJSb1JvIFVzZXIgSW5mby4iLCJpc3MiOiJodHRwczovL3d3dy5wbGF5LWNlLmlvIiwidXNlciI6eyJ1c2VySWQiOjEsInVzZXJMb2dpbklkIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImFkbWluIiwidXNlck5hbWVLb3JlYW4iOiLqtIDrpqzsnpAiLCJ1c2VyTmFtZUVuZ2xpc2giOiJBZG1pbiIsInVzZXJFbWFpbCI6ImFkbWluQG9zY2kua3IifSwicm9sZXMiOlsiUk9MRV9BRE1JTiJdLCJpYXQiOjE2NzMyMjYyODEsImV4cCI6MTc2NzgzNDI4MX0.lT6znRkSfQKkH4MDmr_TEfQ_2ZNFKCAzTEuy7gfaujoL0XIs1bV5zAjZLmn5RnCxrKSlWrG7nyNnHfkiZUpmBQ";

    @Test
    void getOverviewSummary() throws Exception {
        String serviceIds = "31,28,9,5,1";

        mockMvc.perform(get("/api/projects/{projectId}/statistics/summary", 1)
                        .param("serviceIds", serviceIds)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;

    }

    @Test
    void getServerCountPerService() throws Exception {
        String serviceIds = "31,28,9,5,1";
        String sortDirection = "asc";

        mockMvc.perform(get("/api/projects/{projectId}/statistics/server-count", 1)
                        .param("serviceIds", serviceIds)
                        .param("sortDirection", sortDirection)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;

    }

    @Test
    void getOsDistribution() throws Exception {
        String serviceIds = "1,5,9";
        String sortDirection = "desc";

        mockMvc.perform(get("/api/projects/{projectId}/statistics/{metric}/server-count", 1, "os")
                        .param("serviceIds", serviceIds)
                        .param("sortDirection", sortDirection)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;

    }

    @Test
    void getMiddlewareDistribution() throws Exception {
        String serviceIds = "1,5,9";
        String sortDirection = "desc";

        mockMvc.perform(get("/api/projects/{projectId}/statistics/{metric}/middleware-count", 1, "vendor")
                        .param("serviceIds", serviceIds)
                        .param("sortDirection", sortDirection)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void getApplicationDistribution() throws Exception {
        String serviceIds = "2,3,4";
        String sortDirection = "desc";

        mockMvc.perform(get("/api/projects/{projectId}/statistics/application-count", 2)
                        .param("serviceIds", serviceIds)
                        .param("sortDirection", sortDirection)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void getDatabaseDistribution() throws Exception {
        String serviceIds = "1,5,9";
        String sortDirection = "asc";

        mockMvc.perform(get("/api/projects/{projectId}/statistics/database-count", 1)
                        .param("serviceIds", serviceIds)
                        .param("sortDirection", sortDirection)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void getMonitoring() throws Exception {
        String serviceIds = "3,2,1";
        Long startDatetime = 1673755973L;
        Long endDatetime = 1673842373L;
        String measurementType = "avg";
        String limitType = "top";
        int limitCount = 10;

        mockMvc.perform(get("/api/projects/{projectId}/statistics/{metric}/monitoring", 1, "cpu-util")
                        .param("serviceIds", serviceIds)
                        .param("startDatetime", startDatetime + "")
                        .param("endDatetime", endDatetime + "")
                        .param("measurementType", measurementType)
                        .param("limitType", limitType)
                        .param("limitCount", limitCount + "")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void getServerUsage() throws Exception {
        String serviceIds = "3,2,1";
        Long startDatetime = 1673755973L;
        Long endDatetime = 1673842373L;
        String measurementType = "avg";
        String limitType = "top";
        int limitCount = 10;

        mockMvc.perform(get("/api/projects/{projectId}/statistics/{metric}/server-usage", 1, "disk-util")
                        .param("serviceIds", serviceIds)
                        .param("startDatetime", startDatetime + "")
                        .param("endDatetime", endDatetime + "")
                        .param("measurementType", measurementType)
                        .param("limitType", limitType)
                        .param("limitCount", limitCount + "")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;

    }
}