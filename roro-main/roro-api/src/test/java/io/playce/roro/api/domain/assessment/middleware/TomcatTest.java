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
import io.playce.roro.api.domain.insights.service.InsightsService;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.util.SplitUtil;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.jpa.entity.MiddlewareMaster;
import io.playce.roro.jpa.repository.MiddlewareMasterRepository;
import io.playce.roro.mw.asmt.MiddlewareAssessment;
import io.playce.roro.mw.asmt.apache.ApacheAssessment;
import io.playce.roro.mw.asmt.apache.dto.ApacheAssessmentResult;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.jboss.JBossAssessment;
import io.playce.roro.mw.asmt.jboss.dto.JbossAssessmentResult;
import io.playce.roro.mw.asmt.jeus.JeusAssessment;
import io.playce.roro.mw.asmt.jeus.dto.JeusAssessmentResult;
import io.playce.roro.mw.asmt.nginx.NginxAssessment;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult;
import io.playce.roro.mw.asmt.tomcat.TomcatAssessment;
import io.playce.roro.mw.asmt.tomcat.dto.TomcatAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.UnixLikeInfoStrategy;
import io.playce.roro.mw.asmt.weblogic.WebLogicAssessment;
import io.playce.roro.mw.asmt.weblogic.dto.WebLogicAssessmentResult;
import io.playce.roro.mw.asmt.websphere.WebSphereAssessment;
import io.playce.roro.mw.asmt.websphere.dto.WebSphereAssessmentResult;
import io.playce.roro.mw.asmt.webtob.WebToBAssessment;
import io.playce.roro.mw.asmt.webtob.dto.WebToBAssessmentResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

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
@Slf4j
public class TomcatTest {

    @Autowired
    public WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TomcatAssessment TOMCATAssessment;

    @Autowired
    private MiddlewareMasterRepository middlewareMasterRepository;

    @Autowired
    private InsightsService insightsService;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity())  // Security 사용 시 등록
                .build();
    }

    @Test
    public void 톰캣_분석_테스트() throws InterruptedException {


        TargetHost targetHost = new TargetHost();
        targetHost.setIpAddress("192.168.4.77");
        targetHost.setPort(22);
        targetHost.setUsername("root");
        targetHost.setPassword("jan01jan");

        MiddlewareInventory middleware = new MiddlewareInventory();
        middleware.setDomainHomePath("/opt/playce/playce-roro");
        middleware.setInventoryName("AdminServer");
        middleware.setInventoryDetailTypeCode("WEBLOGIC");
        middleware.setEngineInstallationPath("/opt/playce/playce-roro");

        try {

            GetInfoStrategy strategy = new UnixLikeInfoStrategy(false);

            MiddlewareAssessmentResult result = TOMCATAssessment.assessment(targetHost, middleware, strategy);

            ((TomcatAssessmentResult.Engine) result.getEngine()).setVersion(null);

            String engineVersion = ((TomcatAssessmentResult.Engine) result.getEngine()).getVersion();
            String solutionName = ((TomcatAssessmentResult.Engine) result.getEngine()).getName();
            String javaVersion = ((TomcatAssessmentResult.Instance) result.getInstance()).getJavaVersion();
            String javaVendor = ((TomcatAssessmentResult.Instance) result.getInstance()).getJavaVendor();

            if (StringUtils.isNotEmpty(solutionName) && StringUtils.isNotEmpty(engineVersion)) {
                insightsService.createInventoryLifecycleVersionLink(19L, Domain1001.MW, solutionName, engineVersion, javaVendor, javaVersion);
            }


        } catch (Throwable e) {
            log.error("error", e);
        }


    }


    public static void main(String[] args) {
        String versionString1 = "";
        int dotCount = versionString1.length() - versionString1.replace(".", "").length();
        System.out.println(dotCount);

        if(dotCount == 3) {
            System.out.println(versionString1.substring(0, versionString1.lastIndexOf(".")));
        }


//        System.out.println(versionString1);
//        System.out.println(versionString1.replaceAll(".*?((?<!\\w)\\d+([.-]\\d+)*).*", "$1"));


    }

}