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
 * Jaeeon Bae       1월 10, 2022            First Draft.
 */
package io.playce.roro.common.util.support;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <pre>
 * 미들웨어의 정보를 담는 POJO 객체
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Getter
@Setter
@ToString
public class MiddlewareInventory {
    private Long serverInventoryId;
    private String engineVersion;

    /* Middleware Inventory Name */
    private String inventoryName;

    /* Middleware Inventory Detail Type Code */
    private String inventoryDetailTypeCode;

    /* Solution Path */
    private String engineInstallationPath;

    /* Domain Home */
    private String domainHomePath;

    private String configFilePath;

    private String vendorName;

    //v2에서 사용중 코드 호환성때문에 남겨둠.
    private String cellName;
    private String nodeName;
    private String name;
    private String processName;
    private String profileName;

    /* Middleware Type Code */
    private String middlewareTypeCode;
}