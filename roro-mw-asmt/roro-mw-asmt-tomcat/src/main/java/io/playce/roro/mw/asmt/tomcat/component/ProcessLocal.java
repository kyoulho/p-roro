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

package io.playce.roro.mw.asmt.tomcat.component;

import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.mw.asmt.tomcat.dto.TomcatAssessmentResult;
import io.playce.roro.mw.asmt.tomcat.enums.CONFIG_FILES;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@Slf4j
public class ProcessLocal {

    public void loadConfigFiles(String configFilePath, TomcatAssessmentResult.Instance instance) {
        File config = new File(configFilePath);
        Map<String, TomcatAssessmentResult.ConfigFile> map = new HashMap<>();
        if (config.isDirectory()) {
            for (CONFIG_FILES configFile : CONFIG_FILES.values()) {
                File findFile = findFile(config, configFile.filename());
                if (findFile == null) {
                    log.debug("config file not found: {}", configFile.filename());
                    continue;
                }

                TomcatAssessmentResult.ConfigFile configFileResult = new TomcatAssessmentResult.ConfigFile();
                configFileResult.setPath(findFile.getAbsolutePath());
                try {
                    configFileResult.setContents(FileUtils.readFileToString(findFile, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    log.info("[{}] file read failed : [{}]", configFile.filename(), e.getMessage());
                    log.error("{}", e.getMessage(), e);
                    if (configFile == CONFIG_FILES.TOMCAT_CONFIG_SERVER || configFile == CONFIG_FILES.TOMCAT_CONFIG_CONTEXT) {
                        throw new InsufficientException("Tomcat config file(" + findFile.getAbsolutePath() + ") read failed. Please check file is exist and has permission to read.");
                    }
                }
                map.put(configFile.name(), configFileResult);
            }
        }
        instance.setConfigFiles(map);
    }

    private File findFile(File root, String configFileName) {
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files == null)
                return null;

            for (File file : files) {
                File f = findFile(file, configFileName);
                if (f != null) {
                    return f;
                }
            }
        } else {
            if (root.getName().equals(configFileName)) {
                return root;
            }
        }
        return null;
    }
}