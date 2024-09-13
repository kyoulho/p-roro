/*
 * Copyright 2023 The playce-roro-k8s-assessment Project.
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
 * Dong-Heon Han    Jul 13, 2023		First Draft.
 */

package io.playce.roro.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;

@Slf4j
public class K8SCommandUtil {
    public static String execute(String command) {
        try {
            log.debug("execute: {}", command);
            CommandLine commandLine = CommandLine.parse(command);
            return K8SCommandUtil.execute(commandLine);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private static String execute(CommandLine commandLine) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DefaultExecutor executor = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            int exitCode = executor.execute(commandLine);

            if (exitCode != 0) {
                throw new Exception(baos.toString());
            }

            String result = baos.toString().trim();
            log.debug("result size: {} bytes", result.length());

            return result;
        }
    }
}