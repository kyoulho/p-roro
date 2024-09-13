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
 * Jeongho Baek   9월 12, 2021		First Draft.
 */
package io.playce.roro.asmt.windows.command;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
public class PowerShellVersion5Command {

    // 환경변수
    public static final String ENVIRONMENT = "Get-ChildItem env: | format-table";

    public static final String PROCESS =
            "Get-process -IncludeUserName |\n" +
                    "    Select Handles, ID, ProcessName, UserName,\n" +
                    "    @{Name='NPM';Expression={[math]::Round($_.NPM / 1KB)}},\n" +
                    "    @{Name='PM';Expression={($_.PM / 1KB)}},\n" +
                    "    @{Name='WS';Expression={($_.WS / 1KB)}},\n" +
                    "    @{Name='CPU';Expression={[math]::Round($_.CPU, 2)}},\n" +
                    "    @{Name='Path';Expression={ if($_.Path -ne $Null) {$_.Path} else{\"\"}}},\n" +
                    "    @{Name='Company';Expression={ if($_.Company -ne $Null) {$_.Company} else{\"\"}}},\n" +
                    "    @{Name='Description';Expression={ if($_.Description -ne $Null) {$_.Description} else{\"\"}}},\n" +
                    "    @{Name='CommandLine';Expression={ (Get-WmiObject Win32_Process -Filter \"ProcessId = $($_.ID)\").CommandLine}} |\n" +
                    "    ConvertTo-Json";

    public static final String TIMEZONE =
            "Get-TimeZone |\n" +
                    "    Select Id, DisplayName, StandardName, DaylightName, SupportsDaylightSavingTime |\n" +
                    "    ConvertTo-Json";

    public static final String SCHEDULE =
            "Get-ScheduledTask |\n" +
                    "    Select TaskPath, TaskName,\n" +
                    "    @{Name='Description';Expression={ if($_.Description -ne $Null) {$_.Description} else{\"\"}}},\n" +
                    "    @{Name='State';Expression={$_.State.toString()}} |\n" +
                    "    ConvertTo-Json";

    public static final String LOCAL_USER =
            "Get-LocalUser |\n" +
                    "    Select Name, Enabled, ObjectClass, Description |\n" +
                    "    ConvertTo-Json";

}
//end of PowerShellVersion4Command.java