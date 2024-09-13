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
 * Dong-Heon Han    Dec 16, 2021		First Draft.
 */

package io.playce.roro.common.dto.info;

import io.playce.roro.common.code.Domain1013;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Getter @ToString
@RequiredArgsConstructor
@Slf4j
public class LinuxInfo {
    public enum INFO {
        uname, ID_LIKE, VERSION_ID, ID
    }

    private final Domain1013 inventoryDetailTypeCode;
    private final String idLike;
    private final String version;
    private final String id;
    private String like;
    private String versionOnly;

    public String idOnly;

    public String getLike() {
        if(like == null) {
            like = idLike.replaceAll(INFO.ID_LIKE.name() + "=\"?([a-z ]+)\"?", "$1").replaceAll(StringUtils.SPACE, "_");
            log.debug("like: {}", like);
        }
        return like;
    }

    public String getIdOnly() {
        if(idOnly == null && id.length() > INFO.ID.name().length()) {
            String v = id.substring(INFO.ID.name().length());
            v = v.replaceAll("\\.", StringUtils.EMPTY);
            if(v.startsWith("=\"")) {
                idOnly = v.substring(2, v.length() - 1);
            } else {
                idOnly = v.substring(1);
            }
            log.debug("id: {}", idOnly);
        }
        return idOnly;
    }

    public String getVersionOnly() {
        if(versionOnly == null && version.length() > INFO.VERSION_ID.name().length()) {
            String v = version.substring(INFO.VERSION_ID.name().length());
            v = v.replaceAll("\\.", StringUtils.EMPTY);
            if(v.startsWith("=\"")) {
                versionOnly = v.substring(2, v.length() - 1);
            } else {
                versionOnly = v.substring(1);
            }
            log.debug("version id: {}", versionOnly);
        }
        return versionOnly;
    }
}