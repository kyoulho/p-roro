package io.playce.roro.svr.asmt.linux;

import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.common.hardware.CpuInfo;
import io.playce.roro.svr.asmt.dto.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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
 * Dong-Heon Han    Jul 15, 2022		First Draft.
 */

@Slf4j
class LinuxServerAssessmentTest {
    private LinuxServerAssessment assessment;
    private TargetHost targetHost;
    private Map<String, String> errorMap;

    @Test
    void getCpuInfo() throws IOException, InterruptedException {
        File hellovisionCpu = ResourceUtils.getFile("classpath:HelloVision-lscpu.txt");
        String result = getFileContent(hellovisionCpu);
        log.debug(result);
        CpuInfo cpuInfo = assessment.getCpuInfo(targetHost, result, errorMap);
        log.debug("{}", cpuInfo);
    }

    private String getFileContent(File file) throws IOException {
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    @BeforeEach
    void setUp() {
        assessment = new LinuxServerAssessment() {
            @Override
            public <T extends ServerAssessmentResult> T assessment(TargetHost targetHost) throws InterruptedException {
                return null;
            }
        };
        targetHost = new TargetHost();
        errorMap = new HashMap<>();
    }

    @Test
    void getUsers() throws IOException, InterruptedException {
        File hellovisionPasswd = ResourceUtils.getFile("classpath:HelloVision-passwd.txt");
        String result = getFileContent(hellovisionPasswd);
        Map<String, User> users = assessment.getUsers(targetHost, result, errorMap, true);
    }
}