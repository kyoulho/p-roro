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
import io.playce.roro.asmt.windows.command.PowerShellVersion3OverCommand;
import io.playce.roro.asmt.windows.command.PowerShellVersion4Command;
import io.playce.roro.asmt.windows.command.PowerShellVersion5Command;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.Process;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.*;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.playce.roro.asmt.windows.impl.factory.PowerShellParseUtil.isJsonArray;
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
public class PowerShellVersion5Execute extends PowerShellVersion3OverExecute {

    @SuppressWarnings("DuplicatedCode")
    public Environment getEnvironment(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        Environment environment = new Environment();

        try {
            String result = executePsShell(targetHost, PowerShellVersion3OverCommand.ENVIRONMENT);
            log.debug("\n" + PowerShellVersion3OverCommand.ENVIRONMENT.toString());
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {
                environment = objectMapper.readValue(result, Environment.class);
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Environment", e.getMessage());
            return new Environment();
        }

        return environment;
    }

    @Override
    public List<Process> getProcess(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Process> processes = new ArrayList<>();

        try {
            String result = executePsShellByOverThere(targetHost, PowerShellVersion5Command.PROCESS);
            log.debug("\n" + PowerShellVersion5Command.PROCESS);
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

    @SuppressWarnings("DuplicatedCode")
    @Override
    public Timezone getTimezone(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        Timezone timezone = new Timezone();

        try {
            String result = executePsShell(targetHost, PowerShellVersion5Command.TIMEZONE);
            log.debug("\n" + PowerShellVersion5Command.TIMEZONE);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {
                timezone = objectMapper.readValue(result, Timezone.class);
            }

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Timezone", e.getMessage());
            return new Timezone();
        }

        return timezone;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<Schedule> getSchedules(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Schedule> schedules = new ArrayList<>();

        try {
            String result = executePsShell(targetHost, PowerShellVersion5Command.SCHEDULE);
            log.debug("\n" + PowerShellVersion4Command.SCHEDULE);
            log.debug("\n" + result);


            if (StringUtils.isNotEmpty(result)) {
                if (isJsonArray(result)) {
                    schedules = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Schedule[].class)));
                } else {
                    schedules.add(objectMapper.readValue(result, Schedule.class));
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
            String result = executePsShell(targetHost, PowerShellVersion5Command.LOCAL_USER);
            log.debug("\n" + PowerShellVersion5Command.LOCAL_USER);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {
                if (isJsonArray(result)) {
                    localUsers = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, LocalUser[].class)));
                } else {
                    localUsers.add(objectMapper.readValue(result, LocalUser.class));
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