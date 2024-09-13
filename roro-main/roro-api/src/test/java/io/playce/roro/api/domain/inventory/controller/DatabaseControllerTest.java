package io.playce.roro.api.domain.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.dto.inventory.database.DatabaseRequest;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.common.util.JdbcURLParser;
import lombok.SneakyThrows;
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

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DatabaseControllerTest {

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
    void getDatabases() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/inventory/databases", 1)
//                        .param("serviceId", "1")
//                        .param("serverId", "43")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;

    }

    @Test
    void getDatabaseDetail() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/inventory/databases/{databaseInventoryId}", 1, 49)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void getDatabaseInstanceDetail() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/inventory/databases/{databaseInventoryId}/instances/{databaseInstanceId}", 1, 114, 96)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @SneakyThrows
    @Test
    void createDatabase() {

        List<Long> serviceIds = List.of(1L, 2L);
        List<Long> labelIds = List.of(1L, 2L);

        System.out.println(GeneralCipherUtil.encrypt("jan01jan"));

        DatabaseRequest databaseRequest = new DatabaseRequest();
//        databaseRequest.setDiscoveredInstanceId(1000L);
        databaseRequest.setDatabaseInventoryName("오라클서버테스트.");
        databaseRequest.setCustomerInventoryCode("오라");
        databaseRequest.setCustomerInventoryName("오라코드");
        databaseRequest.setServiceIds(serviceIds);
        databaseRequest.setServerInventoryId(2L);
        databaseRequest.setVendor("Oracle");
        databaseRequest.setInventoryDetailTypeCode("ORACLE");
        databaseRequest.setEngineVersion("11");
        databaseRequest.setConnectionPort(152122);
        databaseRequest.setDatabaseServiceName("OSCORA12");
        databaseRequest.setJdbcUrl("jdbc:oracle:thin:@192.168.0.155:1521:OSCORA12");
        databaseRequest.setAllScanYn("N");
        databaseRequest.setUserName("scott");
        databaseRequest.setPassword("jan01jan");
        databaseRequest.setDatabaseAccessControlSystemSolutionName("NA");
        databaseRequest.setLabelIds(labelIds);
        databaseRequest.setDescription("hahahahahhah");

        System.out.println(objectMapper.writeValueAsString(databaseRequest));

        mockMvc.perform(post("/api/projects/{projectId}/inventory/databases", 1)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(databaseRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;

    }

    @SneakyThrows
    @Test
    void modifyDatabase() {

        List<Long> serviceIds = List.of(1L);
        List<Long> labelIds = List.of(1L);

        DatabaseRequest databaseRequest = new DatabaseRequest();
        databaseRequest.setDatabaseInventoryName("테스트 서버.");
        databaseRequest.setCustomerInventoryCode("INV_TEST_CODE_1");
        databaseRequest.setCustomerInventoryName("인벤토리테스트코드");
        databaseRequest.setServiceIds(serviceIds);
        databaseRequest.setServerInventoryId(7L);
        databaseRequest.setVendor("Oracle");
        databaseRequest.setInventoryDetailTypeCode("MYSQL");
        databaseRequest.setEngineVersion("5.8");
        databaseRequest.setConnectionPort(3306);
        databaseRequest.setDatabaseServiceName("roro");
        databaseRequest.setJdbcUrl("jdbc:mysql://192.168.4.61:3306/roro");
        databaseRequest.setAllScanYn("Y");
        databaseRequest.setUserName("root");
        databaseRequest.setPassword("jan01jan");
        databaseRequest.setDatabaseAccessControlSystemSolutionName("NA");
        databaseRequest.setLabelIds(labelIds);
        databaseRequest.setDescription("hahahahahhah");

        System.out.println(objectMapper.writeValueAsString(databaseRequest));

        mockMvc.perform(put("/api/projects/{projectId}/inventory/databases/{databaseId}", 1, 22)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(databaseRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;

    }

    @Test
    void deleteDatabase() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}/inventory/databases/{databaseInventoryId}", 1, 49)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void deleteDatabaseInstance() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}/inventory/databases/{databaseInventoryId}/instances/{databaseInstanceId}", 1, 49, 59)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void 데이터베이스인스턴스_미들웨어() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/inventory/databases/{databaseInventoryId}/instances/{databaseInstanceId}/middlewares", 1, 21, 39)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;

    }

    @Test
    void 데이터베이스인스턴스_애플리케이션() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/inventory/databases/{databaseInventoryId}/instances/{databaseInstanceId}/applications", 1, 21, 39)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;

    }

    @Test
    void 사이베이스_URL() throws Exception {
        String jdbcUrl = "jdbc:sybase:tds:192.168.4.70:5002?ServiceName=master&aa=bb";
        System.out.println(JdbcURLParser.getJdbcProperty(jdbcUrl));

        String jdbcUrl1 = "jdbc:sybase:Tds:192.168.4.70?servicename=master";
        System.out.println(JdbcURLParser.getJdbcProperty(jdbcUrl1));

        String jdbcUrl2 = "jdbc:sybase:Tds:192.168.4.70";
        System.out.println(JdbcURLParser.getJdbcProperty(jdbcUrl2));

        String jdbcUrl3 = "jdbc:sybase:192.168.4.70:5002?ServiceName=master&aa=bb";
        System.out.println(JdbcURLParser.getJdbcProperty(jdbcUrl3));

    }
}