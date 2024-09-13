package io.playce.roro.api.domain.common.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionRequest;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionRequest.ThirdPartySearchTypeRequest;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ThirdPartyControllerTest {

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
    void 서드파티목록조회() throws Exception {
        mockMvc.perform(get("/api/common/third-parties")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void 서드파티상세조회() throws Exception {
        mockMvc.perform(get("/api/common/third-parties/{thirdPartySolutionId}", 11)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    @Transactional
    void 서드파티등록() throws Exception {

        ThirdPartySolutionRequest thirdPartySolutionRequest = new ThirdPartySolutionRequest();
        thirdPartySolutionRequest.setThirdPartySolutionName("JEUS");
        thirdPartySolutionRequest.setVendor("Apache");
        thirdPartySolutionRequest.setDescription("WAS");

        List<ThirdPartySearchTypeRequest> thirdPartySearchTypes = new ArrayList<>();

//        ThirdPartySearchTypeRequest thirdPartySearchTypeRequest = new ThirdPartySearchTypeRequest();
//        thirdPartySearchTypeRequest.setSearchType("CMD");
//        thirdPartySearchTypeRequest.setSearchValue("sqlplus");
//        thirdPartySearchTypeRequest.setWindowsYn("N");

        ThirdPartySearchTypeRequest thirdPartySearchTypeRequest1 = new ThirdPartySearchTypeRequest();
        thirdPartySearchTypeRequest1.setSearchType("RUNUSER");
        thirdPartySearchTypeRequest1.setSearchValue("jeus");
        // thirdPartySearchTypeRequest1.setSearchValue("C:\\TmaxSoft\\JEUS7\\lib\\system\\jeusutil.jar");
        // thirdPartySearchTypeRequest1.setInventoryTypeCode("SVR");
        // thirdPartySearchTypeRequest1.setWindowsYn("Y");

//        thirdPartySearchTypes.add(thirdPartySearchTypeRequest);
        thirdPartySearchTypes.add(thirdPartySearchTypeRequest1);

        thirdPartySolutionRequest.setThirdPartySearchTypes(thirdPartySearchTypes);

        System.out.println(objectMapper.writeValueAsString(thirdPartySolutionRequest));

        mockMvc.perform(post("/api/common/third-party")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(thirdPartySolutionRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
        ;

    }

    @Test
    @Transactional
    void 서드파티수정() throws Exception {

        ThirdPartySolutionRequest thirdPartySolutionRequest = new ThirdPartySolutionRequest();
        thirdPartySolutionRequest.setThirdPartySolutionName("testtesttest");
        thirdPartySolutionRequest.setVendor("Apache Foundation1");
        thirdPartySolutionRequest.setDescription("Middleware1");

        List<ThirdPartySearchTypeRequest> thirdPartySearchTypes = new ArrayList<>();

        ThirdPartySearchTypeRequest thirdPartySearchTypeRequest = new ThirdPartySearchTypeRequest();
        thirdPartySearchTypeRequest.setSearchType("PROCESS");
        thirdPartySearchTypeRequest.setSearchValue("tomcat81");

        ThirdPartySearchTypeRequest thirdPartySearchTypeRequest1 = new ThirdPartySearchTypeRequest();
        thirdPartySearchTypeRequest1.setSearchType("RUNUSER");
        thirdPartySearchTypeRequest1.setSearchValue("apache");

        ThirdPartySearchTypeRequest thirdPartySearchTypeRequest2 = new ThirdPartySearchTypeRequest();
        thirdPartySearchTypeRequest2.setSearchType("RUNUSER");
        thirdPartySearchTypeRequest2.setSearchValue("tomcat");

        thirdPartySearchTypes.add(thirdPartySearchTypeRequest);
        thirdPartySearchTypes.add(thirdPartySearchTypeRequest1);
        thirdPartySearchTypes.add(thirdPartySearchTypeRequest2);

        thirdPartySolutionRequest.setThirdPartySearchTypes(thirdPartySearchTypes);

        System.out.println(objectMapper.writeValueAsString(thirdPartySolutionRequest));

        mockMvc.perform(put("/api/common/third-parties/{thirdPartySolutionId}", 1)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(thirdPartySolutionRequest)))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
        ;

    }

    @Test
    @Transactional
    void 서드파티삭제() throws Exception {

        mockMvc.perform(delete("/api/common/third-parties/{thirdPartySolutionId}", 1)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
        ;

    }
}