package io.playce.roro.api.domain.assessment.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.UnixLikeInfoStrategy;
import io.playce.roro.mw.asmt.websphere.WebSphereAssessment;
import io.playce.roro.mw.asmt.websphere.WebSpherePostProcessor;
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
class WebSphereTest {

    @Autowired
    public WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebSphereAssessment WSPHEREAssessment;

    @Autowired
    private WebSpherePostProcessor WSPHEREPostProcessor;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity())  // Security 사용 시 등록
                .build();
    }

    final String accessToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJSb1JvIFVzZXIgSW5mby4iLCJpc3MiOiJodHRwczovL3d3dy5wbGF5LWNlLmlvIiwidXNlciI6eyJ1c2VySWQiOjEsInVzZXJMb2dpbklkIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImFkbWluIiwidXNlck5hbWVLb3JlYW4iOiLqtIDrpqzsnpAiLCJ1c2VyTmFtZUVuZ2xpc2giOiJBZG1pbiIsInVzZXJFbWFpbCI6ImFkbWluQG9zY2kua3IifSwicm9sZXMiOlsiUk9MRV9BRE1JTiJdLCJpYXQiOjE2NzMyMjYyODEsImV4cCI6MTc2NzgzNDI4MX0.lT6znRkSfQKkH4MDmr_TEfQ_2ZNFKCAzTEuy7gfaujoL0XIs1bV5zAjZLmn5RnCxrKSlWrG7nyNnHfkiZUpmBQ";

    @Test
    void 웹스피어_분석테스트() throws Exception {

        TargetHost targetHost = new TargetHost();
        targetHost.setIpAddress("192.168.4.51");
        targetHost.setPort(22);
        targetHost.setUsername("roro");
        targetHost.setPassword("jan01jan");

        MiddlewareInventory middleware = new MiddlewareInventory();
        middleware.setDomainHomePath("");
        middleware.setInventoryName("AdminServer");
        middleware.setInventoryDetailTypeCode("WSPHERE");
        middleware.setEngineInstallationPath("/opt/IBM/WebSphere/AppServer_1");

        GetInfoStrategy strategy = new UnixLikeInfoStrategy(false);
        MiddlewareAssessmentResult result = WSPHEREAssessment.assessment(targetHost, middleware, strategy);

        System.out.println(objectMapper.writeValueAsString(result));
    }

}
