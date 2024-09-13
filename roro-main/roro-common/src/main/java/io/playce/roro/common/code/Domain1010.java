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
 * Hoon Oh       1월 30, 2022            First Draft.
 */
package io.playce.roro.common.code;

import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public enum Domain1010 {
    AUTO("Automatic"), MAN("Manual"); //, DIS("Disabled");
    private final String fullname;

    Domain1010(String fullname) {
        this.fullname = fullname;
    }

    public String fullname() {
        return fullname;
    }

    public static Domain1010 findBy(String arg) {
        for (Domain1010 domain1010 : values()) {
            if (StringUtils.equals(domain1010.fullname(), arg)) {
                return domain1010;
            }
        }

        return null;
    }
}