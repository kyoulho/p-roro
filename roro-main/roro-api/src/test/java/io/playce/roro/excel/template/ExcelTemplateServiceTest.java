package io.playce.roro.excel.template;

import io.playce.roro.excel.template.config.ExcelTemplateConfig;
import io.playce.roro.excel.template.vo.RecordMap;
import io.playce.roro.excel.template.vo.SheetMap;
import io.playce.roro.jpa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * Dong-Heon Han    Dec 03, 2021		First Draft.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ExcelTemplateServiceTest {
    @Autowired
    private ExcelTemplateService service;

    @Autowired
    private ExcelTemplateConfig excelTemplateConfig;

    @Autowired
    private ModelMapper modelMapper;

    @Test
    public void parseWorkbook() throws IOException {
        File file = new File("src/test/resources/RoRo-v3-Inventory-Template-v0.2.xlsx");

        FileInputStream fileInputStream = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);

        SheetMap result = service.parseWorkbook(workbook, null);

        for(String sheetName: excelTemplateConfig.getSheets().keySet()) {
            log.debug("sheet name: {}", sheetName);
            List<RecordMap> sheet = result.getSheet(sheetName);
            for(RecordMap recod: sheet) {
                setEntity(sheetName, recod);
            }
        }
    }

    private void setEntity(String sheetName, RecordMap record) {
        // log.debug("{}", record.getValueMap());
        switch (sheetName) {
            case "service":
                log.debug("0 - {}", record.getValueMap());
                ServiceMaster serviceMaster = modelMapper.map(record.getValueMap(), ServiceMaster.class);
                log.debug("1 - {}", serviceMaster);
                break;
            case "server":
                log.debug("0 - {}", record.getValueMap());
                InventoryMaster inventoryMaster = modelMapper.map(record.getValueMap(), InventoryMaster.class);
                log.debug("1 - {}", inventoryMaster);
                ServerMaster serverMaster = modelMapper.map(record.getValueMap(), ServerMaster.class);
                log.debug("2 - {}", serverMaster);
                CredentialMaster credentialMaster = modelMapper.map(record.getValueMap(), CredentialMaster.class);
                log.debug("3 - {}", credentialMaster);
                break;
            case "middleware":
                log.debug("0 - {}", record.getValueMap());
                MiddlewareMaster middlewareMaster = modelMapper.map(record.getValueMap(), MiddlewareMaster.class);
                log.debug("1 - {}", middlewareMaster);
                break;
            case "application":
                log.debug("0 - {}", record.getValueMap());
                ApplicationMaster applicationMaster = modelMapper.map(record.getValueMap(), ApplicationMaster.class);
                log.debug("1 - {}", applicationMaster);
                break;
            case "database":
                log.debug("0 - {}", record.getValueMap());
                DatabaseMaster databaseMaster = modelMapper.map(record.getValueMap(), DatabaseMaster.class);
                log.debug("1 - {}", databaseMaster);
                break;
            default:
        }
    }
}