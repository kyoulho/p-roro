/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       3월 23, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper.assessment;

import io.playce.roro.common.dto.assessment.AssessmentResultDto;
import io.playce.roro.common.dto.inventory.database.DatabaseEngineResponseDto;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
public class DatabaseParser implements AssessmentParser {

    @Override
    public Object parse(Object object, String assessment) throws Exception {
        DatabaseEngineResponseDto database = (DatabaseEngineResponseDto) object;

        if (assessment == null) {
            return null;
        }

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(assessment);
        JSONObject jsonObj = (JSONObject) obj;
        org.json.simple.JSONObject dbData = JsonUtil.getJsonObject(jsonObj);

        JSONObject instance = (dbData.containsKey("instance") && dbData.get("instance") != null) ?
                (JSONObject) dbData.get("instance") : null;
        JSONArray databases = (dbData.containsKey("databases") && dbData.get("databases") != null) ?
                (JSONArray) dbData.get("databases") : null;
        JSONArray dbLink = (dbData.containsKey("dbLinks") && dbData.get("dbLinks") != null) ?
                (JSONArray) dbData.get("dbLinks") : null;

        if (instance == null && databases == null) {
            return null;
        }

        // version
        String version = null;
        if (instance != null && instance.containsKey("version") && instance.get("version") != null) {
            // version = (String) instance.get("version");
            version = MWCommonUtil.getStringValue(instance, "version");
        }

        // databases information
        Long tableCount = null;
        Long viewCount = null;
        Long indexCount = null;
        Long functionCount = null;
        Long procedureCount = null;
        Long triggerCount = null;
        Long sequenceCount = null;
        if (databases != null) {
            for (Object db : databases) {
                JSONObject dbObj = (JSONObject) db;

                if (dbObj.containsKey("name") && dbObj.get("name") != null
                        && dbObj.get("name").equals(database.getDatabaseServiceName())) {
                    JSONArray table = (JSONArray) dbObj.get("tables");
                    tableCount = (long) table.size();

                    JSONArray view = (JSONArray) dbObj.get("views");
                    viewCount = (long) view.size();

                    JSONArray index = (JSONArray) dbObj.get("indexes");
                    indexCount = (long) index.size();

                    JSONArray function = (JSONArray) dbObj.get("functions");
                    functionCount = (long) function.size();

                    JSONArray procedure = (JSONArray) dbObj.get("procedures");
                    procedureCount = (long) procedure.size();

                    JSONArray trigger = (JSONArray) dbObj.get("triggers");
                    triggerCount = (long) trigger.size();

                    if (dbObj.containsKey("sequences") && dbObj.get("sequences") != null) {
                        JSONArray sequence = (JSONArray) dbObj.get("sequences");
                        sequenceCount = (long) sequence.size();
                    }
                }
            }
        }

        // db link
        Long dbLinkCount = null;
        if (dbLink != null) {
            dbLinkCount = (long) dbLink.size();
        }

        // if (Domain1013.ORACLE.name().equals(database.getInventoryDetailTypeCode()) ||
        //         Domain1013.MYSQL.name().equals(database.getInventoryDetailTypeCode()) ||
        //         Domain1013.MARIADB.name().equals(database.getInventoryDetailTypeCode()) ||
        //         Domain1013.TIBERO.name().equals(database.getInventoryDetailTypeCode()) ||
        //         Domain1013.MSSQL.name().equals(database.getInventoryDetailTypeCode()) ||
        //         Domain1013.SYBASE.name().equals(database.getInventoryDetailTypeCode()) ||
        //         Domain1013.POSTGRE.name().equals(database.getInventoryDetailTypeCode())) {
        //     return AssessmentResultDto.DatabaseProperty.builder()
        //             .version(version != null ? version.replaceAll("\"", "") : "")
        //             .tableCount(tableCount != null ? tableCount : 0L)
        //             .viewCount(viewCount != null ? viewCount : 0L)
        //             .indexCount(indexCount != null ? indexCount : 0L)
        //             .functionCount(functionCount != null ? functionCount : 0L)
        //             .procedureCount(procedureCount != null ? procedureCount : 0L)
        //             .triggerCount(triggerCount != null ? triggerCount : 0L)
        //             .sequenceCount(sequenceCount != null ? sequenceCount : 0L)
        //             .dbLinkCount(dbLinkCount != null ? dbLinkCount : 0L)
        //             .build();
        // } else {
        //     return null;
        // }

        return AssessmentResultDto.DatabaseProperty.builder()
                .version(version != null ? version.replaceAll("\"", "") : "")
                .tableCount(tableCount != null ? tableCount : 0L)
                .viewCount(viewCount != null ? viewCount : 0L)
                .indexCount(indexCount != null ? indexCount : 0L)
                .functionCount(functionCount != null ? functionCount : 0L)
                .procedureCount(procedureCount != null ? procedureCount : 0L)
                .triggerCount(triggerCount != null ? triggerCount : 0L)
                .sequenceCount(sequenceCount != null ? sequenceCount : 0L)
                .dbLinkCount(dbLinkCount != null ? dbLinkCount : 0L)
                .build();
    }
}