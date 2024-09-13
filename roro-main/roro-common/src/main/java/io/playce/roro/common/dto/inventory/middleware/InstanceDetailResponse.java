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
 * Jaeeon Bae       2월 15, 2022            First Draft.
 */
package io.playce.roro.common.dto.inventory.middleware;

import io.playce.roro.common.dto.inventory.service.Service;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Getter
@Setter
public class InstanceDetailResponse {

    private List<Service> services;
    private Long serverInventoryId;
    private String serverInventoryName;
    private String representativeIpAddress;
    private Long middlewareInventoryId;
    private String middlewareInventoryName;
    private String middlewareTypeCode;
    private String vendorName;
    private String engineVersion;
    private String middlewareInstanceName;
    private String middlewareInstancePath;
    private String inventoryDetailTypeCode;
    private String middlewareConfigPath;
    private String middlewareInstanceServicePort;
    private String runningUser;
    private String javaVersion;
    private String javaVendor;
    private Long registUserId;
    private Date registDatetime;
}