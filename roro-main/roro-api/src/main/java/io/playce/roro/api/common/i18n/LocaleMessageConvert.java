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
package io.playce.roro.api.common.i18n;

import io.playce.roro.api.common.error.ErrorCode;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 1.0
 */
@Component
public class LocaleMessageConvert {

    /**
     * The Message source.
     */
    private final MessageSource messageSource;

    /**
     * Instantiates a new Locale message convert.
     *
     * @param messageSource the message source
     */
    public LocaleMessageConvert(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Gets convert error message.
     *
     * @param errorCodeValue the error code value
     *
     * @return the convert error message
     */
    public ErrorCode getConvertErrorMessage(String errorCodeValue) {
        return getConvertErrorMessage(ErrorCode.stream()
                .filter(ex -> ex.getCode().equals(errorCodeValue))
                .findFirst().get()
        );
    }

    /**
     * Gets convert error message.
     *
     * @param errorCode the error code
     *
     * @return the convert error message
     */
    @SuppressWarnings("ConfusingArgumentToVarargsMethod")
    public ErrorCode getConvertErrorMessage(ErrorCode errorCode) {
        return getConvertErrorMessage(errorCode, null);
    }

    /**
     * Gets convert error message.
     *
     * @param errorCode  the error code
     * @param parameters the parameters
     *
     * @return the convert error message
     */
    public ErrorCode getConvertErrorMessage(ErrorCode errorCode, String... parameters) {
        errorCode.setConvertedMessage(getMessage(errorCode.getMessage(), parameters));
        return errorCode;
    }

    /**
     * Gets message.
     *
     * @param messageCode the message code
     *
     * @return the message
     */
    public String getMessage(String messageCode) {
        return getMessage(messageCode, null);
    }

    /**
     * Gets message.
     *
     * @param messageCode the message code
     * @param parameters  the parameters
     *
     * @return the message
     */
    public String getMessage(String messageCode, Object[] parameters) {
        return messageSource.getMessage(messageCode, parameters, LocaleContextHolder.getLocale());
    }

}
