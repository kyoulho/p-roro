///*
// * Copyright 2021 The Playce-RoRo Project.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// * Revision History
// * Author			Date				Description
// * ---------------	----------------	------------
// * Jeongho Baek   11월 09, 2021		First Draft.
// */
//package io.playce.roro.api.config;
//
//import io.playce.roro.api.domain.authentication.jwt.extractor.TokenExtractor;
//import io.playce.roro.api.domain.authentication.jwt.filter.JwtTokenAuthenticationProcessingFilter;
//import io.playce.roro.api.domain.authentication.jwt.filter.SkipPathRequestMatcher;
//import io.playce.roro.api.domain.authentication.jwt.provider.JwtAuthenticationProvider;
//import io.playce.roro.api.domain.authentication.service.CustomAuthenticationProvider;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.util.matcher.RequestMatcher;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
///**
// * <pre>
// *
// * </pre>
// *
// * @author Jeongho Baek
// * @version 2.0.0
// */
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    private static final String CONSOLE_URL = "/console/**";
//    private static final String API_AUTH_URL = "/api/auth/**";
//    private static final String API_ROOT_URL = "/api/**";
//
//
//    private final CustomAuthenticationProvider customAuthenticationProvider;
//    private final JwtAuthenticationProvider jwtAuthenticationProvider;
//
//    private final AuthenticationFailureHandler failureHandler;
//
//    private final TokenExtractor tokenExtractor;
//
//    public SecurityConfig(CustomAuthenticationProvider customAuthenticationProvider,
//                          JwtAuthenticationProvider jwtAuthenticationProvider,
//                          AuthenticationFailureHandler failureHandler,
//                          TokenExtractor tokenExtractor) {
//        this.customAuthenticationProvider = customAuthenticationProvider;
//        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
//        this.failureHandler = failureHandler;
//        this.tokenExtractor = tokenExtractor;
//    }
//
//    @Override
//    protected void configure(HttpSecurity httpSecurity) throws Exception {
//        // 해당 URL은 Filter에서 생략한다.
//        final String winMiddlewareExcelDownload = "/api/projects/**/inventory/windows/middleware";
//        List<String> permitAllEndpointList = Arrays.asList(CONSOLE_URL, API_AUTH_URL, winMiddlewareExcelDownload, "/api/common/codes");
//
//        httpSecurity
//                .httpBasic().disable()
//                .cors().configurationSource(corsConfigurationSource()) //Cors Config
//                .and()
//                .csrf().disable()
//                // REST 기반이기 때문에 인증 정보를 Session에 담아두지 않는다.
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and()
//                .authorizeRequests()
//                .antMatchers("/**").permitAll()
//                // 인증 Filter를 등록한다.
//                .and()
//                .addFilterBefore(jwtTokenAuthenticationProcessingFilter(permitAllEndpointList, API_ROOT_URL),
//                        UsernamePasswordAuthenticationFilter.class)
//                .formLogin().disable();
//    }
//
//    private JwtTokenAuthenticationProcessingFilter jwtTokenAuthenticationProcessingFilter(
//            List<String> pathsToSkip, String pathToAccept) throws Exception {
//        RequestMatcher matcher = new SkipPathRequestMatcher(pathsToSkip, pathToAccept);
//        JwtTokenAuthenticationProcessingFilter filter
//                = new JwtTokenAuthenticationProcessingFilter(failureHandler, tokenExtractor, matcher);
//        filter.setAuthenticationManager(authenticationManager());
//        return filter;
//    }
//
//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }
//
//    /**
//     * 로그인 처리를 위해서 Spring에서 제공하는 설정만으로는 구현할 수 없기 때문에 직접 Custom을 하여 처리.
//     */
//    @Override
//    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) {
//        authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider);
//        authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider);
//    }
//
//    @Override
//    public void configure(WebSecurity webSecurity) {
//        webSecurity
//                .ignoring()
//                .antMatchers("/v3/api-docs",
//                        "/v3/api-docs/**",
//                        "/swagger-ui.html",
//                        "/swagger-ui/**",
//                        "/logs")
//                // Static 영역
//                .antMatchers("/locales/**",
//                        "/static/**",
//                        "/**/*.json",
//                        "/**/*.html",
//                        "/**/*.js",
//                        "/**/*.ico",
//                        "/**/*.txt"
//                );
//    }
//
//    // CORS 설정 적용.
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // Allow URL
//        configuration.addAllowedOriginPattern("*");
//        // Allow Header
//        configuration.addAllowedHeader("*");
//        // Allow Http Method
//        configuration.addAllowedMethod("*");
//        // configuration.setAllowCredentials(true);
//        configuration.setAllowedOrigins(Collections.singletonList("*"));
//
//        configuration.addExposedHeader("Content-Disposition");
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//
//        return source;
//    }
//
//}
////end of SecurityConfig.java