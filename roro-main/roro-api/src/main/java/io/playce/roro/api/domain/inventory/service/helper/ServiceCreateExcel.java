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
 * Hoon Oh       12월 01, 2021            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper;

import io.playce.roro.api.common.util.DateTimeUtils;
import io.playce.roro.common.dto.common.excel.ListToExcelDto;
import io.playce.roro.common.dto.common.label.Label;
import io.playce.roro.common.dto.inventory.service.ServiceResponse;
import io.playce.roro.jpa.entity.UserMaster;
import io.playce.roro.jpa.repository.UserMasterRepository;
import io.swagger.v3.core.util.Constants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Component
public class ServiceCreateExcel {

    private final UserMasterRepository userMasterRepository;

    public ServiceCreateExcel(UserMasterRepository userMasterRepository) {
        this.userMasterRepository = userMasterRepository;
    }

    /**
     * Make service header row sheet.
     *
     * @return the sheet
     */
    public List<String> generateServiceHeaderRow() {
        List<String> headerItemList = new ArrayList<>();
        headerItemList.add("Service ID");
        headerItemList.add("Service Name");
        headerItemList.add("Business Code");
        headerItemList.add("Business Category");
        headerItemList.add("Labels");
        headerItemList.add("Migration Y/N");
        headerItemList.add("Man-Month");
        headerItemList.add("Preferences Schedule Start Date");
        headerItemList.add("Preferences Schedule End Date");
        headerItemList.add("Application Schedule Test Start Date");
        headerItemList.add("Application Schedule Test End Date");
        headerItemList.add("Cut-Over Date");
        headerItemList.add("Severity");
        headerItemList.add("Server Count");
        headerItemList.add("Middleware Count");
        headerItemList.add("Application Count");
        headerItemList.add("Database Count");
        headerItemList.add("Development Manager");
        headerItemList.add("Maintenance Manager");
        headerItemList.add("Deployment Manager");
        headerItemList.add("Operation Manager");

        return headerItemList;
    }

    /**
     * Make service body row sheet.
     *
     * @param serviceResponsesList the service responses list
     *
     * @return the sheet
     */
    public List<ListToExcelDto.RowItem> generateServiceBodyRow(List<ServiceResponse> serviceResponsesList) {

        List<ListToExcelDto.RowItem> rowItems = new ArrayList<>();

        for (ServiceResponse serviceResponse : serviceResponsesList) {
            ListToExcelDto.RowItem rowItem = new ListToExcelDto.RowItem();
            rowItem.getCellItemList().add(serviceResponse.getServiceId());
            rowItem.getCellItemList().add(serviceResponse.getServiceName());
            rowItem.getCellItemList().add(serviceResponse.getBusinessCategoryCode());
            rowItem.getCellItemList().add(serviceResponse.getBusinessCategoryName());
            if (serviceResponse.getLabelList() != null) {
                StringBuilder labelName = new StringBuilder();
                for (Label.LabelResponse label : serviceResponse.getLabelList()) {
                    if (labelName.length() > 0) {
                        labelName.append(Constants.COMMA);
                    }
                    labelName.append(label.getLabelName());
                }
                rowItem.getCellItemList().add(labelName.toString());
            } else {
                rowItem.getCellItemList().add("");
            }
            rowItem.getCellItemList().add(serviceResponse.getMigrationTargetYn());
            rowItem.getCellItemList().add(serviceResponse.getMigrationManMonth() == null ? 0.0f : serviceResponse.getMigrationManMonth());
            rowItem.getCellItemList().add(DateTimeUtils.convertDefaultDateFormat(serviceResponse.getMigrationEnvConfigStartDatetime()));
            rowItem.getCellItemList().add(DateTimeUtils.convertDefaultDateFormat(serviceResponse.getMigrationEnvConfigEndDatetime()));
            rowItem.getCellItemList().add(DateTimeUtils.convertDefaultDateFormat(serviceResponse.getMigrationTestStartDatetime()));
            rowItem.getCellItemList().add(DateTimeUtils.convertDefaultDateFormat(serviceResponse.getMigrationTestEndDatetime()));
            rowItem.getCellItemList().add(DateTimeUtils.convertDefaultDateFormat(serviceResponse.getMigrationCutOverDatetime()));
            rowItem.getCellItemList().add(serviceResponse.getSeverity());
            rowItem.getCellItemList().add(serviceResponse.getServerCount());
            rowItem.getCellItemList().add(serviceResponse.getMiddlewareCount());
            rowItem.getCellItemList().add(serviceResponse.getApplicationCount());
            rowItem.getCellItemList().add(serviceResponse.getDatabaseCount());

            rowItem.getCellItemList().add(getManagerInfoSummary(serviceResponse, "DEVELOP"));
            rowItem.getCellItemList().add(getManagerInfoSummary(serviceResponse, "MAINT"));
            rowItem.getCellItemList().add(getManagerInfoSummary(serviceResponse, "DEPLOY"));
            rowItem.getCellItemList().add(getManagerInfoSummary(serviceResponse, "OP"));

            rowItems.add(rowItem);
        }

        return rowItems;
    }

    private String getManagerInfoSummary(ServiceResponse serviceResponse, String managerType) {
        if (CollectionUtils.isNotEmpty(serviceResponse.getManagers())) {
            ServiceResponse.ManagerResponse managerResponse = serviceResponse.getManagers().stream()
                    .filter(s -> s.getManagerTypeCode().equals(managerType))
                    .findFirst().orElse(null);

            if (managerResponse != null) {
                UserMaster user = userMasterRepository.findById(managerResponse.getUserId()).orElse(null);

                if (user != null) {
                    String managerInfoSummary = user.getUserNameEnglish();

                    if (StringUtils.isNotEmpty(user.getUserEmail()) && StringUtils.isEmpty(user.getUserMobile())) {
                        managerInfoSummary += " (" + user.getUserEmail() + ")";
                    } else if (StringUtils.isEmpty(user.getUserEmail()) && StringUtils.isNotEmpty(user.getUserMobile())) {
                        managerInfoSummary += " (" + user.getUserMobile() + ")";
                    } else if (StringUtils.isNotEmpty(user.getUserEmail()) && StringUtils.isNotEmpty(user.getUserMobile())) {
                        managerInfoSummary += " (" + user.getUserEmail() + " / " + user.getUserMobile() + ")";
                    }
                    return managerInfoSummary;
                }
            }
        }
        return "";
    }
}
//end of ServiceCreateExcel.java