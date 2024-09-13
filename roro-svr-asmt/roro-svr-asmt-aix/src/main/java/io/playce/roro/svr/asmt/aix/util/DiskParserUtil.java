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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       11월 23, 2021            First Draft.
 */
package io.playce.roro.svr.asmt.aix.util;

import io.playce.roro.svr.asmt.aix.common.Constants;
import io.playce.roro.svr.asmt.dto.aix.FileSystem;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class DiskParserUtil {

    public static void parseFileSystemDetail(FileSystem fileSystem, String[] info) {
        if (info[0].equals(Constants.FILESYSTEM_ATTR_CHECK)) {
            fileSystem.setCheck(info[1]);
        } else if (info[0].equals(Constants.FILESYSTEM_ATTR_DEV)) {
            fileSystem.setDev(info[1]);
        } else if (info[0].equals(Constants.FILESYSTEM_ATTR_FREE)) {
            fileSystem.setFree(info[1]);
        } else if (info[0].equals(Constants.FILESYSTEM_ATTR_LOG)) {
            fileSystem.setLog(info[1]);
        } else if (info[0].equals(Constants.FILESYSTEM_ATTR_MOUNT)) {
            fileSystem.setMount(info[1]);
        } else if (info[0].equals(Constants.FILESYSTEM_ATTR_TYPE)) {
            fileSystem.setType(info[1]);
        } else if (info[0].equals(Constants.FILESYSTEM_ATTR_VFS)) {
            fileSystem.setVfs(info[1]);
        } else if (info[0].equals(Constants.FILESYSTEM_ATTR_VOL)) {
            fileSystem.setVol(info[1]);
        }
    }

}
//end of DiskParserUtil.java