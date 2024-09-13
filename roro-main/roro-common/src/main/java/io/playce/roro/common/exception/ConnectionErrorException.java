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
 * SangCheon Park   Aug 18, 2022		    First Draft.
 */
package io.playce.roro.common.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class ConnectionErrorException extends RuntimeException {

    public ConnectionErrorException(String message) {
        super(getMessage(message));
    }

    public ConnectionErrorException(Throwable cause) {
        super(cause);
    }

    public ConnectionErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    private static String getMessage(String message) {
        if (StringUtils.isNotEmpty(message) && message.startsWith("java.net")) {
            int idx = message.indexOf(":");

            if (idx > 0) {
                message = message.substring(idx + 2);
            }
        }

        return message;
    }
}