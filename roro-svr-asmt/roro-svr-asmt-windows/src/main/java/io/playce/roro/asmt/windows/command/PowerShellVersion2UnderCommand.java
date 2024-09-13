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
public class PowerShellVersion2UnderCommand {

    // System Information
    public static final String SYSTEM_INFO = "systeminfo /fo list";

    public static final String WMI_COMPUTER_SYSTEM = "Get-WmiObject win32_computersystem | select *";
    public static final String WMI_HOTFIXES = "wmic QFE Get HotFixID";

    // 환경변수
    public static final String ENVIRONMENT = "Get-ChildItem env: | format-table";

    // CPU
    public static final String CPU = "Get-WmiObject Win32_Processor | Select Name, Caption, NumberOfCores, NumberOfLogicalProcessors, MaxClockSpeed | format-list";

    // 네트워크
    public static final String NETWORK = "Get-WmiObject Win32_NetworkAdapterConfiguration | " +
            "Select InterfaceIndex, Description, IPAddress, DefaultIPGateway, MACAddress, IPEnabled | " +
            "Sort-Object InterfaceIndex";

    // DNS
    public static final String DNS = "Get-WmiObject Win32_NetworkAdapterConfiguration -ComputerName . |\n" +
            "                       Where-Object {$_.DNSServerSearchOrder -ne $null -and $_.ipaddress -notlike \"fe80*\"} |\n" +
            "                       Select InterfaceIndex, Description, DNSServerSearchOrder | Format-List";

    public static final String ROUTE = "Get-WmiObject Win32_IP4RouteTable | Select Destination, NextHop, Metric1, InterfaceIndex | Format-List";

    public static final String PORT = "netstat -ano | Where-Object{$_ -match 'TCP'}";

    //Hosts File
    public static final String HOSTS = "type C:\\Windows\\System32\\drivers\\etc\\hosts";
    public static final String FIREWALL = "netsh advfirewall firewall show rule name=all";

    // Disk
    public static final String DISK =
            "Get-WmiObject Win32_DiskDrive | ForEach-Object {\n" +
                    "  $disk = $_\n" +
                    "  $partitions = \"ASSOCIATORS OF {Win32_DiskDrive.DeviceID='$($disk.DeviceID)'} WHERE AssocClass = Win32_DiskDriveToDiskPartition\"\n" +
                    "  Get-WmiObject -Query $partitions | ForEach-Object {\n" +
                    "    $partition = $_\n" +
                    "    $drives = \"ASSOCIATORS OF {Win32_DiskPartition.DeviceID='$($partition.DeviceID)'} WHERE AssocClass = Win32_LogicalDiskToPartition\"\n" +
                    "    Get-WmiObject -Query $drives | ForEach-Object {\n" +
                    "      $DiskResult = New-Object -TypeName PSObject\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name PartitionDiskIndex -Value $partition.DiskIndex\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name DriveLetter -Value $_.DeviceID\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name DiskModel -Value $disk.Model\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name DiskSerialNumber -Value $disk.SerialNumber\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name DiskSystemName -Value $disk.SystemName\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name DiskMediaType -Value $disk.MediaType\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name DiskStatus -Value $disk.Status\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name DiskDeviceID -Value $disk.DeviceID\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name PartitionName -Value $partition.Name\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name PartitionType -Value $partition.Type\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name FileSystem -Value $_.FileSystem\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name DiskVolumeName -Value  $_.VolumeName\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name TotalSize -Value $_.Size\n" +
                    "      $DiskResult | Add-Member -MemberType NoteProperty -Name FreeSpace -Value $_.FreeSpace\n" +
                    "      $DiskResult\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

    public static final String NETWORK_DISK = "Get-WmiObject Win32_MappedLogicalDisk | Select DeviceId, FreeSpace, Size, SystemName, VolumeSerialNumber, VolumeName, ProviderName, FileSystem | Format-List";


    public static final String INSTALLED_SOFTWARE = "Get-ItemProperty HKLM:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\* |\n" +
            "    Select DisplayName, DisplayVersion, Publisher, InstallDate |\n" +
            "    WHERE {$_.DisplayName -ne $Null} | Format-List";

    public static final String PROCESS = "Get-WmiObject Win32_Process |\n" +
            "    Select Handles, ProcessId, ProcessName, WS, Path, Description, CommandLine | Format-List";

    public static final String TIMEZONE = "Get-WmiObject Win32_TimeZone |\n" +
            "    Select StandardName, Caption, DaylightName | Format-List";

    public static final String SCHEDULE_TASK = "schtasks /query /fo list /v";

    public static final String LOCAL_USER = "Get-WmiObject Win32_UserAccount |\n" +
            "    Select Name, Disabled, Description | Format-List";

    public static final String LOCAL_GROUP = "Get-WmiObject Win32_Group |\n" +
            "    Select Name, Description | Format-List";

    public static final String LOCAL_GROUP_USER = "Get-WmiObject Win32_GroupUser |\n" +
            "Select GroupComponent, PartComponent | format-List";

}
//end of PowerShellVerion2UnderCommand.java