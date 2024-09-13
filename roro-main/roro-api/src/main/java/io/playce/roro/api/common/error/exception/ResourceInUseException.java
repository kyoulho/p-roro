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
 * SangCheon Park   Oct 20, 2020		    First Draft.
 */
package io.playce.roro.api.common.error.exception;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 1.0
 */
public class ResourceInUseException extends RuntimeException {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -527053172695232649L;

    /**
     * <pre>
     *
     * </pre>
     *
     * @param message the message
     * @param cause   the cause
     */
    public ResourceInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <pre>
     *
     * </pre>
     *
     * @param message the message
     */
    public ResourceInUseException(String message) {
        super(message);
    }

    /**
     * <pre>
     *
     * </pre>
     */
    public ResourceInUseException() {
    }
}
//end of ResourceInUseException.java