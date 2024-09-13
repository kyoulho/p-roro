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
 * SangCheon Park   Feb 23, 2022		    First Draft.
 */
package io.playce.roro.common.util.support.parser;

import io.playce.roro.common.util.support.JdbcProperty;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class AltibaseURLParser implements JdbcUrlParser {


    @Override
    public JdbcProperty parse(String jdbcUrl) {
        int DEFAULT_PORT = 20300;

        if (!jdbcUrl.startsWith("jdbc:Altibase")) {
            return null;
        }

        return JdbcProperty.builder()
                .type(DataBaseConstants.DATABASE_TYPE_ALTIBASE)
                .host(getHost(jdbcUrl))
                .port(getPort(jdbcUrl) == -1 ? DEFAULT_PORT : getPort(jdbcUrl))
                .database(getDbInstance(jdbcUrl))
                .params(getParams(jdbcUrl))
                .build();
    }
}
//end of AltibaseJdbcUrlParser.java