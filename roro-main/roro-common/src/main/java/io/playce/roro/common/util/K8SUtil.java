/*
 * Copyright 2023 The playce-roro Project.
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
 * Dong-Heon Han    Jul 18, 2023		First Draft.
 */

package io.playce.roro.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
public class K8SUtil {

    /**
     * 해당 패스 파일을 삭제한다.
     * @param filePath
     */
    public static void deleteFile(String filePath) {
        try {
            Files.delete(Path.of(filePath));
        } catch (IOException e) {
            log.error("An error occurred while deleting the file.. message: {}", e.getMessage(), e);
        }
    }

    /**
     * Temp directory에 config파일을 작성한다.
     * @param config
     * @return
     */
    public static String writeHomeKube(String config) {
        String temp = System.getProperty("java.io.tmpdir");
        log.debug("temp dir: {}", temp);
        Path kube = Path.of(temp, UUID.randomUUID().toString());
        boolean result = writeFile(kube, config);
        if(result) {
            return kube.toString();
        }
        log.error("Error writing kube config file.");
        return null;
    }

    private static boolean writeFile(Path kube, String config) {
        try {
            Files.deleteIfExists(kube);
            Files.write(kube, config.getBytes());
            return true;
        } catch (IOException e) {
            log.error("An error occurred while creating the file. message: {}", e.getMessage(), e);
            return false;
        }
    }
}