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
 * Dong-Heon Han    Dec 03, 2021		    First Draft.
 */

package io.playce.roro.excel.template.vo;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
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
@Getter
@ToString
public class SheetMap {
    private final Map<String, List<RecordMap>> sheetMap = new HashMap<>();

    public void put(String sheetName, RecordMap record) {
        if(!sheetMap.containsKey(sheetName)) {
            sheetMap.put(sheetName, new ArrayList<>());
        }

        sheetMap.get(sheetName).add(record);
    }

    public List<RecordMap> getSheet(String sheetName) {
        return sheetMap.get(sheetName);
    }
}