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

package io.playce.roro.mw.asmt.tomcat.enums.attribute;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
public enum RESOURCE {
    auth, type, factory, testWhileIdle, testOnBorrow, testOnReturn, validationQuery, validationInterval,
    timeBetweenEvictionRunsMillis, maxActive, minIdle, maxWait, initialSize, removeAbandonedTimeout,
    removeAbandoned, logAbandoned, minEvictableIdleTimeMillis, jmxEnabled, jdbcInterceptors, username, password,
    driverClassName, poolPreparedStatements, url,
    name,
    maxWaitMillis,
    maxTotal,
    maxIdle,
    maxAge,
}

//name="jdbc/DatabaseName"
//        auth="Container"
//        type="javax.sql.DataSource"
//        username="dbUser"
//        password="dbPassword"
//        url="jdbc:postgresql://localhost/dbname"
//        driverClassName="org.postgresql.Driver"
//        initialSize="20"
//        maxWaitMillis="15000"
//        maxTotal="75"
//        maxIdle="20"
//        maxAge="7200000"
//        testOnBorrow="true"
//        validationQuery="select 1"