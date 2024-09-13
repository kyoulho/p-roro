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

import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;

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
public class PowerShellVersion3OverCommand {

    public static final String SYSTEM = "systeminfo /fo CSV | ConvertFrom-Csv | ConvertTo-Json";

    // 환경변수
    public static final List<String> ENVIRONMENT = Arrays.asList(
            "$Result = [Ordered]@{}",
            "Get-ChildItem env: | %{$Result.Add($_.Name,$_.Value)}",
            "$Result | ConvertTo-Json");

    // CPU
    public static final String CPU = "Get-WmiObject Win32_Processor | Select Name, Caption, NumberOfCores, NumberOfLogicalProcessors, MaxClockSpeed | ConvertTo-Json";

    // 네트워크
    public static final String NETWORK = "NetIPConfiguration -ALL | ForEach-Object {\n" +
            "New-Object -Type pscustomobject -Property @{\n" +
            "  \"iIndex\" = $_.InterfaceIndex \n" +
            "  \"iAlias\" = $_.InterfaceAlias\n" +
            "  \"iDesc\" = $_.InterfaceDescription\n" +
            "  \"IPv4\" = $_.IPv4Address.IpAddress\n" +
            "  \"IPv6\" = $_.IPv6Address.IpAddress\n" +
            "  \"IPv4DG\" = $_.IPv4DefaultGateway.NextHop\n" +
            "  \"IPv6DG\" = $_.IPv6DefaultGateway.NextHop\n" +
            // "  \"DNS\" = $_.DNSServer.ServerAddresses -split \" \" -join \"<br>\"\n" +
            "  \"Mac\" = $_.NetAdapter.MACAddress\n" +
            "  \"Status\" = $_.NetAdapter.Status\n" +
            "}\n" +
            "} | ConvertTo-Json";

    public static final String NETWORK_NAME = "Get-WmiObject Win32_NetworkAdapter -Filter \"InterfaceIndex = %s\" | Select NetConnectionID | ConvertTo-Json ";

    // Dns
    public static final String DNS = "Get-DnsClientServerAddress -AddressFamily IPv4 | Select InterfaceAlias, InterfaceIndex, AddressFamily, ServerAddresses | ConvertTo-Json";

    // Routing Table
    public static final String ROUTE = "Get-NetRoute -AddressFamily IPv4, IPv6 | \n" +
            "    Select DestinationPrefix, NextHop, RouteMetric, ifIndex,\n" +
            "    @{Name='AddressFamily';Expression={$_.AddressFamily.toString()}} | ConvertTo-Json";

    //Hosts File
    public static final String HOSTS = "type C:\\Windows\\System32\\drivers\\etc\\hosts";

    // 포트 번호
    public static final String PORT =
            "netstat -ano | Where-Object{$_ -match 'TCP'} | ForEach-Object{\n" +
                    "    $split = $_.Trim() -split \"\\s+\"\n" +
                    "    try {\n" +
                    "        $processInfo = $(Get-Process -Id $split[-1] -ErrorAction SilentlyContinue)\n" +
                    "    } catch {\n" +
                    "        $processInfo = $Null\n" +
                    "    }\n" +
                    "    New-Object -Type pscustomobject -Property @{\n" +
                    "        \"Protocol\" = $split[0]\n" +
                    "        \"LocalAddress\" = $split[1]\n" +
                    "        \"RemoteAddress\" = $split[2]\n" +
                    "        \"State\" = if($split[3] -notmatch \"\\d+\"){$split[3]} else{\"\"}\n" +
                    "        \"PID\" = if($processInfo -ne $Null) {$processInfo.Id} else {\"\"}\n" +
                    "        \"ProcessName\" = if($processInfo -ne $Null) {$processInfo.ProcessName} else {\"\"}\n" +
                    "    }\n" +
                    "} | Select \"Protocol\", \"LocalAddress\", \"RemoteAddress\", \"State\", \"PID\", \"ProcessName\" | ConvertTo-Json";

    // Firewall
    public static final String FIREWALL =
            "Get-NetFirewallRule|ForEach-Object {\n" +
                    "$portFilter = $_ | Get-NetFirewallPortFilter\n" +
                    "New-Object -Type PSCustomObject -Property @{\n" +
                    " NM = $_.Name\n" +
                    " DP = $_.DisplayName\n" +
                    " DS = $_.Description\n" +
                    " PR = $portFilter.Protocol\n" +
                    " LP = $portFilter.LocalPort\n" +
                    " RP = $portFilter.RemotePort\n" +
                    " RA = ($_ | Get-NetFirewallAddressFilter).RemoteAddress\n" +
                    " EA = $_.Enabled.toString()\n" +
                    " DR = $_.Direction.toString()\n" +
                    " AT = $_.Action.toString()\n" +
                    "}\n" +
                    "} | ConvertTo-Json";

