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
 * Dong-Heon Han    Feb 13, 2022		First Draft.
 */

package io.playce.roro.mw.asmt.tomcat.enums;

import io.playce.roro.mw.asmt.util.GetInfoStrategy;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
public enum CONFIG_FILES {
    TOMCAT_CONFIG_SERVER("server.xml", "conf"),
    TOMCAT_CONFIG_CONTEXT("context.xml", "conf"),
    TOMCAT_CONFIG_SETENV("setenv.sh", "bin"),
    TOMCAT_CONFIG_ENV("env.sh", "bin");

    private final String filename;
    private final String directory;

    CONFIG_FILES(String filename, String directory) {
        this.filename = filename;
        this.directory = directory;
    }

    public String filename() {
        return filename;
    }

    public String path(String root, GetInfoStrategy strategy) {
        String file = filename;
        if(strategy.isWindows()) {
            file = file.replaceAll("sh", "bat");
        }
        return root + strategy.getSeparator() + directory + strategy.getSeparator() + file;
    }
}