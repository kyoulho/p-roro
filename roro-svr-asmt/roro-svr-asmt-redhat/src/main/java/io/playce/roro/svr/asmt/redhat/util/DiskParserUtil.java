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
 * Hoon Oh       11월 23, 2021            First Draft.
 */
package io.playce.roro.svr.asmt.redhat.util;

import io.playce.roro.svr.asmt.dto.redhat.LogicalVolume;
import io.playce.roro.svr.asmt.dto.redhat.PhysicalVolume;
import io.playce.roro.svr.asmt.dto.redhat.VolumeGroup;
import io.playce.roro.svr.asmt.redhat.common.Constants;

import java.util.Map;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class DiskParserUtil {

    public static void parsePhysicalVolumeDetail(Map<String, VolumeGroup> vgs, PhysicalVolume pvInfo, String line) {
        String value;
        if (line.contains(Constants.PV_NAME)) {
            value = line.replaceAll(Constants.PV_NAME, "").strip();
            pvInfo.setPvName(value);
        } else if (line.contains(Constants.VG_NAME)) {
            value = line.replaceAll(Constants.VG_NAME, "").strip();
            pvInfo.setVgName(value);
            vgs.get(value).getPvs().add(pvInfo);
        } else if (line.contains(Constants.PV_SIZE)) {
            value = line.replaceAll(Constants.PV_SIZE, "").strip();
            pvInfo.setPvSize(value);
        } else if (line.contains(Constants.ALLOCATABLE)) {
            value = line.replaceAll(Constants.ALLOCATABLE, "").strip();
            pvInfo.setAllocatable(value);
        } else if (line.contains(Constants.PE_SIZE)) {
            value = line.replaceAll(Constants.PE_SIZE, "").strip();
            pvInfo.setPeSize(value);
        } else if (line.contains(Constants.TOTAL_PE)) {
            value = line.replaceAll(Constants.TOTAL_PE, "").strip();
            pvInfo.setTotalPe(value);
        } else if (line.contains(Constants.FREE_PE)) {
            value = line.replaceAll(Constants.FREE_PE, "").strip();
            pvInfo.setFreePe(value);
        } else if (line.contains(Constants.ALLOCATED_PE)) {
            value = line.replaceAll(Constants.ALLOCATED_PE, "").strip();
            pvInfo.setAllocatedPe(value);
        } else if (line.contains(Constants.PV_UUID)) {
            value = line.replaceAll(Constants.PV_UUID, "").strip();
            pvInfo.setPvUuid(value);
        }
    }

    public static void parseLogicalVolumeDetail(Map<String, VolumeGroup> vgs, LogicalVolume lvInfo, String line) {
        String value;
        if (line.contains(Constants.LV_PATH)) {
            value = line.replaceAll(Constants.LV_PATH, "").strip();
            lvInfo.setLvPath(value);
        } else if (line.contains(Constants.LV_NAME)) {
            value = line.replaceAll(Constants.LV_NAME, "").strip();
            lvInfo.setLvName(value);
        } else if (line.contains(Constants.VG_NAME)) {
            value = line.replaceAll(Constants.VG_NAME, "").strip();
            lvInfo.setVgName(value);
            vgs.get(value).getLvs().add(lvInfo);
        } else if (line.contains(Constants.LV_UUID)) {
            value = line.replaceAll(Constants.LV_UUID, "").strip();
            lvInfo.setLvUuid(value);
        } else if (line.contains(Constants.LV_SIZE)) {
            value = line.replaceAll(Constants.LV_SIZE, "").strip();
            lvInfo.setLvSize(value);
        }
    }
}
//end of DiskParserUtil.java