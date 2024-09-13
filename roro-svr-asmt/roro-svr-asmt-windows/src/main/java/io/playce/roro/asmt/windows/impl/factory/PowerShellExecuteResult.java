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
public interface PowerShellExecuteResult {

    SystemInformation getSystemInformation(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    Environment getEnvironment(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws Exception;

    Cpu getCpu(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    List<Network> getNetworks(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    List<Dns> getDns(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    List<Route> getRoutes(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    List<Port> getPorts(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    List<Firewall> getFirewalls(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    List<Disk> getDisks(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    List<InstalledSoftware> getInstalledSoftware(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    List<Process> getProcess(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    List<Service> getServices(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    Timezone getTimezone(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws Exception;

    List<Schedule> getSchedules(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    List<LocalUser> getLocalUsers(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

    List<LocalGroupUser> getLocalGroupUsers(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException;

}
//end of PowerShellExecuteResult.java