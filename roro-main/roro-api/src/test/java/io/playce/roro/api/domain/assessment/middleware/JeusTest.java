package io.playce.roro.api.domain.assessment.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.jeus.JeusAssessment;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.WindowsInfoStrategy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JeusTest {

    @Autowired
    public WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JeusAssessment JEUSAssessment;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity())  // Security 사용 시 등록
                .build();
    }

    final String accessToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJSb1JvIFVzZXIgSW5mby4iLCJpc3MiOiJodHRwczovL3d3dy5wbGF5LWNlLmlvIiwidXNlciI6eyJ1c2VySWQiOjEsInVzZXJMb2dpbklkIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImFkbWluIiwidXNlck5hbWVLb3JlYW4iOiLqtIDrpqzsnpAiLCJ1c2VyTmFtZUVuZ2xpc2giOiJBZG1pbiIsInVzZXJFbWFpbCI6ImFkbWluQG9zY2kua3IifSwicm9sZXMiOlsiUk9MRV9BRE1JTiJdLCJpYXQiOjE2NzMyMjYyODEsImV4cCI6MTc2NzgzNDI4MX0.lT6znRkSfQKkH4MDmr_TEfQ_2ZNFKCAzTEuy7gfaujoL0XIs1bV5zAjZLmn5RnCxrKSlWrG7nyNnHfkiZUpmBQ";

    @Test
    void 제우스_분석테스트() throws Exception {

        TargetHost targetHost = new TargetHost();
        targetHost.setIpAddress("192.168.1.157");
        targetHost.setPort(5985);
        targetHost.setUsername("Administrator");
        targetHost.setPassword("P@ssw0rd");

        MiddlewareInventory middleware = new MiddlewareInventory();
        middleware.setInventoryName("AdminServer");
        middleware.setInventoryDetailTypeCode("JEUS");
        middleware.setEngineInstallationPath("C:\\TmaxSoft\\JEUS7");
        middleware.setDomainHomePath("C:\\TmaxSoft\\JEUS7\\domains\\jeus_domain");

//        GetInfoStrategy strategy = new UnixLikeInfoStrategy(false);
        GetInfoStrategy strategy = new WindowsInfoStrategy(true);

        MiddlewareAssessmentResult result = JEUSAssessment.assessment(targetHost, middleware, strategy);

        System.out.println(objectMapper.writeValueAsString(result));

//        String temp1 = "{\"server-name\":\"DAS\"}";
//        String temp2 = "{\"server-name\":[\"DAS\",\"DAS11\"]}";
//
//
//        JSONObject jsonObject1 = new JSONObject(temp1);
//        JSONObject jsonObject2 = new JSONObject(temp2);
//
//        JSONArray serverNames1 = convertJSONArray(jsonObject1.get("server-name"));
//        JSONArray serverNames2 = convertJSONArray(jsonObject2.get("server-name"));
//
//        System.out.println(serverNames1);
//        System.out.println(serverNames2);


    }

    private JSONArray convertJSONArray(Object object) {
        JSONArray jsonArray = new JSONArray();
        if (object instanceof JSONArray) {
            jsonArray = (JSONArray) object;
        } else if (object instanceof JSONObject) {
            jsonArray.put(object);
        } else if (object instanceof String) {
            jsonArray.put(object);
        }
        return jsonArray;
    }



}
