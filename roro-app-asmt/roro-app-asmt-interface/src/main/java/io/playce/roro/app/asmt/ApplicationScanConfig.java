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
 * SangCheon Park   Jul 06, 2022		    First Draft.
 */
package io.playce.roro.app.asmt;

import io.playce.roro.common.setting.SettingsHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static io.playce.roro.common.setting.SettingsConstants.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "appscan")
public class ApplicationScanConfig {

    @Getter(AccessLevel.NONE)
    private String fileExtensions;
    @Getter(AccessLevel.NONE)
    private String excludeFilenames;
    @Getter(AccessLevel.NONE)
    private Pattern excludeFilePattern;
    @Getter(AccessLevel.NONE)
    private String excludeDomains;

    private Remove remove;
    private Copy copy;

    public List<String> getFileExtensions() {
        String extensions = SettingsHandler.getSettingsValue(APPSCAN_FILE_EXTENSIONS);
        extensions = extensions.replaceAll(" +", StringUtils.EMPTY);
        return Arrays.asList(extensions.split(","));
    }

    public List<String> getExcludeFilenames() {
        String fileNames = SettingsHandler.getSettingsValue(APPSCAN_EXCLUDE_FILENAMES);
        fileNames = fileNames.replaceAll(" +", StringUtils.EMPTY);
        return Arrays.asList(fileNames.split(","));
    }

    public List<String> getExcludeDomains() {
        String domains = SettingsHandler.getSettingsValue(APPSCAN_EXCLUDE_DOMAINS);
        domains = domains.replaceAll(" +", StringUtils.EMPTY);
        return Arrays.asList(domains.split(","));
    }

    public Pattern getExcludeFilePattern() {
        StringBuilder regex = new StringBuilder("(");

        for (String p : getExcludeFilenames()) {
            regex.append(".*").append(p.replaceAll("\\.", "\\\\.")).append("|");
        }
        regex.deleteCharAt(regex.toString().length() - 1);
        regex.append(")");

        excludeFilePattern = Pattern.compile(regex.toString());

        return excludeFilePattern;
    }

    @Setter
    public static class Remove {
        private boolean filesAfterScan;

        public boolean isFilesAfterScan() {
            return Boolean.parseBoolean(SettingsHandler.getSettingsValue(APPSCAN_REMOVE_FILES_AFTER_SCAN));
        }
    }

    @Setter
    public static class Copy {
        private String ignoreFilenames;
        private boolean onlyMatchedExtensions;

        public String getIgnoreFilenames() {
            return SettingsHandler.getSettingsValue(APPSCAN_COPY_IGNORE_FILENAMES);
        }

        public boolean isOnlyMatchedExtensions() {
            return Boolean.parseBoolean(SettingsHandler.getSettingsValue(APPSCAN_COPY_ONLY_MATCHED_EXTENSIONS));
        }
    }
}