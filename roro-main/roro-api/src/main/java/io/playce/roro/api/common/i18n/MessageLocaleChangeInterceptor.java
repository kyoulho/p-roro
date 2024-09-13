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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Jeongho Baek   7월 08, 2021		First Draft.
 */
package io.playce.roro.api.common.i18n;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
public class MessageLocaleChangeInterceptor extends LocaleChangeInterceptor {

    private static final Locale wrongLocale = new Locale("");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {
        if (!StringUtils.isEmpty(request.getHeader("Accept-Language"))) {
            if (LocaleContextHolder.getLocale().equals(wrongLocale)) {
                return super.preHandle(request, response, handler);
            } else {
                return true;
            }
        } else {
            return super.preHandle(request, response, handler);
        }
    }
}
//end of ApplicationLocaleChangeInterceptor.java