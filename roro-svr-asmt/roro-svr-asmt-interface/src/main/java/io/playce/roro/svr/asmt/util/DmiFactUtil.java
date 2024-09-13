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
package io.playce.roro.svr.asmt.util;

import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
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
public class DmiFactUtil {

    private static final String DMI_DE_CODE = "dmidecode -s %s";
    private static final String DEVICE_PATH = "/sys/devices/virtual/dmi/id/%s";

    public static String getAttributeByDmidecode(TargetHost targetHost, String attribute) throws InterruptedException {
        String output = SSHUtil.executeCommand(targetHost, String.format(DMI_DE_CODE, attribute));

        String result = "NA";
        if (StringUtils.isNotEmpty(output)) {
            StringBuilder sb = new StringBuilder();
            for (String line : output.split("\n")) {
                if (sb.length() > 1) {
                    sb.append(" ");
                }
                sb.append(line);
            }
            result = sb.toString();
        }

        return result.trim();
    }

    public static String getAttributeByFile(TargetHost targetHost, String attribute) throws InterruptedException {
        String exist = SSHUtil.executeCommand(targetHost, "sudo find " + String.format(DEVICE_PATH, "product_name"));

        String result = "NA";
        if (StringUtils.isNotEmpty(exist)) {
            String output = SSHUtil.executeCommand(targetHost, "sudo cat " + String.format(DEVICE_PATH, attribute));
            if (StringUtils.isNotEmpty(output)) {
                result = output;
            }
        }
        return result.trim();
    }

}
//end of DMIFactUtil.java