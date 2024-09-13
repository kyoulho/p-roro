package io.playce.roro.api.config;

import io.playce.roro.api.domain.authentication.jwt.extractor.TokenExtractor;
import io.playce.roro.api.domain.authentication.jwt.filter.JwtTokenAuthenticationProcessingFilter;
import io.playce.roro.api.domain.authentication.jwt.filter.SkipPathRequestMatcher;
import io.playce.roro.api.domain.authentication.jwt.provider.JwtAuthenticationProvider;
import io.playce.roro.api.domain.authentication.service.CustomAuthenticationProvider;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MultipleSecurityConfiguration {


    @Order(1)
    @Configuration
    @AllArgsConstructor
    public static class SecurityConfig extends WebSecurityConfigurerAdapter {
        private static final String CONSOLE_URL = "/console/**";
        private static final String API_AUTH_URL = "/api/auth/**";
        private static final String API_ROOT_URL = "/api/**";

        private final CustomAuthenticationProvider customAuthenticationProvider;
        private final JwtAuthenticationProvider jwtAuthenticationProvider;

        private final AuthenticationFailureHandler failureHandler;

        private final TokenExtractor tokenExtractor;

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            // 해당 URL은 Filter에서 생략한다.
            final String winMiddlewareExcelDownload = "/api/projects/**/inventory/windows/middleware";
            List<String> permitAllEndpointList = Arrays.asList(CONSOLE_URL, API_AUTH_URL, winMiddlewareExcelDownload, "/api/common/codes");

            httpSecurity
                    .httpBasic().disable()
                    .cors().configurationSource(corsConfigurationSource()) //Cors Config
                    .and()
                    .csrf().disable()
                    // REST 기반이기 때문에 인증 정보를 Session에 담아두지 않는다.
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .authorizeRequests()
                    .antMatchers("/**").permitAll()
                    // 인증 Filter를 등록한다.
                    .and()
                    .addFilterBefore(jwtTokenAuthenticationProcessingFilter(permitAllEndpointList, API_ROOT_URL),
                            UsernamePasswordAuthenticationFilter.class)
                    .formLogin().disable();
        }

        private JwtTokenAuthenticationProcessingFilter jwtTokenAuthenticationProcessingFilter(
                List<String> pathsToSkip, String pathToAccept) throws Exception {
            RequestMatcher matcher = new SkipPathRequestMatcher(pathsToSkip, pathToAccept);
            JwtTokenAuthenticationProcessingFilter filter
                    = new JwtTokenAuthenticationProcessingFilter(failureHandler, tokenExtractor, matcher);
            filter.setAuthenticationManager(authenticationManager());
            return filter;
        }

        @Bean
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        /**
         * 로그인 처리를 위해서 Spring에서 제공하는 설정만으로는 구현할 수 없기 때문에 직접 Custom을 하여 처리.
         */
        @Override
        protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) {
            // 로그인은 oauth에서 진행하기 때문에 CustomAuthenticationProvider는 주석처리
            // authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider);
            authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider);
        }

        @Override
        public void configure(WebSecurity webSecurity) {
            webSecurity
                    .ignoring()
                    .antMatchers("/v3/api-docs",
                            "/v3/api-docs/**",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/logs/rest/**")
                    // Static 영역
                    .antMatchers("/locales/**",
                            "/static/**",
                            "/**/*.json",
                            "/**/*.html",
                            "/**/*.js",
                            "/**/*.ico",
                            "/**/*.txt"
                    );
        }

        // CORS 설정 적용.
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();

            // Allow URL
            configuration.addAllowedOriginPattern("*");
            // Allow Header
            configuration.addAllowedHeader("*");
            // Allow Http Method
            configuration.addAllowedMethod("*");
            // configuration.setAllowCredentials(true);
            configuration.setAllowedOrigins(Collections.singletonList("*"));

            configuration.addExposedHeader("Content-Disposition");

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);

            return source;
        }
    }

//    @Order(2)
//    @Configuration
//    @AllArgsConstructor
//    public static class WebViewerAuthentication extends WebSecurityConfigurerAdapter {
//
//        private final PasswordEncoder passwordEncoder;
//
//        @Override
//        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//
//            auth.inMemoryAuthentication()
//                    .withUser("roro")
//                    .password(passwordEncoder.encode("jan01jan")).roles("ADMIN")
//            ;
//        }
//    }

    @Order(-1)
    @Configuration
    @AllArgsConstructor
    public static class WebViewerPathConfig extends WebSecurityConfigurerAdapter {

        private final PasswordEncoder passwordEncoder;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {

            String username = System.getProperty("roro.log.viewer.username", "admin");
            String password = System.getProperty("roro.log.viewer.password", "admin");

            auth.inMemoryAuthentication()
                    .withUser(username)
                    .password(passwordEncoder.encode(password)).roles("ADMIN")
            ;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers().antMatchers("/logs/**")
                    .and()
                    .authorizeRequests().anyRequest().hasRole("ADMIN")
                    .and()
                    .httpBasic();
        }
    }

}
