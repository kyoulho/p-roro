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
 * Author			Date				Description
 * ---------------	----------------	------------
 * SangCheon Park   Feb 22, 2021		First Draft.
 */
package io.playce.roro.common.util;

import io.playce.roro.common.exception.RoRoException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 2.0.0
 */
@Slf4j
public class ManifestUtil {

    /**
     * Gets description.
     *
     * @param file the file
     *
     * @return the description
     */
    public static String getDescription(File file) throws InterruptedException {
        String description = null;

        try {
            JarFile jar = new JarFile(file);
            Manifest manifest = jar.getManifest();

            if (manifest != null) {
                Attributes attributes = manifest.getMainAttributes();
                if (attributes != null) {
                    java.util.Iterator it = attributes.keySet().iterator();

                    while (it.hasNext()) {
                        Attributes.Name key = (Attributes.Name) it.next();
                        String keyword = key.toString();
                        if (keyword.equals("Bundle-Description")) {
                            description = (String) attributes.get(key);
                            break;
                        }
                    }

                    if (description == null) {
                        it = attributes.keySet().iterator();
                        while (it.hasNext()) {
                            Attributes.Name key = (Attributes.Name) it.next();
                            String keyword = key.toString();
                            if (keyword.equals("Specification-Title")) {
                                description = (String) attributes.get(key);
                                break;
                            }
                        }
                    }

                    if (description == null) {
                        it = attributes.keySet().iterator();
                        while (it.hasNext()) {
                            Attributes.Name key = (Attributes.Name) it.next();
                            String keyword = key.toString();
                            if (keyword.equals("Comment")) {
                                description = (String) attributes.get(key);
                                break;
                            }
                        }
                    }

                    if (description == null) {
                        it = attributes.keySet().iterator();
                        while (it.hasNext()) {
                            Attributes.Name key = (Attributes.Name) it.next();
                            String keyword = key.toString();
                            if (keyword.equals("Implementation-Title")) {
                                description = (String) attributes.get(key);
                                break;
                            }
                        }
                    }
                }
            }

            if (description != null) {
                description = description.replaceAll("       ", " ");
                description = description.replaceAll("      ", " ");
                description = description.replaceAll("     ", " ");
                description = description.replaceAll("    ", " ");
                description = description.replaceAll("   ", "");
                description = description.replaceAll("  ", "");
            }

            jar.close();
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.warn("Unhandled exception occurred while parse MANIFEST.MF file. [{}]", e.getMessage());
        }

        return description;
    }
}
//end of ManifestUtil.java