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
 * Dong-Heon Han    Feb 17, 2022		First Draft.
 */

package io.playce.roro.mw.asmt;

import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Slf4j
public abstract class AbstractMiddlewarePostProcess implements MiddlewarePostProcess {

    public static final String NOT_JAVA = "NOT JAVA";

    /**
     * Jdbc Url Parser.
     */
    /*public MiddlewareAssessmentResult.JdbcProperty getJdbcProperty(String jdbcUrl) {
        MiddlewareAssessmentResult.JdbcProperty jdbcProperty = null;

        try {
            jdbcUrl = jdbcUrl.replaceAll("log4jdbc:", "");

            if (jdbcUrl.contains("oracle")) {
                jdbcProperty = new OracleJdbcUrlParser().parse(jdbcUrl);
            } else if (jdbcUrl.contains("mysql")) {
                jdbcProperty = new MySqlJdbcUrlParser().parse(jdbcUrl);
            } else if (jdbcUrl.contains("mariadb")) {
                jdbcProperty = new MariaDbJdbcUrlParser().parse(jdbcUrl);
            } else if (jdbcUrl.contains("postgresql")) {
                jdbcProperty = new PostgreSqlJdbcUrlParser().parse(jdbcUrl);
            } else if (jdbcUrl.contains("tibero")) {
                jdbcProperty = new TiberoJdbcUrlParser().parse(jdbcUrl);
            } else if (jdbcUrl.contains("sqlserver")) {
                jdbcProperty = new MsSqlJdbcUrlParser().parse(jdbcUrl);
            } else if (jdbcUrl.contains("db2")) {
                jdbcProperty = new Db2JdbcUrlParser().parse(jdbcUrl);
            } else if (jdbcUrl.contains("Altibase")) {
                jdbcProperty = new AltibaseJdbcUrlParser().parse(jdbcUrl);
            } else if (jdbcUrl.contains("derby")) {
                jdbcProperty = new DerbyJdbcUrlParser().parse(jdbcUrl);
            }
        } catch (Exception e) {
            // ignore
            log.warn("Unhandled exception occurred while parse jdbc property : ", e);
        }

        return jdbcProperty;
    }*/
}