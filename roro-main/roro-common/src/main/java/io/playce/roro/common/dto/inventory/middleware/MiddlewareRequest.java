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
 * Jaeeon Bae       1월 27, 2022            First Draft.
 */
package io.playce.roro.common.dto.inventory.middleware;

import io.playce.roro.common.dto.inventory.inventory.InventoryRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@ToString
public class MiddlewareRequest extends InventoryRequest {

    private String middlewareInventoryName;
    private String middlewareTypeCode;
    private String vendorName;
    private String engineVersion;
    private String engineName;
    private String engineInstallPath;
    private String domainHomePath;

    // https://cloud-osci.atlassian.net/wiki/spaces/PRUS/pages/22225158189
    private String dedicatedAuthenticationYn = "N";
    private Long credentialId;
    private String userName;
    private String userPassword;
}