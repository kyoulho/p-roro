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
package io.playce.roro.app.asmt.java.threadpool.executor;

import io.playce.roro.app.asmt.java.threadpool.handler.AssessmentRejectedExecutionHandler;
import io.playce.roro.app.asmt.java.threadpool.monitor.AssessmentThreadPoolMonitor;
import io.playce.roro.app.asmt.java.threadpool.task.BaseTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * Refer : https://deep-dive-dev.tistory.com/11, https://www.baeldung.com/java-rejectedexecutionhandler
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0.0
 */
@Slf4j
public class AssessmentThreadPoolExecutor {

    /**
     * The constant MONITORING_PERIOD.
     */
    private static final long MONITORING_PERIOD = 5;
    /**
     * The Core pool size.
     */
    private int corePoolSize = 1;
    /**
     * The Max pool size.
     */
    private int maxPoolSize = 10;
    /**
     * The Keep alive time.
     */
    private long keepAliveTime = 60;
    /**
     * The Queue capacity.
     */
    private int queueCapacity = Integer.MAX_VALUE;
    /**
     * The Rejected execution handler.
     */
    private AssessmentRejectedExecutionHandler rejectedExecutionHandler = new AssessmentRejectedExecutionHandler();

    /**
     * The Executor.
     */
    private ThreadPoolExecutor executor;
    /**
     * The Monitor.
     */
    private AssessmentThreadPoolMonitor monitor;

    private String applicationDir;

    /**
     * Gets executor.
     *
     * @return the executor
     */
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public AssessmentThreadPoolExecutor(String applicationDir) {
        this.applicationDir = applicationDir;
        initialize();
    }

    /**
     * Initialize.
     */
    private void initialize() {
        /*
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                keepAliveTime, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                rejectedExecutionHandler);
        /*/
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                keepAliveTime, TimeUnit.SECONDS,
                // new SynchronousQueue<>(),
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        //*/

        monitor = new AssessmentThreadPoolMonitor(applicationDir);
        monitor.setMonitoringPeriod(MONITORING_PERIOD);
        monitor.setExecutor(executor);
    }

    /**
     * Execute.
     *
     * @param task the task
     */
    public void execute(Runnable task) {
        Assert.notNull(task, "task must not be null.");

        checkExecutor();
        executor.execute(task);
    }

    /**
     * Execute.
     *
     * @param task the task
     */
    public void execute(BaseTask task) {
        Assert.notNull(task, "task must not be null.");

        checkExecutor();
        executor.execute(task);
    }

    /**
     * Execute.
     *
     * @param taskList the task list
     */
    public void execute(List<BaseTask> taskList) {
        Assert.notNull(taskList, "taskList must not be null.");

        checkExecutor();
        for (BaseTask task : taskList) {
            executor.execute(task);
        }
    }

    /**
     * executor가 terminated 되었을 경우 executor 초기화 및 monitor가 중지 되었을 경우 monitor 초기화
     */
    private void checkExecutor() {
        try {
            if (executor.isTerminated()) {
                initialize();
            }

            if (monitor != null && !monitor.isAlive()) {
                if (monitor.getState().equals(Thread.State.TERMINATED)) {
                    monitor = new AssessmentThreadPoolMonitor(applicationDir);
                    monitor.setMonitoringPeriod(MONITORING_PERIOD);
                    monitor.setExecutor(executor);
                }

                monitor.start();
            }
        } catch (Exception e) {
            log.warn("AssessmentThreadPoolExecutor state check has been failed. [{}]", e.getMessage());
            //throw new RuntimeException(e);
        }
    }
}
//end of AssessmentThreadPoolExecutor.java