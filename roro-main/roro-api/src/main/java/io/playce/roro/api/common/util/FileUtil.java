/*
 * Copyright 2020 The Playce-RORO Project.
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
 * Jaeeon Bae       10월 08, 2020            First Draft.
 */
package io.playce.roro.api.common.util;

import io.playce.roro.common.property.CommonProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 1.0
 */
@Slf4j
public class FileUtil {

    public static String getEncodeFileName(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Gets upload file path.
     *
     * @param subDirectory
     *
     * @return
     */
    public static String getUploadFilePath(String subDirectory) {
        return CommonProperties.getWorkDir() + subDirectory;
    }

    /**
     * save File.
     *
     * @param multipartFile
     * @param name
     * @param subDirectories
     *
     * @return
     *
     * @throws IOException
     */
    public static String saveFile(MultipartFile multipartFile, String name, String... subDirectories) throws IOException {
        try {
            File path = Paths.get(CommonProperties.getWorkDir(), subDirectories).toFile();
            if (!path.exists()) {
                path.mkdirs();
            }
            File target = Paths.get(path.getAbsolutePath(), name).toFile();
            multipartFile.transferTo(target);
            return target.getAbsolutePath();
        } catch (IOException e) {
            log.error("MultipartFile write failed.", e);
            throw e;
        }
    }

    public static String saveFileFromURL(String remotePath, String fileName, String... subDirectories) throws IOException {
        try {
            URL url = new URL(remotePath);
            File path = Paths.get(CommonProperties.getWorkDir(), subDirectories).toFile();
            if (!path.exists()) {
                path.mkdirs();
            }
            File target = Paths.get(path.getAbsolutePath(), fileName).toFile();
            FileUtils.copyURLToFile(url, target);
            return target.getAbsolutePath();
        } catch (IOException e) {
            log.error("Save  write failed.", e);
            throw e;
        }
    }
}
//end of FileUtil.java