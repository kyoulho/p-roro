/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Hoon Oh       2월 08, 2022            First Draft.
 */
package io.playce.roro.discover.server.util;

import com.google.gson.Gson;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class ServerSummaryUtil {
    public static String getCpuModel(ServerAssessmentResult result) {
        return StringUtils.defaultString(result.getCpu().getProcessor());
    }

    public static String getCpuArchitecture(ServerAssessmentResult result) {
        return StringUtils.defaultString(result.getArchitecture());
    }

    public static Integer getCpuCoreCount(ServerAssessmentResult result) {
        if (result.getCpu() != null) {
            String processorCores = result.getCpu().getProcessorCores();
            if (StringUtils.isNotEmpty(processorCores)) {
                return Integer.parseInt(processorCores.trim().split(StringUtils.SPACE)[0]);
            }
        }
        return null;
    }

    public static Integer getCpuSocketCount(ServerAssessmentResult result) {
        if (result.getCpu() != null) {
            String processorCount = result.getCpu().getProcessorCount();
            if (StringUtils.isNotEmpty(processorCount)) {
                return Integer.parseInt(processorCount.trim().split(StringUtils.SPACE)[0]);
            }
        }
        return null;
    }

    public static String getOsFamily(ServerAssessmentResult result) {
        return StringUtils.defaultString(result.getFamily());
    }

    public static String getOsKernel(ServerAssessmentResult result) {
        return StringUtils.defaultString(result.getKernel());
    }

    public static String getOsName(ServerAssessmentResult result) {
        String distributionRelease = result.getDistributionRelease();
        if (StringUtils.isNotEmpty(distributionRelease)) {
            return distributionRelease;
        }
        return null;
    }

    public static String getVendorName(ServerAssessmentResult result) {
        return StringUtils.defaultString(result.getSystemVendor());
    }

    public static Integer getMemSize(ServerAssessmentResult result) {
        if (result.getMemory() != null) {
            String memToToalMb = result.getMemory().getMemTotalMb();
            if (StringUtils.isNotEmpty(memToToalMb)) {
                return Integer.parseInt(memToToalMb);
            }
        }
        return null;
    }

    public static Integer getSwapSize(ServerAssessmentResult result) {
        if (result.getMemory() != null) {
            String swapTotalMb = result.getMemory().getSwapTotalMb();
            if (StringUtils.isNotEmpty(swapTotalMb)) {
                return Integer.parseInt(swapTotalMb);
            }
        }
        return null;
    }

    public static String getUserGroupConfigJson(Gson gson, ServerAssessmentResult result) {
//        Gson gson = new Gson();
        // Map<String , Object> map = new HashMap<>();
        // map.put("users",result.getUsers());
        // map.put("groups",result.getGroups());

        return gson.toJson(result.getDefInfo());
    }
}
//end of ServerSummaryUtil.java