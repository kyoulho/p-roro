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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0.0
 */
@Slf4j
public class AssessmentRejectedExecutionHandler implements RejectedExecutionHandler {

    /**
     * Rejected execution.
     *
     * @param runnable the runnable
     * @param executor the executor
     */
    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        log.info("[{}] has been rejected.", runnable.toString());

        // TODO task의 실행이 거부되었을 경우 처리해야할 로직이 남아 있다면 여기에 추가.
    }
}
//end of AssessmentRejectedExecutionHandler.java