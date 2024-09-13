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
 * SangCheon Park   Aug 26, 2022		    First Draft.
 */
package io.playce.roro.common.setting;

import io.playce.roro.common.property.CommonProperties;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public class SettingsHandler {

    public static Map<String, String> settingsMap = new ConcurrentHashMap<>();

    public static String getSettingsValue(String key) {
        if (settingsMap.containsKey(key)) {
            return settingsMap.get(key);
        } else {
            // Environment 에서 값을 조회한다.
            return CommonProperties.getProperty(key);
        }
    }

    @Synchronized
    public static void setSettingsValue(String key, String value) {
        // null 값이 저장되면 empty string으로 변경한다.
        if (value == null) {
            settingsMap.put(key, StringUtils.EMPTY);
        } else {
            settingsMap.put(key, value);
        }
    }
}