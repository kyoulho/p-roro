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
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.Process;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.*;
import io.playce.roro.common.util.support.TargetHost;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Component
public class PowerShellExecuteResultFactory {

    public SystemInformation getSystemInformation(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getSystemInformation(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getSystemInformation(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getSystemInformation(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getSystemInformation(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getSystemInformation(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new SystemInformation();
        }
    }

    public Environment getEnvironment(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getEnvironment(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getEnvironment(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getEnvironment(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getEnvironment(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getEnvironment(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new Environment();
        }
    }

    public Cpu getCpu(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getCpu(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getCpu(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getCpu(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getCpu(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getCpu(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new Cpu();
        }
    }

    public List<Network> getNetworks(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getNetworks(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getNetworks(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getNetworks(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getNetworks(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getNetworks(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }

    }

    public List<Dns> getDns(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getDns(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getDns(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getDns(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getDns(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getDns(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }
    }

    public List<Route> getRoutes(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getRoutes(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getRoutes(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getRoutes(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getRoutes(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getRoutes(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }
    }

    public List<Port> getPorts(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getPorts(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getPorts(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getPorts(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getPorts(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getPorts(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }
    }

    public Hosts getHosts(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getHosts(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getHosts(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getHosts(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getHosts(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getHosts(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new Hosts();
        }
    }

    public List<Firewall> getFirewalls(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getFirewalls(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getFirewalls(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getFirewalls(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getFirewalls(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getFirewalls(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }
    }

    public List<Disk> getDisks(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getDisks(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getDisks(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getDisks(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getDisks(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getDisks(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }
    }

    public List<InstalledSoftware> getInstalledSoftware(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getInstalledSoftware(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getInstalledSoftware(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getInstalledSoftware(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getInstalledSoftware(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getInstalledSoftware(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }
    }

    public List<Process> getProcess(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getProcess(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getProcess(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getProcess(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getProcess(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getProcess(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }
    }

    public List<Service> getServices(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getServices(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getServices(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getServices(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getServices(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getServices(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }
    }

    public Timezone getTimezone(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getTimezone(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getTimezone(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getTimezone(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getTimezone(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getTimezone(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new Timezone();
        }
    }

    public List<Schedule> getSchedules(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getSchedules(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getSchedules(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getSchedules(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getSchedules(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getSchedules(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }
    }

    public List<LocalUser> getLocalUsers(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getLocalUsers(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getLocalUsers(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getLocalUsers(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getLocalUsers(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getLocalUsers(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }
    }

    public List<LocalGroupUser> getLocalGroupUsers(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion, Map<String, String> errorMap) throws InterruptedException {
        if (powerShellMajorVersion == 1) {
            return new PowerShellVersion1Execute().getLocalGroupUsers(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 2) {
            return new PowerShellVersion2Execute().getLocalGroupUsers(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 3) {
            return new PowerShellVersion3Execute().getLocalGroupUsers(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion == 4) {
            return new PowerShellVersion4Execute().getLocalGroupUsers(targetHost, objectMapper, errorMap);
        } else if (powerShellMajorVersion >= 5) {
            return new PowerShellVersion5Execute().getLocalGroupUsers(targetHost, objectMapper, errorMap);
        } else {
            errorMap.put("Powershell", "Powershell version(" + powerShellMajorVersion + ") is invalid");
            return new ArrayList<>();
        }
    }
}
//end of PowerShellResultFactory.java