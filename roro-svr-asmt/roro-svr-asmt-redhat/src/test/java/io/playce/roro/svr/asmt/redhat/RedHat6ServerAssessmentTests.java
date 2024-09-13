/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Ji-Woong Choi     1월 11, 2022        First Draft.
 */
package io.playce.roro.svr.asmt.redhat;

import io.playce.roro.svr.asmt.dto.Server;
import io.playce.roro.svr.asmt.redhat.impl.RedHat6ServerAssessment;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = RedHat6ServerAssessment.class)
public class RedHat6ServerAssessmentTests {
    private static final Logger logger = LoggerFactory.getLogger(RedHat6ServerAssessmentTests.class);

    @Autowired
    private RedHat6ServerAssessment assessment;

    Server server = null;

    @Before
    public void init() {
        server = new Server();
        server.setIpAddress("192.168.0.52");
        server.setPassword("NoLcoCH/NjaS+Vi5cqH4+8e42og7YOkkLrCA6OU115vDccc/LOOoJqE8+ucT5K1MF3FVmLmaIamk3OtWnq6S8HwzFMGXtisXcOgJD0skKLoRUQRnSKnIsuD//6+3iEC2AOEJSny/6uvsq8KDWkubQuMDNjHxAPReIp5CatSSMnzmZ/MKupESj83L2bf/D7B0TPSYXFJi5W5izupUEMBzeQpxFM6O6IN089KPZC5wCtxj/75/lQs0IDUPIx7/3Yg3rMEV1ScPjC6HwAsL37L8bsV1Cm8TvGuW2MA7Z8av9uAypaejr47KrTC19cHgqGAFdZOe+mPnw62FeUUqq1X9mg==");
        server.setPort(22);
        server.setUsername("roro");
    }

    @Test
    @DisplayName("RHEL6 Server Assessment Test")
    public void rhel6Assessment() throws Exception {
        // Change logic deleted method.
//        Object obj = assessment.assessment(DistributionChecker.convert(server));

//        String result = JsonUtil.objToJson(obj, true);
        logger.info("RHEL 6 Assessment Result");
        logger.info("=======================================");
//        logger.info(result);
//        assertNotNull(result);
    }
}
