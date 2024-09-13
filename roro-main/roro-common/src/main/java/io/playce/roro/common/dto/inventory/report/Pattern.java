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
public enum Pattern {
    TYPE,
    ID,
    NAME,
    USERNAME,
    SCANNED_DATE,
    PROJECT_NAME,
    SERVICE_NAME,
    SERVER_NAME,
    SERVICE_BUSINESS_CODE,
    SERVICE_BUSINESS_CATEGORY,
    SERVER_IP_ADDRESS,
    SERVER_PORT,
    SERVER_USERNAME,
    MIDDLEWARE_TYPE,
    MIDDLEWARE_VENDOR,
    MIDDLEWARE_ENGINE_NAME,
    MIDDLEWARE_ENGINE_VERSION,
    APPLICATION_TYPE,
    DATABASE_ENGINE_NAME,
    DATABASE_PORT,
    DATABASE_SERVICE_NAME,
    DATABASE_USERNAME
}