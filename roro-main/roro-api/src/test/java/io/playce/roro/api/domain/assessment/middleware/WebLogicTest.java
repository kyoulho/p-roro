/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       1월 10, 2022            First Draft.
 */
package io.playce.roro.api.domain.assessment.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.WindowsInfoStrategy;
import io.playce.roro.mw.asmt.weblogic.WebLogicAssessment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebLogicTest {

    // 테스트 할때 roro-main에 아래 dependency 추가 후 테스트
    // <dependency>
    //    <groupId>io.playce.roro</groupId>
    //    <artifactId>roro-mw-asmt-weblogic</artifactId>
    //    <version>3.0.0</version>
    //    <scope>test</scope>
    // </dependency>

    @Autowired
    private WebLogicAssessment WEBLOGICAssessment;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void 웹로직_분석_테스트() throws Exception {
        TargetHost targetHost = new TargetHost();
        targetHost.setIpAddress("192.168.1.95");
        targetHost.setPort(5985);
        targetHost.setUsername("Administrator");
        targetHost.setPassword("P@ssw0rd");

        MiddlewareInventory middleware = new MiddlewareInventory();
        middleware.setDomainHomePath("C:\\Oracle\\MIDDLE~1\\ORACLE~1\\user_projects\\domains\\base_domain");
        middleware.setInventoryName("AdminServer");
        middleware.setInventoryDetailTypeCode("WEBLOGIC");
        middleware.setEngineInstallationPath("C:\\Oracle\\MIDDLE~1\\ORACLE~1\\wlserver\\server");

        GetInfoStrategy strategy = new WindowsInfoStrategy(true);

        MiddlewareAssessmentResult result = WEBLOGICAssessment.assessment(targetHost, middleware, strategy);

        System.out.println(objectMapper.writeValueAsString(result));
    }

}