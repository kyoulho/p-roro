/*
 * Copyright 2020 The Playce-RoRo Project.
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
 * Hoon Oh          10월 26, 2020		First Draft.
 */
package io.playce.roro.api.common.error.exception;

import io.playce.roro.api.common.error.ErrorCode;
import lombok.Getter;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 2.0.0
 */
public class RoRoApiException extends RuntimeException {

    private static final long serialVersionUID = 6396373282928286452L;

    @Getter
    private ErrorCode errorCode;
    @Getter
    private String[] parameters;

    /**
     * Instantiates a new Ro ro common exception.
     */
    public RoRoApiException() {
    }

    /**
     * Instantiates a new Ro ro common exception.
     *
     * @param errorCode the error code
     */
    public RoRoApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Instantiates a new Ro ro common exception.
     *
     * @param errorCode the error code
     * @param message   the message
     */
    public RoRoApiException(ErrorCode errorCode, String message) {
        super(message);
        this.parameters = new String[]{message};
        this.errorCode = errorCode;
    }

    public RoRoApiException(ErrorCode errorCode, String... parameters) {
        super("Exception...");
        this.parameters = parameters;
        this.errorCode = errorCode;
    }

}
//end of RoRoCommonException.java