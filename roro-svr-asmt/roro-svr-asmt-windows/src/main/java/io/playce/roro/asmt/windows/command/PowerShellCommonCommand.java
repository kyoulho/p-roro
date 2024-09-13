/*
 * Copyright 2020 The Playce-RoRo Project.
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
 * Jeongho Baek   11월 03, 2020		First Draft.
 */
package io.playce.roro.asmt.windows.command;

import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
public class PowerShellCommonCommand {

    // Administrator Check
    public static final List<String> CHECK_ADMINISTRATOR = Arrays.asList(
            "$myWindowsID = [System.Security.Principal.WindowsIdentity]::GetCurrent()",
            "$myWindowsPrincipal = New-Object System.Security.Principal.WindowsPrincipal($myWindowsID)",
            "$adminRole = [System.Security.Principal.WindowsBuiltInRole]::Administrator",
            "$myWindowsPrincipal.IsInRole($adminRole)");

    public static final String WMIC_VERIFY_REPOSITORY = "winmgmt /verifyrepository";
    public static final String WMIC_SERVICE_COMMAND = "wmic service where 'name like \"%winmgmt%\"' get name, state /format:list";

    // Build Number
    public static final String BUILD_NUMBER = "[System.Environment]::OSVersion.Version.Build";

    // PowerShell Version
    public static final String POWERSHELL_VERSION = "$Host.Version.Major";

}
//end of WindowExecuteCommand.java