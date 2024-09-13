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

import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 2.0.0
 */
public enum OSFamily {

    REDHAT("RedHat", Arrays.asList("RedHat", "Fedora", "CentOS", "Scientific", "SLC", "Ascendos", "CloudLinux", "PSBM", "OracleLinux",
            "OVS", "OEL", "Amazon", "Virtuozzo", "XenServer")),
    DEBIAN("Debian", Arrays.asList("Ubuntu", "Debian", "Rasbian")),
    SUSE("SuSE", Arrays.asList("SuSE", "SLES_SAP", "SLES", "SUSE_LINUX")),
    ARCH_LINUX("Archlinux", Arrays.asList("Manjaro", "Archlinux")),
    SOLARIS("Solaris", Arrays.asList("Solaris", "SmartOS", "OmniOS", "OpenIndiana")),
    GENTOO("Gentoo", Arrays.asList("Gentoo", "Funtoo")),
    AIX("AIX", Arrays.asList("AIX")),
    SLACKWARE("Slackware", Arrays.asList("Slackware")),
    ALPINE("Alpine", Arrays.asList("Alpine")),
    FREE_BSD("FreeBSD", Arrays.asList("FreeBSD")),
    HP_UX("HP-UX", Arrays.asList("HPUX")),
    MACOSX("MacOSX", Arrays.asList("Darwin"));

    private final String name;
    private final List<String> osKinds;

    OSFamily(String name, List<String> osKinds) {
        this.name = name;
        this.osKinds = osKinds;
    }

    public static String getFamily(String source) {
        for (OSFamily family : OSFamily.values()) {
            if (family.hasOs(source)) {
                return family.getName();
            }
        }
        return null;
    }

    public boolean hasOs(String os) {
        return osKinds.stream().anyMatch(m -> os.indexOf(m) > -1);
    }


    public String getName() {
        return name;
    }

    public List<String> getOsKinds() {
        return osKinds;
    }
}
//end of OSRelease.java