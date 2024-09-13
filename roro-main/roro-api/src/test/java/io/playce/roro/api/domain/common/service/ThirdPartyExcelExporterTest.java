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
 * SangCheon Park   Nov 03, 2022		    First Draft.
 */
package io.playce.roro.api.domain.common.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.playce.roro.api.domain.cloudreadiness.service.CloudReadinessExcelExporter;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionResponse;
import io.playce.roro.common.util.JsonUtil;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class ThirdPartyExcelExporterTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        ThirdPartyExcelExporter exporter = new ThirdPartyExcelExporter(new CloudReadinessExcelExporter());

        String jsonStr = IOUtils.toString(Objects.requireNonNull(ThirdPartyExcelExporterTest.class.getResourceAsStream("/thirdParties.json")), StandardCharsets.UTF_8);

        TypeReference<List<ThirdPartySolutionResponse>> type = new TypeReference<>() {};
        List<ThirdPartySolutionResponse> thirdPartySolutionList = JsonUtil.jsonToObj(jsonStr, type);

        ByteArrayOutputStream out = exporter.createExcelReport(thirdPartySolutionList);

        try (FileOutputStream fileOutputStream = new FileOutputStream("/tmp/result.xlsx")) {
            out.writeTo(fileOutputStream);
        }
    }
}