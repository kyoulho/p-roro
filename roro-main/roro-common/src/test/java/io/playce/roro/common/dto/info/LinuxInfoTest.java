package io.playce.roro.common.dto.info;

import io.playce.roro.common.code.Domain1013;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 *
 * Author			Date				Description
 * ---------------	----------------	------------
 * Dong-Heon Han    Feb 10, 2022		First Draft.
 */

class LinuxInfoTest {
    @Test
    void getIdLikeCentos() {
        LinuxInfo linuxInfo = new LinuxInfo(Domain1013.LINUX, "ID_LIKE=\"rhel fedora\"", "VERSION_ID=\"8\"", "ID=\"centos\"");
        assertEquals(linuxInfo.getLike(), "rhel_fedora");
    }
    @Test
    void getVersionCentos() {
        LinuxInfo linuxInfo = new LinuxInfo(Domain1013.LINUX, "ID_LIKE=\"rhel fedora\"", "VERSION_ID=\"8\"", "ID=\"centos\"");
        assertEquals(linuxInfo.getVersionOnly(), "8");
    }
    @Test
    void getIdnCentos() {
        LinuxInfo linuxInfo = new LinuxInfo(Domain1013.LINUX, "ID_LIKE=\"rhel fedora\"", "VERSION_ID=\"8\"", "ID=\"centos\"");
        assertEquals(linuxInfo.getIdOnly(), "centos");
    }

    @Test
    void getIdLikeUbuntu() {
        LinuxInfo linuxInfo = new LinuxInfo(Domain1013.LINUX, "ID_LIKE=\"debian\"", "VERSION_ID=\"21.10\"", "ID=\"ubuntu\"");
        assertEquals(linuxInfo.getLike(), "debian");
    }
    @Test
    void getVersionUbuntu() {
        LinuxInfo linuxInfo = new LinuxInfo(Domain1013.LINUX, "ID_LIKE=\"debian\"", "VERSION_ID=\"21.10\"", "ID=\"ubuntu\"");
        assertEquals(linuxInfo.getVersionOnly(), "2110");
    }
    @Test
    void getIdnUbuntu() {
        LinuxInfo linuxInfo = new LinuxInfo(Domain1013.LINUX, "ID_LIKE=\"debian\"", "VERSION_ID=\"21.10\"", "ID=\"ubuntu\"");
        assertEquals(linuxInfo.getIdOnly(), "ubuntu");
    }

    @Test
    void getVersionDebian() {
        LinuxInfo linuxInfo = new LinuxInfo(Domain1013.LINUX, "", "VERSION_ID=\"9\"", "ID=debian");
        assertEquals(linuxInfo.getVersionOnly(), "9");
    }
    @Test
    void getIdLikeDebian() {
        LinuxInfo linuxInfo = new LinuxInfo(Domain1013.LINUX, "", "VERSION_ID=\"9\"", "ID=debian");
        assertEquals(linuxInfo.getLike(), "");
    }
    @Test
    void getIdnDebian() {
        LinuxInfo linuxInfo = new LinuxInfo(Domain1013.LINUX, "", "VERSION_ID=\"9\"", "ID=debian");
        assertEquals(linuxInfo.getIdOnly(), "debian");
    }
}