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
 * SangCheon Park   Feb 22, 2022		    First Draft.
 */
package io.playce.roro.common.util.support.parser;


import io.playce.roro.common.util.support.JdbcProperty;

import java.net.URI;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class DerbyURLParser implements JdbcUrlParser {

    @Override
    public JdbcProperty parse(String jdbcUrl) {
        int DEFAULT_PORT = 1527;

        if (!jdbcUrl.startsWith("jdbc:derby")) {
            return null;
        }

        JdbcProperty jdbcProperty = null;

        try {
            jdbcProperty = JdbcProperty.builder()
                    .type(DataBaseConstants.DATABASE_TYPE_DERBY)
                    .host(getHost(jdbcUrl))
                    .port(getPort(jdbcUrl) == -1 ? DEFAULT_PORT : getPort(jdbcUrl))
                    .database(getDbInstance(jdbcUrl))
                    .params(null)
                    .build();
        } catch (Exception e) {
            // ignore
        }

        return jdbcProperty;
    }

    @Override
    public String getDbInstance(String jdbcUrl) {
        String uriWithoutPrefixStr = jdbcUrl.substring(5);

        URI uri = URI.create(uriWithoutPrefixStr);

        return uri.getPath().substring(1).split(";")[0];
    }
}
//end of DerbyJdbcUrlParser.java