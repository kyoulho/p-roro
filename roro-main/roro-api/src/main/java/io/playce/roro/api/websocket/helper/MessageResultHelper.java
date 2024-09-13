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
 * SangCheon Park   Mar 22, 2022		    First Draft.
 */
package io.playce.roro.api.websocket.helper;

import io.playce.roro.common.dto.websocket.RoRoMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
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
public class MessageResultHelper {

    private static Map<String, List<RoRoMessage>> resultMap = new ConcurrentHashMap<>();

    /**
     * Sets result.
     *
     * @param key
     * @param value
     */
    public synchronized static void setResult(String key, RoRoMessage value) {
        List<RoRoMessage> result = resultMap.get(key);

        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(value);

        resultMap.put(key, result);
    }

    /**
     * Gets result.
     *
     * @param key
     *
     * @return
     */
    public synchronized static RoRoMessage getResult(String key) {
        List<RoRoMessage> result = resultMap.get(key);

        RoRoMessage value = null;
        if (result != null) {
            value = result.remove(0);

            if (result.size() == 0) {
                resultMap.remove(key);
            }
        }

        return value;
    }

    /**
     * Gets results.
     *
     * @param key
     *
     * @return
     */
    public synchronized static List<RoRoMessage> getResults(String key) {
        List<RoRoMessage> result = resultMap.get(key);

        if (result != null) {
            resultMap.remove(key);
        }

        return result;
    }
}
//end of MessageResultHelper.java