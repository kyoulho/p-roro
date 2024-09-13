/*
 * Copyright 2021 The playce-roro-v3} Project.
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
 * Dong-Heon Han    Dec 01, 2021		    First Draft.
 */

package io.playce.roro.excel.template;

import io.playce.roro.common.dto.inventory.inventory.InventoryUploadFail;
import io.playce.roro.excel.template.config.ExcelTemplateConfig;
import io.playce.roro.excel.template.vo.SheetMap;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
public interface InventoryExcelService {
    void parse(ExcelTemplateConfig.SheetInfo sheetInfo, Sheet sheet, SheetMap result, List<InventoryUploadFail> validationList);
}