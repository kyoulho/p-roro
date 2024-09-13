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
 * Jeongho Baek   11월 08, 2021		First Draft.
 */
package io.playce.roro.api.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Null인 경우 null로 표시
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Null 대신 "" 로 변경
        // objectMapper.getSerializerProvider().setNullValueSerializer(new NullToEmptyStringSerializer());

        return objectMapper;
    }

    @Bean
    public XmlMapper xmlMapper() {
        return new XmlMapper();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.getConfiguration().setCollectionsMergeEnabled(false);

        /*
        // failed to convert org.hibernate.collection.internal.PersistentBag to java.util.List
        // https://github.com/modelmapper/modelmapper/issues/97
        modelMapper.getConfiguration().setPropertyCondition(context -> !(context.getSource() instanceof PersistentCollection));
        //*/

        return modelMapper;
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder().serializeNulls().create();
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Default : BCryptPasswordEncoder
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Add view controllers.
     *
     * @param registry the registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map "/"
        registry.addViewController("/").setViewName("forward:/index.html");

        // Map "/word", "/word/word", and "/word/word/word" - except for anything starting with "/api/..." or ending with
        // a file extension like ".js" - to index.html. By doing this, the client receives and routes the url. It also
        // allows client-side URLs to be bookmarked.
        // Single directory level - no need to exclude "api"
        //registry.addViewController("/{x:[\\w\\-]+}").setViewName("forward:/index.html");

        // Multi-level directory path, need to exclude "api" on the first part of the path
        //registry.addViewController("/{x:^[(?!api$)|(?!auth$)].*$}/**/{y:[\\w\\-]+}").setViewName("forward:/index.html");
        //registry.addViewController("/{x:[\\w\\-]+}/**/{y:[\\w\\-]+}").setViewName("forward:/index.html");

        registry.addViewController("/{x:console}").setViewName("forward:/index.html");
        registry.addViewController("/{x:console}/").setViewName("forward:/index.html");
        registry.addViewController("/{x:console}/**/{y:[\\w\\-]+}").setViewName("forward:/index.html");
    }

}
//end of WebConfig.java