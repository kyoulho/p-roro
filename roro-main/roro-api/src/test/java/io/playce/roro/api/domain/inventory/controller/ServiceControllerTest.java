package io.playce.roro.api.domain.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.dto.inventory.service.ServiceCreateRequest;
import lombok.SneakyThrows;
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
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest
class ServiceControllerTest {

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

    @SneakyThrows
    @Test
    void 서비스생성() {

        ServiceCreateRequest serviceCreateRequest = new ServiceCreateRequest();

        serviceCreateRequest.setServiceName("TEST-Service");
        serviceCreateRequest.setBusinessCategoryCode("ABC");
        serviceCreateRequest.setBusinessCategoryName("ABC");
        serviceCreateRequest.setCustomerServiceCode("ABC");
        serviceCreateRequest.setCustomerServiceName("ABC-Service");
        serviceCreateRequest.setDescription("");
        serviceCreateRequest.setMigrationTargetYn("N");
        serviceCreateRequest.setLabelIds(null);

        System.out.println(objectMapper.writeValueAsString(objectMapper.writeValueAsString(serviceCreateRequest)));

        mockMvc.perform(post("/api/projects/{projectId}/inventory/services", 1)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(serviceCreateRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;

    }

    @SneakyThrows
    @Test
    void 서비스수정() {

        ServiceCreateRequest serviceCreateRequest = new ServiceCreateRequest();

        serviceCreateRequest.setServiceName("TEST-Service");
        serviceCreateRequest.setBusinessCategoryCode("ABC");
        serviceCreateRequest.setBusinessCategoryName("ABC");
        serviceCreateRequest.setCustomerServiceCode("test");
        serviceCreateRequest.setCustomerServiceName("ABC-Service");
        serviceCreateRequest.setDescription("");
        serviceCreateRequest.setMigrationTargetYn("N");
        serviceCreateRequest.setLabelIds(null);

        System.out.println(objectMapper.writeValueAsString(objectMapper.writeValueAsString(serviceCreateRequest)));

        mockMvc.perform(put("/api/projects/{projectId}/inventory/services/{serviceId}", 1,11)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(serviceCreateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;

    }

}
