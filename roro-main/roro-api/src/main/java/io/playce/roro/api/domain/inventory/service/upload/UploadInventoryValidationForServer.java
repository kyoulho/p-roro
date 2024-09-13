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
 * Jaeeon Bae       12월 13, 2021            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.upload;

import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.dto.inventory.inventory.InventoryUploadFail;
import io.playce.roro.jpa.entity.CredentialMaster;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.entity.ServerMaster;
import io.playce.roro.jpa.repository.InventoryMasterRepository;
import io.playce.roro.jpa.repository.ServerMasterRepository;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.playce.roro.api.common.CommonConstants.EXCEL_PW_MSG;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadInventoryValidationForServer {

    private final InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
    private final InventoryMasterRepository inventoryMasterRepository;
    private final ServerMasterRepository serverMasterRepository;
    private final ServerMapper serverMapper;
    private final InventoryProcessMapper inventoryProcessMapper;

    public void validationServerInventory(String sheetName, InventoryMaster serverInventoryMaster,
                                          ServerMaster serverMaster, CredentialMaster credentialMaster, List<String> serverCodeList,
                                          Map<String, List<Integer>> ipPorts, List<InventoryUploadFail> validationList, int row) {

        /* Server Validations */
        checkServerCode(sheetName, serverInventoryMaster.getCustomerInventoryCode(), serverCodeList, validationList, row);
        checkValidateIpAddress(sheetName, serverMaster.getRepresentativeIpAddress(), validationList, row);
        checkValidatePort(sheetName, serverMaster.getConnectionPort(), validationList, row);
        checkSheetIpAndPorts(sheetName, serverMaster, ipPorts, validationList, row);
        checkExistKeyFilePath(sheetName, credentialMaster, validationList, row);
        checkValidateKeyFileContents(sheetName, credentialMaster, validationList, row);
        checkDuplicateIpAndPorts(sheetName, serverInventoryMaster, serverMaster, validationList, row);
        checkPasswordAndKeyFile(sheetName, credentialMaster, validationList, row);
        checkEnableSuYnAndRootPassword(sheetName, serverMaster, validationList, row);
        checkMonitoringValues(sheetName, serverMaster, validationList, row);
        checkValidateModifyIpAddress(sheetName, serverInventoryMaster, serverMaster, validationList, row);
    }

    /**
     * 엑셀 시트에 동일한 Server Code 가 중복 체크
     */
    private void checkServerCode(String sheetName, String customerInventoryCode, List<String> serverCodeList, List<InventoryUploadFail> validationList, int row) {
        if (customerInventoryCode != null && serverCodeList.contains(customerInventoryCode)) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(0);
            inventoryUploadFail.setColumnNumber("Server Code");
            inventoryUploadFail.setFailDetail("Server Code cannot be duplicated.");
            validationList.add(inventoryUploadFail);
        } else {
            serverCodeList.add(customerInventoryCode);
        }
    }

    /**
     * 엑셀 시트에 IP Address & Port 중복 체크
     */
    private void checkSheetIpAndPorts(String sheetName, ServerMaster serverMaster, Map<String, List<Integer>> ipPorts, List<InventoryUploadFail> validationList, int row) {
        if (ipPorts.isEmpty()) {
            // sheet 의 ip address and port 확인
            List<Integer> ports = new ArrayList<>();
            ports.add(serverMaster.getConnectionPort());
            ipPorts.put(serverMaster.getRepresentativeIpAddress(), ports);
        } else {
            List<Integer> ipAddressAndPort = ipPorts.get(serverMaster.getRepresentativeIpAddress());
            int port = serverMaster.getConnectionPort();

            if (ipAddressAndPort != null) {
                if (ipAddressAndPort.contains(port)) {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                    inventoryUploadFail.setSheet(sheetName);
                    inventoryUploadFail.setRowNumber(0);
                    inventoryUploadFail.setColumnNumber("Port");
                    inventoryUploadFail.setFailDetail("IP Address & Port cannot be duplicated.");
                    validationList.add(inventoryUploadFail);
                } else {
                    ipPorts.get(serverMaster.getRepresentativeIpAddress()).add(port);
                }
            }
        }
    }

    /**
     * Credential Key 파일이 있는 경우, 해당 경로에 key file 이 있는지 체크
     */
    private void checkExistKeyFilePath(String sheetName, CredentialMaster credentialMaster, List<InventoryUploadFail> validationList, int row) {
        String keyFilePath = credentialMaster.getKeyFilePath();

        if (StringUtils.isNotEmpty(keyFilePath)) {
            if (!new File(keyFilePath).exists()) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Private Key File Path");
                inventoryUploadFail.setFailDetail("Key File(" + keyFilePath + ") does not exist.");
                validationList.add(inventoryUploadFail);
            }
        }
    }

    /**
     * 유효한 key file content 인지 체크
     */
    private void checkValidateKeyFileContents(String sheetName, CredentialMaster credentialMaster, List<InventoryUploadFail> validationList, int row) {
        String keyFileContents = credentialMaster.getKeyFileContent();

        if (StringUtils.isNotEmpty(credentialMaster.getKeyFileContent()) && !EXCEL_PW_MSG.equals(credentialMaster.getKeyFileContent())) {
            if (!keyFileContents.startsWith("----")) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Private Key File Contents");
                inventoryUploadFail.setFailDetail("Key string contents have to start with \"-----\".");
                validationList.add(inventoryUploadFail);
            }
        }
    }

    /**
     * 이미 등록된 IP Address & Port 가 있는지 체크
     */
    private void checkDuplicateIpAndPorts(String sheetName, InventoryMaster inventoryMaster, ServerMaster serverMaster, List<InventoryUploadFail> validationList, int row) {
        // project id & ip address & port & inventory id로 validation check
        InventoryMaster inventory = inventoryMasterRepository
                .findByProjectIdAndInventoryIdAndInventoryTypeCode(inventoryMaster.getProjectId(), inventoryMaster.getInventoryId(), Domain1001.SVR.name());

        if (inventory == null && inventoryMaster.getInventoryId() != null) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Server ID");
            inventoryUploadFail.setFailDetail("Server ID(" + inventoryMaster.getInventoryId() + ") does not exist in this project.");
            validationList.add(inventoryUploadFail);
        }

        int count;
        if (inventory == null) {
            count = serverMapper.selectServerCountByRepresentativeIpAddressAndPortAndProjectId(serverMaster.getRepresentativeIpAddress(),
                    serverMaster.getConnectionPort(), inventoryMaster.getProjectId(), null);
        } else {
            // update인 경우 쿼리에서 자신의 inventory id를 제외하고 조회한다.
            count = serverMapper.selectServerCountByRepresentativeIpAddressAndPortAndProjectId(serverMaster.getRepresentativeIpAddress(),
                    serverMaster.getConnectionPort(), inventoryMaster.getProjectId(), inventory.getInventoryId());
        }

        if (count > 0) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("IP Address,Port");
            inventoryUploadFail.setFailDetail("IP Address and Port already exist in project");
            validationList.add(inventoryUploadFail);
        }
    }

    /**
     * Password & Key File 인증 방법이 하나라도 있는지 체크
     */
    private void checkPasswordAndKeyFile(String sheetName, CredentialMaster credentialMaster, List<InventoryUploadFail> validationList, int row) {
        if (StringUtils.isEmpty(credentialMaster.getUserPassword())
                && (StringUtils.isEmpty(credentialMaster.getKeyFileName())
                && StringUtils.isEmpty(credentialMaster.getKeyFileContent()))) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Password,Key File Contents,Key File Path");
            inventoryUploadFail.setFailDetail("Either Password or (Key File Contents or Key File Path) must not be null.");
            validationList.add(inventoryUploadFail);
        }

    }

    /**
     * 서버의 Customer Inventory Code 가 유효한 값인지 체크
     */
    public void checkCustomerServerCode(String sheetName, InventoryMaster serverInventoryMaster, List<String> inventoryCodeList, List<InventoryUploadFail> validationList, int row) {
        String customerServerCode = serverInventoryMaster.getCustomerInventoryCode();
        int count;

        if (StringUtils.isNotEmpty(customerServerCode)) {
            count = inventoryMasterRepository.countByCustomerInventoryCodeAndProjectId(customerServerCode, serverInventoryMaster.getProjectId());

            if (count == 0) {
                if (!inventoryCodeList.contains(customerServerCode)) {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                    inventoryUploadFail.setSheet(sheetName);
                    inventoryUploadFail.setRowNumber(row);
                    inventoryUploadFail.setColumnNumber("Server Code");
                    inventoryUploadFail.setFailDetail("Server Code(" + customerServerCode + ") does not exist.");
                    validationList.add(inventoryUploadFail);
                }
            }
        }
    }

    /**
     * 파라미터의 Ip Address 가 유효한 값인지 체크
     */
    public void checkValidateIpAddress(String sheetName, String representativeIpAddress, List<InventoryUploadFail> validationList, int row) {
        if (representativeIpAddress != null) {
            boolean validIp = inetAddressValidator.isValidInet4Address(representativeIpAddress);

            if (!validIp) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Ip Address");
                inventoryUploadFail.setFailDetail("Ip Address(" + representativeIpAddress + ") is not valid.");
                validationList.add(inventoryUploadFail);
            }
        }
    }

    /**
     * 파라미터의 Port 가 유효한 값인지 체크
     */
    public void checkValidatePort(String sheetName, Integer connectionPort, List<InventoryUploadFail> validationList, int row) {
        if (connectionPort != null) {
            if (connectionPort < 1 || connectionPort > 65535) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Port");
                inventoryUploadFail.setFailDetail("Port number must be in range from 1 to 65535.");
                validationList.add(inventoryUploadFail);
            }
        }
    }

    /**
     * su 기능 사용여부가 'Y' 일때 Root 패스워드가 빈 값인지 체크
     */
    private void checkEnableSuYnAndRootPassword(String sheetName, ServerMaster serverMaster, List<InventoryUploadFail> validationList, int row) {
        if (Domain101.Y.name().equals(serverMaster.getEnableSuYn())
                && StringUtils.isEmpty(serverMaster.getRootPassword())) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Root Password");
            inventoryUploadFail.setFailDetail("'su Y/N' If the value is 'Y', the Root Password cannot be blank.");
            validationList.add(inventoryUploadFail);
        }
    }

    /**
     * 모니터링 사용 여부에 따라 모니터링 데이터가 빈 값이 아닌지 체크
     */
    private void checkMonitoringValues(String sheetName, ServerMaster serverMaster, List<InventoryUploadFail> validationList, int row) {
        if (Domain101.Y.name().equalsIgnoreCase(serverMaster.getMonitoringYn())) {
            if (serverMaster.getMonitoringCycle() != null && !CronExpression.isValidExpression(serverMaster.getMonitoringCycle())) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Monitoring Cycle");
                inventoryUploadFail.setFailDetail("Server Monitoring Cycle 'Cron Expression' is not Valid.");
                validationList.add(inventoryUploadFail);
            }

            // Cron Expression, Start Time, End Time은 모두 필수 아님 (https://cloud-osci.atlassian.net/wiki/spaces/PRUS/pages/22225223681/g.+Monitoring)
            // if (serverMaster.getMonitoringCycle() == null) {
            //     InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            //     inventoryUploadFail.setSheet(sheetName);
            //     inventoryUploadFail.setRowNumber(row);
            //     inventoryUploadFail.setColumnNumber("Monitoring Cycle");
            //     inventoryUploadFail.setFailDetail("'Monitoring Y/N' If the value is 'Y', the Monitoring Cycle cannot be blank.");
            //     validationList.add(inventoryUploadFail);
            // } else {
            //     if (!CronExpression.isValidExpression(serverMaster.getMonitoringCycle())) {
            //         InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            //         inventoryUploadFail.setSheet(sheetName);
            //         inventoryUploadFail.setRowNumber(row);
            //         inventoryUploadFail.setColumnNumber("Monitoring Cycle");
            //         inventoryUploadFail.setFailDetail("Server Monitoring Cycle 'Cron Expression' is not Valid.");
            //         validationList.add(inventoryUploadFail);
            //     }
            // }
            //
            // if (serverMaster.getMonitoringStartDatetime() == null) {
            //     InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            //     inventoryUploadFail.setSheet(sheetName);
            //     inventoryUploadFail.setRowNumber(row);
            //     inventoryUploadFail.setColumnNumber("Monitoring Start Datetime");
            //     inventoryUploadFail.setFailDetail("'Monitoring Y/N' If the value is 'Y', the Monitoring Start Datetime cannot be blank.");
            //     validationList.add(inventoryUploadFail);
            // }
            //
            // if (serverMaster.getMonitoringEndDatetime() == null) {
            //     InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            //     inventoryUploadFail.setSheet(sheetName);
            //     inventoryUploadFail.setRowNumber(row);
            //     inventoryUploadFail.setColumnNumber("Monitoring End Datetime");
            //     inventoryUploadFail.setFailDetail("'Monitoring Y/N' If the value is 'Y', the Monitoring End Datetime cannot be blank.");
            //     validationList.add(inventoryUploadFail);
            // }
        }
    }

    /**
     * 기존의 서버 수정할 때 IP Address를 수정할 경우, Server Assessment Scan의 Complete가 있는지 체크
     * Asessment Scan의 Complete가 하나라도 있으면 Ip Address 수정 불가
     */
    private void checkValidateModifyIpAddress(String sheetName, InventoryMaster serverInventoryMaster, ServerMaster serverMaster, List<InventoryUploadFail> validationList, int row) {
        if (serverInventoryMaster.getInventoryId() != null) {
            ServerMaster originServerMaster = serverMasterRepository.findById(serverInventoryMaster.getInventoryId()).orElse(null);

            if (originServerMaster != null) {
                String originIpAddress = originServerMaster.getRepresentativeIpAddress();
                String newIpAddress = serverMaster.getRepresentativeIpAddress();

                if (!(originIpAddress.equals(newIpAddress))) {
                    if (inventoryProcessMapper.selectSuccessCompleteCount(serverInventoryMaster.getInventoryId()) != 0) {
                        InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                        inventoryUploadFail.setSheet(sheetName);
                        inventoryUploadFail.setRowNumber(row);
                        inventoryUploadFail.setColumnNumber("IP Address");
                        inventoryUploadFail.setFailDetail("This server has already been successfully scanned and the IP address cannot be modified.");
                        validationList.add(inventoryUploadFail);
                    }
                }
            }
        }
    }
}