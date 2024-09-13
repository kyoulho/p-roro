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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Jeongho Baek   9월 10, 2021		First Draft.
 */
package io.playce.roro.asmt.windows.impl.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.asmt.windows.command.PowerShellVersion2Command;
import io.playce.roro.asmt.windows.command.PowerShellVersion2UnderCommand;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.Service;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.SystemInformation;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.playce.roro.asmt.windows.impl.factory.PowerShellParseUtil.splitToArrayByCrlf;
import static io.playce.roro.common.util.WinRmUtils.executePsShell;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Slf4j
public class PowerShellVersion2Execute extends PowerShellVersion2UnderExecute {

    @SuppressWarnings("DuplicatedCode")
    @Override
    public SystemInformation getSystemInformation(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        SystemInformation systemInformation = new SystemInformation();

        try {
            String systemInfo = executePsShell(targetHost, PowerShellVersion2UnderCommand.SYSTEM_INFO);
            String computerSystem = executePsShell(targetHost, PowerShellVersion2UnderCommand.WMI_COMPUTER_SYSTEM);
            String hotfixes = executePsShell(targetHost, PowerShellVersion2UnderCommand.WMI_HOTFIXES);

            log.debug("\n" + PowerShellVersion2UnderCommand.SYSTEM_INFO);
            log.debug("\n" + PowerShellVersion2UnderCommand.WMI_COMPUTER_SYSTEM);
            log.debug("\n" + PowerShellVersion2UnderCommand.WMI_HOTFIXES);

            systemInformation.setHostName(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Host Name"));
            systemInformation.setOsName(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "OS Name"));
            systemInformation.setOsVersion(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "OS Version"));
            systemInformation.setOsManufacturer(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "OS Manufacturer"));
            systemInformation.setOsConfiguration(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "OS Configuration"));
            systemInformation.setOsBuildType(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "OS Build Type"));
            systemInformation.setRegisteredOwner(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Registered Owner"));
            systemInformation.setRegisteredOrganization(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Registered Organization"));
            systemInformation.setProductId(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Product ID"));
            systemInformation.setOriginalInstallDate(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Original Install Date"));
            systemInformation.setSystemBootTime(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "System Boot Time"));
            systemInformation.setSystemManufacturer(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "System Manufacturer"));
            systemInformation.setSystemModel(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "System Model"));
            systemInformation.setSystemType(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "System Type"));
            systemInformation.setProcessors(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Processor(s)"));
            systemInformation.setBiosVersion(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "BIOS Version"));
            systemInformation.setWindowsDirectory(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Windows Directory"));
            systemInformation.setSystemDirectory(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "System Directory"));
            systemInformation.setBootDevice(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Boot Device"));
            systemInformation.setSystemLocale(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "System Locale"));
            systemInformation.setInputLocale(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Input Locale"));
            systemInformation.setTimeZone(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Time Zone"));
            systemInformation.setTotalPhysicalMemory(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Total Physical Memory"));
            systemInformation.setAvailablePhysicalMemory(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Available Physical Memory"));
            systemInformation.setVirtualMemoryMaxSize(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Virtual Memory: Max Size", BACKWARD));
            systemInformation.setVirtualMemoryAvailable(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Virtual Memory: Available", BACKWARD));
            systemInformation.setVirtualMemoryInUse(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Virtual Memory: In Use", BACKWARD));
            systemInformation.setPageFileLocations(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Page File Location(s)"));
            systemInformation.setDomain(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Domain"));
            systemInformation.setLogonServer(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Logon Server"));

            List<String> hotFixes = new ArrayList<>();
            if (StringUtils.isNotEmpty(hotfixes)) {
                String[] tempArray = splitToArrayByCrlf(hotfixes);

                // 첫번째 Row Header 이름, 마지막 공백 라인이 들어가서 -2를 해준다.
                if (tempArray.length > 2) {
                    hotFixes.add((tempArray.length - 2) + " Hotfix(s) Installed. ");
                    for (int i = 1; i < tempArray.length; i++) {
                        if (StringUtils.isNotEmpty(tempArray[i].replaceAll("\\r", "").trim())) {
                            hotFixes.add("[" + String.format("%03d", i) + "] : " + tempArray[i].replaceAll("\\r", "").trim());
                        }
                    }

                    systemInformation.setHotFixes(String.join(", ", hotFixes));
                }
            }

            systemInformation.setNetworkCards(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Network Card(s)"));
            systemInformation.setHyperVRequirements(PowerShellParseUtil.getSystemPropertyValue(systemInfo, "Hyper-V Requirements"));
            systemInformation.setManufacturer(PowerShellParseUtil.getSystemPropertyValue(computerSystem, "Manufacturer"));
            systemInformation.setModel(PowerShellParseUtil.getSystemPropertyValue(computerSystem, "Model"));
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("System Information", e.getMessage());
            return new SystemInformation();
        }

        return systemInformation;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<Service> getServices(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2Command.SERVICE);
            log.debug("\n" + PowerShellVersion2Command.SERVICE);
            log.debug("\n" + result);

            String[] stringArrays = splitToArrayByCrlf(result);
            List<Service> services = new ArrayList<>();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    String[] serviceArray = temp.replaceAll("\"", "").split(",", -1);
                    Service service = new Service();
                    service.setName(serviceArray[0]);
                    service.setDisplayName(serviceArray[1]);
                    service.setServiceName(serviceArray[2]);
                    service.setServiceType(serviceArray[3]);
                    service.setStartType(serviceArray[4]);
                    service.setStatus(serviceArray[5]);
                    services.add(service);
                }
            }

            return services;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Service", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String removeDoubleQuotation(String value) {
        return value.replaceAll("\"", "");
    }
}
//end of PowerShellMajorVersion1.java