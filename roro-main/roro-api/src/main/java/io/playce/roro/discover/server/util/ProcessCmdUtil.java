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
 * Hoon Oh       1월 28, 2022            First Draft.
 */
package io.playce.roro.discover.server.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class ProcessCmdUtil {

    public static String getJavaPath(List<String> commands) {
        for (int i = 0; i < commands.size(); i++) {
            String param = commands.get(i);

            if (param.charAt(0) != '-' && param.contains("java")) {
                return param;
            }
        }
        return null;
    }

    public static String getParam(List<String> commands, String str) {
        for (String param : commands) {
            if (param.contains(str)) {
                String[] keyValue = param.split("=");
                if (keyValue.length < 2)
                    return StringUtils.EMPTY;

                return StringUtils.strip(keyValue[1], "\"");
            }
        }
        return StringUtils.EMPTY;
    }

    public static int getIndexFromString(List<String> commands, String str) {
        for (int i = 0; i < commands.size(); i++) {
            String param = commands.get(i);

            if (param.equals(str)) {
                return i;
            }
        }

        return -1;
    }

    public static String getTemporaryName(String solution, String svrName) {
        return solution + "-" + svrName;
    }
}
//end of ProcessCmdUtil.java