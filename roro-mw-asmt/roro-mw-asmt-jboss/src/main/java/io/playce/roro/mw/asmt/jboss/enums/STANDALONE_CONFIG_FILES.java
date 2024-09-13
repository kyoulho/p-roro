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
 * Jhpark       8월 05, 2022            First Draft.
 */
package io.playce.roro.mw.asmt.jboss.enums;

import io.playce.roro.mw.asmt.util.GetInfoStrategy;

/**
 * <pre>
 *
 * </pre>
 *
 * @author jhpark
 * @version 3.0
 */
public enum STANDALONE_CONFIG_FILES {
    JBOSS_STANDALONE_XML("standalone.xml", "configuration"),
    JBOSS_STANDALONE_SETUP_ENV("env.sh", "bin"),

    JBOSS_WEB_XML("jboss-web.xml", "WEB-INF");

    private final String filename;
    private final String directory;

    STANDALONE_CONFIG_FILES(String filename, String directory) {
        this.filename = filename;
        this.directory = directory;
    }

    public String filename() {
        return filename;
    }

    public String directory() {return directory;}

    public String path(String root, GetInfoStrategy strategy) {
        String file = filename;
        if(strategy.isWindows()) {
            file = file.replaceAll("sh", "bat");
        }
        return root + strategy.getSeparator() + directory + strategy.getSeparator() + file;
    }
}