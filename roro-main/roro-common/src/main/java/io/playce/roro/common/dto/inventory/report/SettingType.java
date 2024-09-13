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
 * Jaeeon Bae       1월 21, 2022            First Draft.
 */
package io.playce.roro.common.dto.inventory.report;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
public enum SettingType {

    /**
     * Service Report Pattern type.
     */
    SERVICE_REPORT_PATTERN("Service_Report_Pattern"),
    /**
     * Server Report Pattern type.
     */
    SERVER_REPORT_PATTERN("Server_Report_Pattern"),
    /**
     * Middleware Report Pattern type.
     */
    MIDDLEWARE_REPORT_PATTERN("Middleware_Report_Pattern"),
    /**
     * Application Report Pattern type.
     */
    APPLICATION_REPORT_PATTERN("Application_Report_Pattern"),
    /**
     * Database Report Pattern type.
     */
    DATABASE_REPORT_PATTERN("Database_Report_Pattern");

    /**
     * The Description.
     */
    private String description;

    /**
     * Instantiates a new Setting type.
     *
     * @param description the description
     */
    private SettingType(String description) {
        this.description = description;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return this.getDescription();
    }
}