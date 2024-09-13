package io.playce.roro.api.domain.migration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest
class MigrationControllerTest {

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
    void 서버상세_마이그레이션_목록_조회() throws Exception {

        mockMvc.perform(get("/api/projects/{projectId}/migrations", 1)
                        .param("inventoryId", "135")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void 마이그레이션_목록_조회() throws Exception {

        String startDate = "2022-02-28";
        String endDate = "2022-04-19";
        String targetPlatform = "ALL";
        String migrationStatus = "ALL";
        String pageNumber = "1";
        String pageSize = "25";
        String keyword = "";

        mockMvc.perform(get("/api/projects/{projectId}/migrations", 1)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .param("targetPlatform", targetPlatform)
                        .param("migrationStatus", migrationStatus)
                        .param("pageNumber", pageNumber)
                        .param("pageSize", pageSize)
                        .param("keyword", keyword)
                )
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void 마이그레이션_취소() throws Exception {
        mockMvc.perform(patch("/api/projects/{projectId}/migrations/{migrationId}", 1, 200)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
        ;
    }

    @Test
    void 마이그레이션_삭제() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}/migrations/{migrationId}", 1, 200)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
        ;
    }

    @Test
    void 마이그레이션_상세() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/migrations/{migrationId}", 1, 506)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void 마이그레이션_csv다운로드() throws Exception {
        String startDate = "2022-02-28";
        String endDate = "2022-03-29";
        String targetPlatform = "ALL";
        String migrationStatus = "ALL";
        String pageNumber = "1";
        String pageSize = "10";
        String keyword = "";

        mockMvc.perform(get("/api/projects/{projectId}/migrations/csv-download", 1)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .param("targetPlatform", targetPlatform)
                        .param("migrationStatus", migrationStatus)
                        .param("pageNumber", pageNumber)
                        .param("pageSize", pageSize)
                        .param("keyword", keyword)
                )
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv")))
        ;
    }

}

