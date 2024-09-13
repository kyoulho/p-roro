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
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Configuration
public class AcceptHeaderAndCookieLocaleResolver extends CookieLocaleResolver {

    @Nullable
    protected Locale determineDefaultLocale(HttpServletRequest request) {
        if (StringUtils.isNotEmpty(request.getHeader("Accept-Language"))) {
            //Accept header에서 ja-JP, ko-KR로 전송되는 경우가 있다 -> _(Under bar)를 허용하지 않기 때문이다
            return Locale.forLanguageTag(request.getHeader("Accept-Language"));
        } else {
            return super.determineDefaultLocale(request);
        }
    }

}
//end of CustomLocaleResolver.java