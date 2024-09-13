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

package io.playce.roro.common.util;

import io.opentracing.contrib.jdbc.ConnectionInfo;
import io.opentracing.contrib.jdbc.parser.URLParser;
import io.playce.roro.common.dto.info.JdbcInfo;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.JdbcProperty;
import io.playce.roro.common.util.support.parser.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static io.playce.roro.common.util.support.parser.DataBaseConstants.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Slf4j
public class JdbcURLParser {

    private static final String UNKNOWN_PEER = "unknown_peer";

    public static List<JdbcInfo> parse(String url) throws InterruptedException {
        List<JdbcInfo> infos = new ArrayList<>();

        if (StringUtils.isNotEmpty(url)) {
            url = url.replaceAll("log4jdbc:", "");

            ConnectionInfo info = getJdbcUrlConnectionInfo(url);

            if (info == null) {
                log.warn("The JDBC url parser does not exist for [{}].", url);
            } else {
                String peer = info.getDbPeer();
                if (peer.contains(",")) {
                    String[] peers = peer.split(",");
                    addJdbcInfos(peers, info, url, infos);
                } else {
                    addJdbcInfo(info, peer, url, infos);
                }
            }
        }

        return infos;
    }

//    private static JdbcInfo transJdbcInfo(JdbcProperty jdbcProperty) {
//        return JdbcInfo.builder()
//                .type(jdbcProperty.getType())
//                .host(jdbcProperty.getHost())
//                .port(jdbcProperty.getPort())
//                .database(jdbcProperty.getDatabase())
//                .build();
//    }

    private static void addJdbcInfos(String[] peers, ConnectionInfo info, String url, List<JdbcInfo> infos) throws InterruptedException {
        for (String p : peers) {
            addJdbcInfo(info, p, url, infos);
        }
    }

    private static void addJdbcInfo(ConnectionInfo info, String peer, String url, List<JdbcInfo> infos) throws InterruptedException {
        JdbcInfo jdbcInfo = getJdbcInfo(info, peer, url);
        if (jdbcInfo != null) {
            infos.add(jdbcInfo);
        }
    }

    private static JdbcInfo getJdbcInfo(ConnectionInfo info, String peer, String url) throws InterruptedException {

        if (UNKNOWN_PEER.equals(peer)) {
            // parse of jdbc url
            JdbcProperty jdbcProperty = getJdbcProperty(url);
            if (jdbcProperty == null)
                return null;

            return JdbcInfo.builder()
                    .type(jdbcProperty.getType())
                    .user("")
                    .database(jdbcProperty.getDatabase())
                    .host(jdbcProperty.getHost())
                    .port(jdbcProperty.getPort())
                    .build();
        } else {
            String[] p = peer.split(":");
            String host = p.length >= 1 ? p[0] : null;
            String port = p.length == 2 ? p[1] : getDefaultPort(info.getDbType());

            return JdbcInfo.builder()
                    .type(getDbType(info.getDbType()))
                    .user(info.getDbUser())
                    .database(info.getDbInstance())
                    .host(host)
                    .port(Integer.parseInt(port))
                    .build();
        }
    }

    private static String getDefaultPort(String dbType) {
        switch (dbType) {
            case "as400":
                return "3470";
        }
        return "";
    }

    public static JdbcProperty getJdbcProperty(String jdbcUrl) throws InterruptedException {
        JdbcProperty jdbcProperty = null;

        try {
            jdbcUrl = jdbcUrl.replaceAll("log4jdbc:", "");

            if (jdbcUrl.contains("Altibase")) {
                jdbcProperty = new AltibaseURLParser().parse(jdbcUrl);
            } else if (jdbcUrl.contains("derby")) {
                jdbcProperty = new DerbyURLParser().parse(jdbcUrl);
            } else if (jdbcUrl.contains("sybase")) {
                jdbcProperty = new SybaseURLParser().parse(jdbcUrl);
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            // ignore
            log.warn("Unhandled exception occurred while parse jdbc property : ", e);
        }

        return jdbcProperty;
    }


    public static ConnectionInfo getJdbcUrlConnectionInfo(String jdbcUrl) {
        ConnectionInfo connectionInfo;

        if (jdbcUrl.contains("oracle")) {
            connectionInfo = new CustomOracleURLParser().parse(jdbcUrl);
        } else if (jdbcUrl.contains("tibero")) {
            connectionInfo = new CustomTiberoURLParser().parse(jdbcUrl);
        } else if (jdbcUrl.contains("sqlserver")) {
            connectionInfo = new CustomSqlServerURLParser().parse(jdbcUrl);
        } else {
            connectionInfo = URLParser.parse(jdbcUrl);
        }

        return connectionInfo;
    }

    private static String getDbType(String connectionInfoDbType) {
        if (connectionInfoDbType.equalsIgnoreCase("mysql")) {
            return DATABASE_TYPE_MYSQL;
        } else if (connectionInfoDbType.equalsIgnoreCase("oracle")) {
            return DATABASE_TYPE_ORACLE;
        } else if (connectionInfoDbType.equalsIgnoreCase("postgresql")) {
            return DATABASE_TYPE_POSTGRESQL;
        } else if (connectionInfoDbType.equalsIgnoreCase("mariadb")) {
            return DATABASE_TYPE_MARIADB;
        } else if (connectionInfoDbType.equalsIgnoreCase("sqlserver")) {
            return DATABASE_TYPE_MSSQL;
        } else if (connectionInfoDbType.equalsIgnoreCase("db2")) {
            return DATABASE_TYPE_DB2;
        } else if (connectionInfoDbType.equalsIgnoreCase("tibero")) {
            return DATABASE_TYPE_TIBERO;
        } else {
            return "";
        }
    }

}