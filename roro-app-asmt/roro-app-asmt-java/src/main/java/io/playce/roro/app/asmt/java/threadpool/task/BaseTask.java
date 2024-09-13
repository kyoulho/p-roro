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
package io.playce.roro.app.asmt.java.threadpool.task;

import io.playce.roro.app.asmt.java.threadpool.handler.AssessmentTaskExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0.0
 */
@Slf4j
public abstract class BaseTask implements Runnable {

    /**
     * The constant IPADDRESS_PATTERN.
     */
    protected static final String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    /**
     * The Task name.
     */
    protected String taskName;

    /**
     * <pre>
     * Constructor
     * </pre>
     *
     * @param taskName the task name
     */
    public BaseTask(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Gets task name.
     *
     * @return the taskName
     */
    public String getTaskName() {
        return taskName == null ? super.toString() : taskName;
    }

    /**
     * Sets task name.
     *
     * @param taskName the taskName to set
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * <pre>
     *
     * </pre>
     *
     * @see Runnable#run() java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // Thread 실행 시 UncaughtException에 대해 catch 하기 위해 handler 등록.
        Thread.currentThread().setUncaughtExceptionHandler(new AssessmentTaskExceptionHandler());

        //log.debug("[{}] is started.", getTaskName());
        // TODO 사전 작업이 필요하면 beforeRun() 메소드 구현 후 호출

        taskRun();

        // TODO 사후 작업이 필요하면 afterRun() 메소드 구현 후 호출
        //log.debug("[{}] is completed.", getTaskName());
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return getTaskName();
    }

    /**
     * Task run.
     */
    protected abstract void taskRun();
}
//end of BaseTask.java