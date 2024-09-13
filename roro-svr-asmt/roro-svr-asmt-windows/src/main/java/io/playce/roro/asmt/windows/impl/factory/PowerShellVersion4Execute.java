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
import io.playce.roro.asmt.windows.command.PowerShellVersion4Command;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.Process;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.*;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static io.playce.roro.asmt.windows.impl.factory.PowerShellParseUtil.isJsonArray;
import static io.playce.roro.asmt.windows.impl.factory.PowerShellParseUtil.splitToArrayByCrlf;
import static io.playce.roro.common.util.WinRmUtils.executePsShell;
import static io.playce.roro.common.util.WinRmUtils.executePsShellByOverThere;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Slf4j
public class PowerShellVersion4Execute extends PowerShellVersion3OverExecute {

    @SuppressWarnings("DuplicatedCode")
    @Override
    public Environment getEnvironment(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        Environment environment = new Environment();

        try {
            String result = executePsShell(targetHost, PowerShellVersion4Command.ENVIRONMENT);
            log.debug("\n" + PowerShellVersion4Command.ENVIRONMENT);


            environment.setAllUsersProfile(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ALLUSERSPROFILE"));
            environment.setAppdata(PowerShellParseUtil.getPropertyValueForMultiLine(result, "APPDATA"));
            environment.setCommonProgramFiles(PowerShellParseUtil.getPropertyValueForMultiLine(result, "CommonProgramFiles"));
            environment.setCommonProgramFilesx86(PowerShellParseUtil.getPropertyValueForMultiLine(result, "CommonProgramFiles(x86)"));
            environment.setCommonProgramW6432(PowerShellParseUtil.getPropertyValueForMultiLine(result, "CommonProgramW6432"));
            environment.setComputerName(PowerShellParseUtil.getPropertyValueForMultiLine(result, "COMPUTERNAME"));
            environment.setComSpec(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ComSpec"));
            environment.setLocalAppData(PowerShellParseUtil.getPropertyValueForMultiLine(result, "LOCALAPPDATA"));
            environment.setMsmpiBin(PowerShellParseUtil.getPropertyValueForMultiLine(result, "MSMPI_BIN"));
            environment.setFpNoHostCheck(PowerShellParseUtil.getPropertyValueForMultiLine(result, "FP_NO_HOST_CHECK"));
            environment.setHomeDrive(PowerShellParseUtil.getPropertyValueForMultiLine(result, "HOMEDRIVE"));
            environment.setHomePath(PowerShellParseUtil.getPropertyValueForMultiLine(result, "HOMEPATH"));
            environment.setLogonServer(PowerShellParseUtil.getPropertyValueForMultiLine(result, "LOGONSERVER"));
            environment.setNumberOfProcessors(PowerShellParseUtil.getPropertyValueForMultiLine(result, "NUMBER_OF_PROCESSORS"));
            environment.setOs(PowerShellParseUtil.getPropertyValueForMultiLine(result, "OS"));
            environment.setPath(PowerShellParseUtil.getPropertyValueForMultiLine(result, "Path"));
            environment.setPathExt(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PATHEXT"));
            environment.setProcessorArchitecture(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PROCESSOR_ARCHITECTURE"));
            environment.setProcessorIdentifier(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PROCESSOR_IDENTIFIER"));
            environment.setProcessorLevel(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PROCESSOR_LEVEL"));
            environment.setProcessorRevision(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PROCESSOR_REVISION"));
            environment.setProgramData(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ProgramData"));
            environment.setProgramFiles(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ProgramFiles"));
            environment.setProgramFilesx86(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ProgramFiles(x86)"));
            environment.setProgramW6432(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ProgramW6432"));
            environment.setPrompt(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PROMPT"));
            environment.setPsModulePath(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PSModulePath"));
            environment.setPUBLIC(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PUBLIC"));
            environment.setSystemDrive(PowerShellParseUtil.getPropertyValueForMultiLine(result, "SystemDrive"));
            environment.setSystemRoot(PowerShellParseUtil.getPropertyValueForMultiLine(result, "SystemRoot"));
            environment.setTemp(PowerShellParseUtil.getPropertyValueForMultiLine(result, "TEMP"));
            environment.setTmp(PowerShellParseUtil.getPropertyValueForMultiLine(result, "TMP"));
            environment.setUserDomain(PowerShellParseUtil.getPropertyValueForMultiLine(result, "USERDOMAIN"));
            environment.setUserDomainRoamingProfile(PowerShellParseUtil.getPropertyValueForMultiLine(result, "USERDOMAIN_ROAMINGPROFILE"));
            environment.setUsername(PowerShellParseUtil.getPropertyValueForMultiLine(result, "USERNAME"));
            environment.setUserprofile(PowerShellParseUtil.getPropertyValueForMultiLine(result, "USERPROFILE"));
            environment.setWindir(PowerShellParseUtil.getPropertyValueForMultiLine(result, "windir"));

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Environment", e.getMessage());
            return new Environment();
        }

        return environment;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<Process> getProcess(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Process> processes = new ArrayList<>();

        try {
            String result = executePsShellByOverThere(targetHost, PowerShellVersion4Command.PROCESS);

            log.debug("\n" + PowerShellVersion4Command.PROCESS);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {
                if (isJsonArray(result)) {
                    processes = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Process[].class)));
                } else {
                    processes.add(objectMapper.readValue(result, Process.class));
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Process", e.getMessage());
            return new ArrayList<>();
        }

        return processes;
    }


    @SuppressWarnings({"DuplicatedCode", "unchecked"})
    @Override
    public Timezone getTimezone(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        Timezone timezone = new Timezone();

        try {
            String result = executePsShell(targetHost, PowerShellVersion4Command.TIMEZONE);

            log.debug("\n" + PowerShellVersion4Command.TIMEZONE);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {
                Map<String, String> timeMap = objectMapper.readValue(result, HashMap.class);

                timezone.setId(timeMap.get("StandardName"));
                timezone.setDisplayName(timeMap.get("Caption"));
                timezone.setStandardName(timeMap.get("StandardName"));
                timezone.setDaylightName(timeMap.get("DaylightName"));
                timezone.setSupportsDaylightSavingTime("");

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Timezone", e.getMessage());
            return new Timezone();
        }

        return timezone;
    }

    @SuppressWarnings("DuplicatedCode")
    public List<Schedule> getSchedules(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Schedule> schedules = new ArrayList<>();

        try {
            String result = executePsShell(targetHost, PowerShellVersion4Command.SCHEDULE);

            log.debug("\n" + PowerShellVersion4Command.SCHEDULE);
            log.debug("\n" + result);

            String[] stringArrays = splitToArrayByCrlf(result);

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    String[] scheduleArray = temp.replaceAll("\"", "").split(",", -1);
                    Schedule schedule = new Schedule();
                    schedule.setTaskPath(scheduleArray[0]);
                    schedule.setTaskName(scheduleArray[1]);
                    schedule.setDescription(scheduleArray[2]);
                    schedule.setState(scheduleArray[3]);

                    schedules.add(schedule);
                }
            }

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Schedule", e.getMessage());
            return new ArrayList<>();
        }

        return schedules;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<LocalUser> getLocalUsers(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<LocalUser> localUsers = new ArrayList<>();

        try {
            String result = executePsShell(targetHost, PowerShellVersion4Command.LOCAL_USER);

            log.debug("\n" + PowerShellVersion4Command.LOCAL_USER);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {

                if (isJsonArray(result)) {
                    localUsers = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, LocalUser[].class)));
                } else {
                    localUsers.add(objectMapper.readValue(result, LocalUser.class));
                }

                for (LocalUser tempLocalUser : localUsers) {
                    tempLocalUser.setEnabled(tempLocalUser.isDisabled() ? "false" : "true");
                }

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("User", e.getMessage());
            return new ArrayList<>();
        }

        return localUsers;
    }

}
//end of PowerShellMajorVersion1.java