/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * SangCheon Park   Jan 12, 2022	    First Draft.
 */
package io.playce.roro.app.asmt.java.threadpool.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * Runnable Task 실행 시 발생되는 UncaughtException을 처리하기 위한 핸들러
 * 예상되는 Exception은 task 내의 run() 내에서 구현.
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0.0
 */
@Slf4j
public class AssessmentTaskExceptionHandler implements Thread.UncaughtExceptionHandler {

    /**
     * <pre>
     *
     * </pre>
     *
     * @param t
     * @param e
     *
     * @see Thread.UncaughtExceptionHandler#uncaughtException(Thread, Throwable)
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.info("Exception occurred during [{}] thread was running.", t.getName());
        log.error("[" + t.getName() + "]'s Exception detail => ", e);

        // TODO task 실행시 발생된 UncaughtException에 대하여 공통적으로 처리해야할 부분이 있을 경우 구현한다.
    }
}
//end of AssessmentTaskExceptionHandler.java