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
 * Jaeeon Bae       6월 08, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.upload;

import io.playce.roro.common.dto.inventory.inventory.InventoryUploadFail;
import io.playce.roro.jpa.entity.CredentialMaster;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
public class UploadValidation {

    /**
     * Password 와 key File 체크
     */
    public void validatePasswordAndKeyFiles(String sheetName, CredentialMaster credentialMaster, List<InventoryUploadFail> validationList, int row) {
        if (StringUtils.isEmpty(credentialMaster.getUserPassword())
                && StringUtils.isEmpty(credentialMaster.getKeyFileContent())) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Password,Private Key File Contents");
            inventoryUploadFail.setFailDetail("'Dedicated Authentication Y/N' If the value is 'Y', Either Password or Private Key File Contents must not be null.");
            validationList.add(inventoryUploadFail);
        }
    }
}