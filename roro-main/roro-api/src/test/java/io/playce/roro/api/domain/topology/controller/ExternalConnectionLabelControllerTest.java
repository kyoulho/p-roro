/*
 * Copyright 2023 The playce-roro-v3 Project.
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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Jihyun Park      6월 15, 2023            First Draft.
 */
package io.playce.roro.api.domain.topology.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.api.domain.topology.service.ExternalConnectionLabelService;
import io.playce.roro.common.dto.topology.ExternalConnectionLabelRequest;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Jihyun Park
 * @version 1.0
 */
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExternalConnectionLabelControllerTest {

    @Autowired
    public WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExternalConnectionLabelService externalConnectionLabelService;

    final String accessToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJSb1JvIFVzZXIgSW5mby4iLCJpc3MiOiJodHRwczovL3d3dy5wbGF5LWNlLmlvIiwidXNlciI6eyJ1c2VySWQiOjEsInVzZXJMb2dpbklkIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImFkbWluIiwidXNlck5hbWVLb3JlYW4iOiLqtIDrpqzsnpAiLCJ1c2VyTmFtZUVuZ2xpc2giOiJBZG1pbiIsInVzZXJFbWFpbCI6ImFkbWluQG9zY2kua3IifSwicm9sZXMiOlsiUk9MRV9BRE1JTiJdLCJpYXQiOjE2NzMyMjYyODEsImV4cCI6MTc2NzgzNDI4MX0.lT6znRkSfQKkH4MDmr_TEfQ_2ZNFKCAzTEuy7gfaujoL0XIs1bV5zAjZLmn5RnCxrKSlWrG7nyNnHfkiZUpmBQ";

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity())  // Security 사용 시 등록
                .build();
    }

    @DisplayName("Application 하드코딩된 IP 라벨 저장 테스트")
    @Test
    void saveExternalConnectionLabelTest() throws Exception {
        Long projectId = 1L;

        ExternalConnectionLabelRequest request = new ExternalConnectionLabelRequest();
        request.setProjectId(projectId);
        request.setIp("2.3.3.1");
        request.setLabel("사내 인트라넷");

        doNothing().when(externalConnectionLabelService).saveExternalConnectionLabel(any(ExternalConnectionLabelRequest.class));

        mockMvc.perform(post("/api/projects/{projectId}/topology/external-connection-label", projectId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("Application 하드코딩된 IP 라벨 삭제 테스트")
    @Test
    void deleteExternalConnectionLabelTest() throws Exception{
        Long projectId = 1L;
        String ip = "2.3.3.1";

        doNothing().when(externalConnectionLabelService).deleteExternalConnectionLabel(anyLong(), anyString());

        mockMvc.perform(delete("/api/projects/{projectId}/topology/external-connection-label/{ip}", projectId, ip)
                        .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

}
