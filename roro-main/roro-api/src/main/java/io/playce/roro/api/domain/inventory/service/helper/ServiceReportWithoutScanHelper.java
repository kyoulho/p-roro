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
 * Jaeeon Bae       4월 07, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.dto.inventory.application.ApplicationDatasourceResponse;
import io.playce.roro.common.dto.inventory.application.ApplicationResponse;
import io.playce.roro.common.dto.inventory.database.DatabaseEngineResponseDto;
import io.playce.roro.common.dto.inventory.middleware.MiddlewareResponse;
import io.playce.roro.common.dto.inventory.server.ServerResponse;
import io.playce.roro.common.dto.inventory.service.ServiceDetail;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.entity.ServerNetworkInformation;
import io.playce.roro.jpa.entity.ServerSummary;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Component
@Slf4j
public class ServiceReportWithoutScanHelper {

    public static final String DELIMITER = ", ";

    public void generateServerSheet(ServiceDetail service, ServerResponse server,
                                    ServerSummary serverSummary, List<ServerNetworkInformation> networks, Row row, int rowIndex) {
        int columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;

        ExcelUtil.createCell(row, columnIndex++).setCellValue(rowIndex - 1);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(server.getCustomerInventoryCode()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(server.getCustomerInventoryName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(service.getServiceId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(service.getServiceName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(server.getServerInventoryId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(server.getServerInventoryName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(server.getRepresentativeIpAddress()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(server.getConnectionPort());
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue(server.getMakerName() != null ? server.getMakerName() : "");
        ExcelUtil.createCell(row, columnIndex++).setCellValue(server.getModelName() != null ? server.getModelName() : "");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue(serverSummary != null ? String.valueOf(serverSummary.getCpuCoreCount()) : "");
        ExcelUtil.createCell(row, columnIndex++).setCellValue(serverSummary != null ? String.valueOf(serverSummary.getCpuSocketCount()) : "");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue(CollectionUtils.isNotEmpty(networks) ? networks.stream().map(ServerNetworkInformation::getAddress).collect(Collectors.joining(DELIMITER)) : "");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex).setCellValue("");
    }

    public void generateWebServerSheet(ServiceDetail service, InventoryMaster serverInventoryMaster, MiddlewareResponse web, Row row, int rowIndex) {
        int columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;

        ExcelUtil.createCell(row, columnIndex++).setCellValue(rowIndex - 1);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(web.getCustomerInventoryCode());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(web.getCustomerInventoryName());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(service.getServiceId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(service.getServiceName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster.getInventoryId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster != null ? StringUtils.defaultString(serverInventoryMaster.getInventoryName()) : "");
        ExcelUtil.createCell(row, columnIndex++).setCellValue(web.getMiddlewareInventoryId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(web.getMiddlewareInventoryName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(web.getMiddlewareTypeCode()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(web.getVendorName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(web.getEngineVersion()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(web.getEngineInstallPath()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex).setCellValue("");
    }

    public void generateWasServerSheet(ServiceDetail service, InventoryMaster serverInventoryMaster, MiddlewareResponse was, Row row, int rowIndex) {
        int columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;

        ExcelUtil.createCell(row, columnIndex++).setCellValue(rowIndex - 1);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(was.getCustomerInventoryCode());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(was.getCustomerInventoryName());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(service.getServiceId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(service.getServiceName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster.getInventoryId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster != null ? StringUtils.defaultString(serverInventoryMaster.getInventoryName()) : "");
        ExcelUtil.createCell(row, columnIndex++).setCellValue(was.getMiddlewareInventoryId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(was.getMiddlewareInventoryName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(was.getMiddlewareTypeCode()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(was.getVendorName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(was.getEngineVersion()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(was.getEngineInstallPath()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(was.getDomainHomePath()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex++).setCellValue("");
        ExcelUtil.createCell(row, columnIndex).setCellValue("");
    }

    public void generateDatabaseSheet(ServiceDetail service, InventoryMaster serverInventoryMaster, DatabaseEngineResponseDto database, Row row, int rowIndex) {
        int columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;

        ExcelUtil.createCell(row, columnIndex++).setCellValue(rowIndex - 1);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(database.getCustomerInventoryCode());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(database.getCustomerInventoryName());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(service.getServiceId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(service.getServiceName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster.getInventoryId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster != null ? StringUtils.defaultString(serverInventoryMaster.getInventoryName()) : "");
        ExcelUtil.createCell(row, columnIndex++).setCellValue(database.getDatabaseInventoryId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(database.getDatabaseInventoryName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(database.getVendor()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(String.valueOf(database.getConnectionPort() != null ? database.getConnectionPort() : ""));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(database.getJdbcUrl()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(database.getUserName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(database.getEngineVersion()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(database.getDatabaseServiceName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex).setCellValue(0);
    }

    public void generateApplicationSheet(ServiceDetail service, InventoryMaster serverInventoryMaster,
                                         ApplicationResponse app, List<ApplicationDatasourceResponse> datasoureList, Row row, int rowIndex) {
        int columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;

        ExcelUtil.createCell(row, columnIndex++).setCellValue(rowIndex - 1);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(app.getCustomerInventoryCode());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(app.getCustomerInventoryName());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(service.getServiceId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(service.getServiceName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster.getInventoryId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster != null ? StringUtils.defaultString(serverInventoryMaster.getInventoryName()) : "");
        ExcelUtil.createCell(row, columnIndex++).setCellValue(app.getApplicationInventoryId());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(app.getApplicationInventoryName()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(app.getUploadSourceFilePath()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(generateApplicationType(app.getInventoryDetailTypeCode())));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(StringUtils.defaultString(app.getDeployPath()));
        ExcelUtil.createCell(row, columnIndex++).setCellValue(app.getApplicationSize());
        ExcelUtil.createCell(row, columnIndex++).setCellValue(CollectionUtils.isNotEmpty(datasoureList) ? datasoureList.stream().map(ApplicationDatasourceResponse::getDatasourceName).collect(Collectors.joining(DELIMITER)) : "");
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex++).setCellValue(0);
        ExcelUtil.createCell(row, columnIndex).setCellValue(0);
    }

    /**
     * 애플리케이션 타입 리턴
     */
    private String generateApplicationType(String code) {
        String type = null;
        if (Domain1013.EAR.name().equals(code)) {
            type = "Java Enterprise Application";
        } else if (Domain1013.JAR.name().equals(code)) {
            type = "Java Application";
        } else if (Domain1013.WAR.name().equals(code)) {
            type = "Java Web Application";
        } else if (Domain1013.ETC.name().equals(code)) {
            type = "Etc";
        }

        return type;
    }
}