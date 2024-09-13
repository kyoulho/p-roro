/*
 * Copyright 2021 The playce-roro-v3} Project.
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
 * Dong-Heon Han    Dec 01, 2021		    First Draft.
 */

package io.playce.roro.excel.template.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Configuration
@ConfigurationProperties(prefix = "excel")
@Setter @Getter
public class ExcelTemplateConfig {
    private Map<String, SheetInfo> sheets;

    public SheetInfo getSheetName(String key) {
        return sheets.get(key);
    }

    @Setter @Getter
    public static class SheetInfo {
        private String beanName;
        private int headerSize;
        private List<String> cols;
        private List<String> keyCols;
    }
}