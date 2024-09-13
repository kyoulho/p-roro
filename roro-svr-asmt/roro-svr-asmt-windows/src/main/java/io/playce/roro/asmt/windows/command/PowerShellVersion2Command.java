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
public class PowerShellVersion2Command {

//    public static final String PROCESS =
//            "Get-Process |\n" +
//                    "    Select Handles, ID, ProcessName, NPM, PM, WS, CPU, Path, Company, Description, \n" +
//                    "    @{Name='CommandLine';Expression={ (Get-WmiObject Win32_Process -Filter \"ProcessId = $($_.ID)\").CommandLine}} |\n" +
//                    "    ConvertTo-Csv -NoTypeInformation | Select-Object -Skip 1";

    public static final String SERVICE =
            "Get-Service |\n" +
                    "    Select Name, DisplayName, ServiceName,\n" +
                    "    @{Name='ServiceType';Expression={$_.ServiceType.toString()}},\n" +
                    "    @{Name='StartType';Expression={$_.StartType.toString()}},\n" +
                    "    @{Name='Status';Expression={$_.Status.toString()}} |\n" +
                    "    ConvertTo-Csv -NoTypeInformation | Select-Object -Skip 1";

}
//end of PowerShellVersion2Command.java