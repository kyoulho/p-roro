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
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * io.playce.roro.common.util.JdbcURLParser는 지원되는 DB가 적고 Parameter에 대한 내용을 확인할 수 없어 일단 현재 코드를 유지한다.
 * https://github.com/opentracing-contrib/java-jdbc/blob/master/src/main/java/io/opentracing/contrib/jdbc/parser/URLParser.java
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public interface JdbcUrlParser {

    JdbcProperty parse(String jdbcUrl);

    /**
     * @param jdbcUrl
     *
     * @return
     */
    default String getHost(String jdbcUrl) {
        String uriWithoutPrefixStr = jdbcUrl.substring(5);

        URI uri = URI.create(uriWithoutPrefixStr);

        return uri.getHost();
    }

    /**
     * @param jdbcUrl
     *
     * @return
     */
    default Integer getPort(String jdbcUrl) {
        String uriWithoutPrefixStr = jdbcUrl.substring(5);

        URI uri = URI.create(uriWithoutPrefixStr);

        return uri.getPort();
    }

    /**
     * @param jdbcUrl
     *
     * @return
     */
    default String getDbInstance(String jdbcUrl) {
        String uriWithoutPrefixStr = jdbcUrl.substring(5);

        URI uri = URI.create(uriWithoutPrefixStr);

        return uri.getPath().substring(1);
    }

    /**
     * @param jdbcUrl
     *
     * @return
     */
    default Map<String, String> getParams(String jdbcUrl) {
        Map<String, String> paramMap = new HashMap<>();

        if (jdbcUrl.lastIndexOf("?") != -1) {
            String params = jdbcUrl.substring(jdbcUrl.lastIndexOf("?") + 1);

            if (StringUtils.isNotEmpty(params)) {
                String[] paramArray = params.split("&");

                for (String param : paramArray) {
                    String[] paramKeyValue = param.split("=", -1);
                    if (paramKeyValue.length == 2) {
                        paramMap.put(paramKeyValue[0], paramKeyValue[1]);
                    }
                }
            }
        }

        return paramMap;
    }
}
//end of JdbcUrlParser.java
