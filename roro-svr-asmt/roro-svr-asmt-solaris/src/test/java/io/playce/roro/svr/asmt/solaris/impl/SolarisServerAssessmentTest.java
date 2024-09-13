// /*
//  * Copyright 2022 The Playce-RoRo Project.
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  *      http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  *
//  * Revision History
//  * Author            Date                Description
//  * ---------------  ----------------    ------------
//  * Hoon Oh       1월 24, 2022            First Draft.
//  */
// package io.playce.roro.svr.asmt.solaris.impl;
//
// import io.playce.roro.common.util.JsonUtil;
// import io.playce.roro.common.util.support.TargetHost;
// import io.playce.roro.svr.asmt.ServerAssessment;
// import io.playce.roro.svr.asmt.solaris.dto.SolarisAssessmentResult;
// import org.junit.jupiter.api.Test;
//
// import java.io.IOException;
//class SolarisServerAssessmentTest {
//
//     @Test
//     void assessment() {
//         TargetHost targetHost = new TargetHost();
//         targetHost.setIpAddress("192.168.0.22");
//         targetHost.setPort(22);
//         targetHost.setUsername("root");
//         targetHost.setPassword("jan01jan");
//
//         ServerAssessment assessment = new SolarisServerAssessment();
//         SolarisAssessmentResult result = assessment.assessment(targetHost);
//
//         try {
//             System.out.println(JsonUtil.objToJson(result, true));
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//
//     }
// }