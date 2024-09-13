package io.playce.roro.api.domain.common.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.app.asmt.ApplicationScanConfig;
import io.playce.roro.common.dto.common.setting.SettingRequest;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.setting.SettingsHandler;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SettingControllerTest {

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
    void 세팅조회() throws Exception {
        mockMvc.perform(get("/api/common/settings")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    @Transactional
    void 세팅수정() throws Exception {
        List<SettingRequest> settingRequests = new ArrayList<>();

        SettingRequest settingRequest1 = new SettingRequest();
        settingRequest1.setSettingId(11L);
        settingRequest1.setPropertyValue("true");

//        SettingRequest settingRequest2 = new SettingRequest();
//        settingRequest2.setSettingId(4L);
//        settingRequest2.setPropertyValue("22");
//
//        SettingRequest settingRequest8 = new SettingRequest();
//        settingRequest8.setSettingId(8L);
//        settingRequest8.setPropertyValue(GeneralCipherUtil.encrypt("roro"));


        settingRequests.add(settingRequest1);
//        settingRequests.add(settingRequest2);
//        settingRequests.add(settingRequest8);

        System.out.println(objectMapper.writeValueAsString(settingRequests));

        mockMvc.perform(put("/api/common/settings")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(settingRequests)))
                .andDo(print())
                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    @Transactional
    void 세팅변경확인() throws Exception {
        System.out.println("before : " + SettingsHandler.getSettingsValue("roro.ssh.port"));

        List<SettingRequest> settingRequests = new ArrayList<>();

        SettingRequest settingRequest8 = new SettingRequest();
        settingRequest8.setSettingId(6L);
        settingRequest8.setPropertyValue("44");

        settingRequests.add(settingRequest8);

        System.out.println(objectMapper.writeValueAsString(settingRequests));

        mockMvc.perform(put("/api/common/settings")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(settingRequests)))
                .andDo(print())
                .andExpect(status().isOk());


        System.out.println("after : " + SettingsHandler.getSettingsValue("roro.ssh.port"));

    }


}