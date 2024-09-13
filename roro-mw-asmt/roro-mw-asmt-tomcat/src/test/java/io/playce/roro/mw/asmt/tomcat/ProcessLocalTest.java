package io.playce.roro.mw.asmt.tomcat;

import io.playce.roro.mw.asmt.tomcat.component.ProcessLocal;
import io.playce.roro.mw.asmt.tomcat.dto.TomcatAssessmentResult;
import org.junit.jupiter.api.BeforeEach;
/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Dong-Heon Han    Feb 12, 2022		First Draft.
 */

class ProcessLocalTest {
    private ProcessLocal processLocal;
    private TomcatAssessmentResult.Instance instance;

    @BeforeEach
    void setUp() {
        processLocal = new ProcessLocal();
        instance = new TomcatAssessmentResult.Instance();
    }

//    @Test
    void loadConfigFiles() {
        String configFilePath = "/home/yohan/works/product/tomcat/apache-tomcat-9.0.56";
        processLocal.loadConfigFiles(configFilePath, instance);
        System.out.println("------------------");
    }
}