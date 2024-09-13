/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Hoon Oh          7월 07, 2021		First Draft.
 */
package io.playce.roro.mw.asmt.jeus.helper;

import io.playce.roro.mw.asmt.jeus.dto.JeusAssessmentResult;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 3.0.0
 */
@Slf4j
public class JeusHelper {


    public static class DatabaseHelper {

        public static Map<String, String> baseUrlMap;
        static {
            baseUrlMap = new HashMap<>();
            baseUrlMap.put("oracle", "jdbc:oracle:thin:@IP:PORT:DBNAME");
            baseUrlMap.put("mysql", "jdbc:mysql://IP:PORT/DBNAME?characterEncoding=utf8&autoReconnect=true");
            baseUrlMap.put("mssql", "jdbc:sqlserver://IP:PORT;DatabaseName=DBNAME");
            baseUrlMap.put("maria", "jdbc:mariadb://IP:PORT/DBNAME?characterEncoding=utf8&autoReconnect=true");
            baseUrlMap.put("tibero", "jdbc:tibero:thin:@IP:PORT:DBNAME");
            baseUrlMap.put("db2", "jdbc:db2://IP:PORT/DBNAME");
            baseUrlMap.put("infomix", "jdbc:informix-sqli://IP:PORT/DBNAME");
            baseUrlMap.put("sybase", "jdbc:sybase:Tds:IP:PORT/DBNAME");
            baseUrlMap.put("postgresql", "jdbc:postgresql://IP:PORT/DBNAME");
        }

        public static String getConnectionUrl(JeusAssessmentResult.Database database) {

            List<JeusAssessmentResult.DatabaseProperty> properties = database.getProperty();
            if (properties == null) return null;

            for (JeusAssessmentResult.DatabaseProperty property : properties) {
                if (property.getName().equals("URL")) {
                    return property.getValue();
                }
            }
            /*String baseUrlFormat = baseUrlMap.get(database.getVendor().toLowerCase());

            String ipAddress = database.getServerName() != null ? database.getServerName() : "";
            String port = database.getPortNumber() != null ? database.getPortNumber() : "";
            String dbname = database.getDatabaseName() != null ? database.getDatabaseName() : "";


            if (ObjectUtils.isEmpty(baseUrlFormat)) {
                log.debug("There is not supported yet");
                return "";
            }

            if (database.getProperty() != null) {
                if (database.getVendor().equals("oracle")) {
                    for (JeusAssessmentResult.DatabaseProperty property : database.getProperty()) {
                        if ("URL".equals(property.getName().toUpperCase())) {
                            return property.getValue();
                        }
                    }
                }

                if (ObjectUtils.isEmpty(port)) {
                    for (JeusAssessmentResult.DatabaseProperty property : database.getProperty()) {
                        if ("PORTNUMBER".equals(property.getName().toUpperCase())) {
                            port = property.getValue();
                        }
                    }
                }
            }

            return baseUrlFormat.replace("IP", ipAddress)
                    .replace("PORT", port)
                    .replace("DBNAME", dbname);
        */
            return null;
        }
    }
}
//end of JeusHelper.java