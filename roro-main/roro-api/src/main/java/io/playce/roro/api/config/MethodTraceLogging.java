/*
 * Copyright 2023 The playce-roro Project.
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
 * Dong-Heon Han    May 22, 2023		First Draft.
 */

package io.playce.roro.api.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class MethodTraceLogging {
    @Before("execution(* io.playce.roro.api.domain..*.*(..))")
    public void beforeRoRoMethodLogging(JoinPoint jp) {
        Signature signature = jp.getSignature();
        log.trace("--> {}.{}", signature.getDeclaringTypeName(), signature.getName());
    }

    @After("execution(* io.playce.roro.api.domain..*.*(..))")
    public void afterRoRoMethodLogging(JoinPoint jp) {
        Signature signature = jp.getSignature();
        log.trace("<-- {}.{}", signature.getDeclaringTypeName(), signature.getName());
    }
}