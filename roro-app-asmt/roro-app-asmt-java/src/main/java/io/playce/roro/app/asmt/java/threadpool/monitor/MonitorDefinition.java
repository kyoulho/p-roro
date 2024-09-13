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

import lombok.Getter;
import lombok.Setter;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0.0
 */
@Getter
@Setter
public class MonitorDefinition {

    private String applicationDir;

    /**
     * The Current pool size.
     */
    private int currentPoolSize;
    /**
     * The Core pool size.
     */
    private int corePoolSize;
    /**
     * The Maximum pool size.
     */
    private int maximumPoolSize;
    /**
     * The Active task count.
     */
    private int activeTaskCount;
    /**
     * The Completed task count.
     */
    private long completedTaskCount;
    /**
     * The Total task count.
     */
    private long totalTaskCount;
    /**
     * The Queue size.
     */
    private int queueSize;
    /**
     * The Queue remaining capacity size.
     */
    private int queueRemainingCapacity;
    /**
     * The Is terminated.
     */
    private boolean isTerminated;

    // MemoryMXBean (bytes 단위)
    /**
     * The Used.
     */
    private long used;
    /**
     * The Committed.
     */
    private long committed;
    /**
     * The Max.
     */
    private long max;

    //ThreadMXBean
    /**
     * The Live.
     */
    private int live;
    /**
     * The Peak.
     */
    private int peak;
    /**
     * The Total.
     */
    private long total;

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        sb.append("[Application Directory] ").append(getApplicationDir()).append("\n")
                .append("[ThreadPoolExecutor] CurrentPoolSize : ").append(getCurrentPoolSize())
                .append(", CorePoolSize : ").append(getCorePoolSize())
                .append(", MaximumPoolSize : ").append(getMaximumPoolSize())
                .append(", ActiveTaskCount : ").append(getActiveTaskCount())
                .append(", CompletedTaskCount : ").append(getCompletedTaskCount())
                .append(", TotalTaskCount : ").append(getTotalTaskCount())
                .append(", QueueSize : ").append(getQueueSize())
                .append(", QueueRemainingCapacity : ").append(getQueueRemainingCapacity())
                .append(", isTerminated : ").append(isTerminated()).append("\n")
                .append("[MemoryMXBean] Used : ").append(getUsed())
                .append(", Committed : ").append(getCommitted())
                .append(", Max : ").append(getMax()).append("\n")
                .append("[ThreadMXBean] Live : ").append(getLive())
                .append(", Peak : ").append(getPeak())
                .append(", Total : ").append(getTotal());

        return sb.toString();
    }
}
//end of MonitorDefinition.java