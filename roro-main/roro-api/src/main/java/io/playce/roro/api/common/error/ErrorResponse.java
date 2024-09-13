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
 * Jeongho Baek     10월 21, 2020        First Draft.
 */
package io.playce.roro.api.common.error;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 1.0
 */
@Getter
@NoArgsConstructor
public class ErrorResponse {

    /**
     * The Code.
     */
    private String code;
    /**
     * The Message.
     */
    private String message;

    /**
     * Instantiates a new Error response.
     *
     * @param errorCode the error code
     */
    private ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getConvertedMessage();
    }

    /**
     * Instantiates a new Error response.
     *
     * @param code    the code
     * @param message the message
     */
    private ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Of error response.
     *
     * @param errorCode the error code
     *
     * @return the error response
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    /**
     * Of error response.
     *
     * @param code    the code
     * @param message the message
     *
     * @return the error response
     */
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message);
    }

}