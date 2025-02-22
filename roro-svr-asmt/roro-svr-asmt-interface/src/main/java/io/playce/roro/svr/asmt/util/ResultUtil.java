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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Dong-Heon Han    Jun 29, 2022		First Draft.
 */

package io.playce.roro.svr.asmt.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
public class ResultUtil {
    public static List<String> removeCommentLine(String result, String delimiter, char commentChar) {
        return Arrays.stream(result.split(delimiter)).filter(u -> {
            u = StringUtils.strip(u);
            return !StringUtils.isEmpty(u) && u.charAt(0) != commentChar;
        }).collect(Collectors.toList());
    }

    public static List<String> removeCommentLine(String result, char commentChar) {
        return removeCommentLine(result, "\n", commentChar);
    }

    public static List<String> removeCommentLine(String result) {
        return removeCommentLine(result, '#');
    }
}