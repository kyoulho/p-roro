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
 * Hoon Oh          10월 13, 2021		First Draft.
 */
package io.playce.roro.svr.asmt;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 2.0.0
 */
public enum OSRelease {

    ORACLE_LINUX("/etc/oracle-release", "OracleLinux"),
    SLACKWARE("/etc/slackware-version", "Slackware"),
    RED_HAT("/etc/redhat-release", "RedHat"),
    VMWARE_ESX("/etc/vmware-release", "VMwareESX"),
    OPEN_WRT("/etc/openwrt_release", "OpenWrt"),
    AMAZON("/etc/system-release", "Amazon"),
    ALPINE("/etc/alpine-release", "Alpine"),
    ARCH_LINUX("/etc/arch-release", "Archlinux"),
    SUSE("/etc/SuSE-release", "SuSE"),
    GENTOO("/etc/gentoo-release", "Gentoo"),
    DEBIAN("/etc/os-release", "Debian"),
    MANDRIVA("/etc/lsb-release", "Mandriva"),
    ALT_LINUX("/etc/altlinux-release", "Altlinux"),
    SMGL("/etc/sourcemage_release", "SMGL"),
    CORE_OS("/etc/coreos/update.conf", "Coreos"),
    CLEAR_LINUX("/usr/lib/os-release", "ClearLinux"),
    NA("/etc/os_release", "NA");

    private final String path;
    private final String name;

    OSRelease(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
//end of OSRelease.java