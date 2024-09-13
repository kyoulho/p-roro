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
 * Sang-cheon Park	2020. 3. 14.		First Draft.
 */
package io.playce.roro.common.exception;

/**
 * <pre>
 * An exception which raises when communication meets any problem
 * </pre>
 * @author Sang-cheon Park
 * @version 1.0
 */
public class RoRoException extends RuntimeException {

    private static final long serialVersionUID = 4610406634943872782L;

    private Integer errorCode;

    /**
     * <pre>
     * Constructor of RoRoException with message
     * </pre>
     */
    public RoRoException() {
    }

    /**
     * <pre>
     * Constructor of RoRoException with message
     * </pre>
     * @param message
     */
    public RoRoException(String message) {
        super(message);
    }

    /**
     * <pre>
     * Constructor of RoRoException with cause
     * </pre>
     * @param cause
     */
    public RoRoException(Throwable cause) {
        super(cause);
    }

    /**
     * <pre>
     * Constructor of RoRoException with errorCode & message
     * </pre>
     * @param errorCode
     * @param message
     */
    public RoRoException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * <pre>
     * Constructor of RoRoException with message and cause
     * </pre>
     * @param message
     * @param cause
     */
    public RoRoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @return the errorCode
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    public static void checkInterruptedException(Exception e) throws InterruptedException {
        if(e instanceof InterruptedException) {
            throw (InterruptedException) e;
        }
    }
}
//end of RoRoException.java