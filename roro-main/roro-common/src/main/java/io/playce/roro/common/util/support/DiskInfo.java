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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       1월 04, 2021            First Draft.
 */
package io.playce.roro.common.util.support;

import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 2.0.0
 */
public class DiskInfo {

    /**
     * /boot 로 mount 된 값들은 duplicated = 'Y'로 설정
     * device 가 같은 경우 duplicated 로 볼 겻인지 확인 필요.
     *
     * @param diskInfoMap
     *
     * @return
     */
    public static Map<String, Object> generatedDuplicated(Map<String, Object> diskInfoMap) {

        for (String mount : diskInfoMap.keySet()) {
            String duplicated = "N";
            Map<String, String> map = (Map<String, String>) diskInfoMap.get(mount);

            if (mount != null && (mount.equals("/boot") || mount.startsWith("/boot/"))) {
                duplicated = "Y";
            }

            map.put("duplicated", duplicated);
            diskInfoMap.put(mount, map);
        }

        return diskInfoMap;
    }

    /*public static Map<String, Object> generateWindows(List<Disk> disks) {
        Map<String, Object> diskMap = new HashMap<>();

        for (Disk disk : disks) {
            diskMap.put(disk.getDriveLetter(), disk);
        }

        return diskMap;
    }*/

    // public static void main(String[] args) {
    //     Map<String, Object> diskMap = new LinkedHashMap<>();
    //     Map<String, String> map = new LinkedHashMap<>();
    //     map.put("device", "/dev/sda1");
    //     map.put("fstype", "xfs");
    //     map.put("size", "1014M");
    //     map.put("free", "866M");
    //     diskMap.put("/boot", map);
    //
    //     map = new LinkedHashMap<>();
    //     map.put("device", "/dev/sdb1");
    //     map.put("fstype", "ext4");
    //     map.put("size", "50G");
    //     map.put("free", "11G");
    //     diskMap.put("/data", map);
    //
    //     map = new LinkedHashMap<>();
    //     map.put("device", "/dev/mapper/centos-root");
    //     map.put("fstype", "xfs");
    //     map.put("size", "17G");
    //     map.put("free", "7.3G");
    //     diskMap.put("/", map);
    //
    //     generatedDuplicated(diskMap);
    // }
}
//end of DiskInfo.java