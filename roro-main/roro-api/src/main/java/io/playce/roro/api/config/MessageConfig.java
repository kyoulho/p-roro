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
package io.playce.roro.api.config;

import io.playce.roro.api.common.i18n.AcceptHeaderAndCookieLocaleResolver;
import io.playce.roro.api.common.i18n.MessageLocaleChangeInterceptor;
import net.rakugakibox.util.YamlResourceBundle.Control;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 1.0
 */
@Configuration
public class MessageConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderAndCookieLocaleResolver localeResolver = new AcceptHeaderAndCookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setCookieName("roro.locale");

        return localeResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        MessageLocaleChangeInterceptor localeChangeInterceptor = new MessageLocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        registry.addInterceptor(localeChangeInterceptor);
    }

    /**
     * Message source message source.
     *
     * @param basename the basename
     * @return the message source
     */
    @Bean // yml 파일을 참조하는 MessageSource 선언
    public MessageSource messageSource(@Value("${spring.messages.basename}") String basename) {
        YamlMessageSource ms = new YamlMessageSource();
        ms.setBasename(basename);
        ms.setDefaultEncoding(StandardCharsets.UTF_8.name());
        ms.setUseCodeAsDefaultMessage(true);
        ms.setAlwaysUseMessageFormat(true);
        ms.setFallbackToSystemLocale(true);
        return ms;
    }

    /**
     * <pre>
     * locale 정보에 따라 다른 yml 파일을 읽도록 처리
     * </pre>
     *
     * @author SangCheon Park
     * @version 1.0
     */
    static class YamlMessageSource extends ResourceBundleMessageSource {
        /**
         * Do get bundle resource bundle.
         *
         * @param basename the basename
         * @param locale   the locale
         * @return the resource bundle
         * @throws MissingResourceException the missing resource exception
         */
        @Override
        protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
            return ResourceBundle.getBundle(basename, locale, Control.INSTANCE);
        }
    }

}