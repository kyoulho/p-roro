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

package io.playce.roro.mw.asmt.jboss.enums.attribute;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
public enum RESOURCE {
    jndiname("jndiName"),
    poolname("poolName"),
    datasources("datasources"),
    datasource("datasource"),
    enabled("enabled"),
    usejavacontext("useJavaContext"),
    connectionurl("connectionUrl"),
    driver("driver"),
    drivers("drivers"),
    security("security"),
    username("userName"),
    password("password"),
    drivername("name"),
    drivermodule("module"),
    xadatasourceclass("xa-datasource-class"),
    threadpools("thread-pools"),
    threadpool("thread-pool"),
    maxthreads("max-threads"),
    keepalivetime("keepalive-time");


    private final String codeName;

    RESOURCE(String codeName) {
        this.codeName = codeName;
    }

    public String getCodeName() {
        return codeName;
    }
}

