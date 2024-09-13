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
package io.playce.roro.app.asmt.java.threadpool.monitor;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <pre>
 * ThreadPool Monitoring을 위한 클래스로써 초단위 monitoringPeriod 값을 기준으로 주기적으로 ThreadPool 상태를 모니터링한다.
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0.0
 */
@Slf4j
public class AssessmentThreadPoolMonitor extends Thread {

    private String applicationDir;

    /**
     * The Executor.
     */
    private ThreadPoolExecutor executor;
    /**
     * The Monitoring period.
     */
    private long monitoringPeriod = 5;

    /**
     * The Memory mx bean.
     */
    private MemoryMXBean memoryMXBean;
    /**
     * The Thread mx bean.
     */
    private ThreadMXBean threadMXBean;
    /**
     * The Monitor definition.
     */
    private MonitorDefinition monitorDefinition;

    public AssessmentThreadPoolMonitor(String applicationDir) {
        this.applicationDir = applicationDir;
    }

    /**
     * Sets executor.
     *
     * @param executor the executor
     */
    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    /**
     * Sets monitoring period.
     *
     * @param monitoringPeriod the monitoring period
     */
    public void setMonitoringPeriod(long monitoringPeriod) {
        this.monitoringPeriod = monitoringPeriod;
    }

    /**
     * Run.
     */
    public void run() {
        if (memoryMXBean == null) {
            log.debug("memoryMXBean is null.");
            this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        }

        if (threadMXBean == null) {
            log.debug("threadMXBean is null.");
            this.threadMXBean = ManagementFactory.getThreadMXBean();
        }

        try {
            while (true) {
                monitoring();

                if (executor.isTerminated()) {
                    break;
                }

                Thread.sleep(monitoringPeriod * 1000);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Monitoring.
     */
    private void monitoring() {
        MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();

        monitorDefinition = new MonitorDefinition();

        monitorDefinition.setApplicationDir(applicationDir);
        monitorDefinition.setCurrentPoolSize(executor.getPoolSize());
        monitorDefinition.setCorePoolSize(executor.getCorePoolSize());
        monitorDefinition.setMaximumPoolSize(executor.getMaximumPoolSize());
        monitorDefinition.setActiveTaskCount(executor.getActiveCount());
        monitorDefinition.setCompletedTaskCount(executor.getCompletedTaskCount());
        monitorDefinition.setTotalTaskCount(executor.getTaskCount());
        monitorDefinition.setTerminated(executor.isTerminated());
        monitorDefinition.setQueueSize(executor.getQueue().size());
        monitorDefinition.setQueueRemainingCapacity(executor.getQueue().remainingCapacity());

        monitorDefinition.setUsed(memoryUsage.getUsed());
        monitorDefinition.setCommitted(memoryUsage.getCommitted());
        monitorDefinition.setMax(memoryUsage.getMax());

        monitorDefinition.setLive(threadMXBean.getThreadCount());
        monitorDefinition.setPeak(threadMXBean.getPeakThreadCount());
        monitorDefinition.setTotal(threadMXBean.getTotalStartedThreadCount());

        log.debug("{}", monitorDefinition);
    }
}
//end of AssessmentThreadPoolMonitor.java