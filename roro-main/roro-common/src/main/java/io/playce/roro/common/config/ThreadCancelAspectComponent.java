/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * Dong-Heon Han    May 19, 2022		First Draft.
 */

package io.playce.roro.common.config;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
@Aspect
public class ThreadCancelAspectComponent {
    @Before("execution(* io.playce.roro.svr..*.*(..)) || " +
            "execution(* io.playce.roro.mw..*.*(..)) || " +
            "execution(* io.playce.roro.mig..*.*(..)) || " +
            "execution(* io.playce.roro.prerequsite..*.*(..)) || " +
            "execution(* io.playce.roro.discover..*.*(..))")
    public void sleep() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(10);
    }
}