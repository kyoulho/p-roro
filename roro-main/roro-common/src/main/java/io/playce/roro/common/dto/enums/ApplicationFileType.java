/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * SangCheon Park   Dec 07, 2021		    First Draft.
 */
package io.playce.roro.common.dto.enums;

import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public enum ApplicationFileType {

    /**
     * Source file application.
     */
    SOURCE_FILE("source", Arrays.asList("zip")),

    /**
     * Archive file application.
     */
    ARCHIVE_FILE("archive", Arrays.asList("ear", "war", "jar", "gz"));

    private String type;
    private List<String> supportList;

    ApplicationFileType(String type, List<String> supportList) {
        this.type = type;
        this.supportList = supportList;
    }

    /**
     * Is support boolean.
     *
     * @param type the type
     *
     * @return the boolean
     */
    public boolean isSupport(String type) {
        return supportList.stream().anyMatch(t -> t.equals(type));
    }
}
//end of AnalysisFileType.java