    // Disk
    public static final String DISK =
            "Get-WmiObject Win32_DiskDrive | ForEach-Object {\n" +
                    "  $disk = $_\n" +
                    "  $partitions = \"ASSOCIATORS OF {Win32_DiskDrive.DeviceID='$($disk.DeviceID)'} WHERE AssocClass = Win32_DiskDriveToDiskPartition\"\n" +
                    "  Get-WmiObject -Query $partitions | ForEach-Object {\n" +
                    "    $partition = $_\n" +
                    "    $drives = \"ASSOCIATORS OF {Win32_DiskPartition.DeviceID='$($partition.DeviceID)'} WHERE AssocClass = Win32_LogicalDiskToPartition\"\n" +
                    "    Get-WmiObject -Query $drives | ForEach-Object {\n" +
                    "      New-Object -Type PSCustomObject -Property @{\n" +
                    "       PartitionDiskIndex = $partition.DiskIndex\n" +
                    "       DriveLetter = $_.DeviceID\n" +
                    "       DiskModel = $disk.Model\n" +
                    "       DiskSerialNumber = $disk.SerialNumber\n" +
                    "       DiskSystemName = $disk.SystemName\n" +
                    "       DiskMediaType = $disk.MediaType\n" +
                    "       DiskStatus = $disk.Status\n" +
                    "       DiskDeviceID = $disk.DeviceID\n" +
                    "       PartitionName = $partition.Name\n" +
                    "       PartitionType = $partition.Type\n" +
                    "       FileSystem = $_.FileSystem\n" +
                    "       DiskVolumeName  = $_.VolumeName\n" +
                    "       TotalSize  = [math]::Round($_.Size/1GB,2).ToString() + \" GB\"\n" +
                    "       FreeSpace = [math]::Round($_.FreeSpace/1GB,2).ToString() + \" GB\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "} | ConvertTo-Json";

    public static final String NETWORK_DISK = "Get-WmiObject Win32_MappedLogicalDisk | Select DeviceId, FreeSpace, Size, SystemName, VolumeSerialNumber, VolumeName, ProviderName, FileSystem | ConvertTo-Json";

    public static final String INSTALLED_SOFTWARE =
            "Get-ItemProperty HKLM:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\* |\n" +
                    "    Select DisplayName, DisplayVersion, Publisher, InstallDate |\n" +
                    "    WHERE {$_.DisplayName -ne $Null} |\n" +
                    "    ConvertTo-Json";

    public static final String SERVICE =
            "Get-Service |\n" +
                    "    Select Name, DisplayName, ServiceName,\n" +
                    "    @{Name='ServiceType';Expression={$_.ServiceType.toString()}},\n" +
                    "    @{Name='StartType';Expression={$_.StartType.toString()}},\n" +
                    "    @{Name='Status';Expression={$_.Status.toString()}} |\n" +
                    "    ConvertTo-Json";

    public static final List<String> LOCAL_GROUP_USER = Arrays.asList(
            "$ComputerName = $env:COMPUTERNAME\n",
            "$Computer = [ADSI]\"WinNT://$Computername\"\n",
            "$Groups = $Computer.psbase.Children | Where {$_.psbase.schemaClassName -eq 'group'}\n",
            "$GroupAndUsers = Foreach ($Group in $Groups) {\n" +
                    "    New-Object -TypeName PSCustomObject -Property @{\n" +
                    "        Group = $Group.Name -join ''\n" +
                    "        Description = $Group.Description[0]\n" +
                    "        User = ($group.psbase.Invoke(\"Members\") | % { $_.gettype().InvokeMember(\"Name\",\"GetProperty\",$null,$_,$null)}) -join ','\n" +
                    "    } | Select-Object -Property Group, Description, User\n" +
                    "}\n",
            "$GroupAndUsers | ConvertTo-Json"
    );

    public static void main(String[] args) throws Exception {
        TargetHost targetHost = new TargetHost();

        // targetHost.setIpAddress("192.168.1.157"); // Windows 2012
        targetHost.setIpAddress("192.168.1.95"); // Windows 2012 R2
        // targetHost.setIpAddress("192.168.1.106"); // Windows 2016
        // targetHost.setIpAddress("192.168.1.107"); // Windows 2019
        targetHost.setPort(5985);
        targetHost.setUsername("Administrator");
        targetHost.setPassword("*****");

        String result1 = WinRmUtils.executePsShell(targetHost, PowerShellVersion3OverCommand.FIREWALL);

        WinRmUtils.executePsShell(targetHost, PowerShellVersion3OverCommand.FIREWALL + " |  Out-File C:\\TEMP\\roro\\firewall.txt -encoding UTF8");
        String result2 = WinRmUtils.executeCommand(targetHost, "type C:\\TEMP\\roro\\firewall.txt");

        WinRmUtils.executeCommand(targetHost, "del C:\\TEMP\\roro\\firewall.txt");

        System.err.println("Without Redirection : " + result1);
        System.err.println("================================");
        System.err.println("  With Redirection  : " + result2);
    }
}
//end of PowerShellVersion3OverCommand.java