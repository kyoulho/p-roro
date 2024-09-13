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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       12월 09, 2021            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper;

import io.playce.roro.common.code.Domain1105;
import io.playce.roro.common.code.Domain1106;
import io.playce.roro.common.code.Domain1107;
import io.playce.roro.common.code.Domain1110;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
public class InventoryUploadCommonCodeHelper {

    public static String getInventoryTypeCode(String inventoryTypeCode) {
        String code = null;

        if (inventoryTypeCode != null) {
            switch (inventoryTypeCode.trim()) {
                case "Server":
                    code = "SVR";
                    break;
                case "Middleware":
                    code = "MW";
                    break;
                case "Application":
                    code = "APP";
                    break;
                case "Database":
                    code = "DBMS";
                    break;
                default:
                    break;
            }
        }

        return code;
    }

    public static String getMigrationTypeCode(String migrationTypeCode) {
        String code = null;

        if (migrationTypeCode != null) {
            migrationTypeCode = migrationTypeCode.trim();
            for (Domain1107 type : Domain1107.values()) {
                if (type.fullname().equals(migrationTypeCode)) {
                    code = type.name();
                }
            }
        }

        return code;
    }

    public static String getServerUsageTypeCode(String serverUsageTypeCode) {
        String code = null;

        if (serverUsageTypeCode != null) {
            serverUsageTypeCode = serverUsageTypeCode.trim();
            for (Domain1110 type : Domain1110.values()) {
                if (type.fullname().equals(serverUsageTypeCode)) {
                    code = type.name();
                }
            }
        }

        return code;
    }

    public static String getHypervisorTypeCode(String hypervisorTypeCode) {
        String code = null;

        if (hypervisorTypeCode != null) {
            hypervisorTypeCode = hypervisorTypeCode.trim();
            for (Domain1105 type : Domain1105.values()) {
                if (type.fullname().equals(hypervisorTypeCode)) {
                    code = type.name();
                }
            }
        }

        return code;
    }

    public static String getDualizationTypeCode(String dualizationTypeCode) {
        String code = null;

        if (dualizationTypeCode != null) {
            dualizationTypeCode = dualizationTypeCode.trim();
            for (Domain1106 type : Domain1106.values()) {
                if (type.fullname().equals(dualizationTypeCode)) {
                    code = type.name();
                }
            }
        }

        return code;
    }
}
//end of CommonCodeHelper.java
