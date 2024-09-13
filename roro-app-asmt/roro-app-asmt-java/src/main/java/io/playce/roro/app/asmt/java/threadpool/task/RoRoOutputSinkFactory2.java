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
 * SangCheon Park   Jul 07, 2022		    First Draft.
 */
package io.playce.roro.app.asmt.java.threadpool.task;

import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class RoRoOutputSinkFactory2 implements OutputSinkFactory {

    private List<SinkReturns.DecompiledMultiVer> decompiledList;
    private OutputSinkFactory.Sink<SinkReturns.DecompiledMultiVer> decompiledSourceSink;

    public RoRoOutputSinkFactory2() {
        this.decompiledList = new ArrayList<>();
        this.decompiledSourceSink = this.decompiledList::add;
    }

    @Override
    public List<OutputSinkFactory.SinkClass> getSupportedSinks(OutputSinkFactory.SinkType sinkType, Collection<OutputSinkFactory.SinkClass> collection) {
        switch (sinkType) {
            case JAVA:
                return Collections.singletonList(OutputSinkFactory.SinkClass.DECOMPILED_MULTIVER);
            default:
                return Collections.singletonList(OutputSinkFactory.SinkClass.STRING);
        }
    }

    @Override
    public <T> OutputSinkFactory.Sink<T> getSink(OutputSinkFactory.SinkType sinkType, OutputSinkFactory.SinkClass sinkClass) {
        switch (sinkType) {
            case JAVA:
                if (sinkClass != OutputSinkFactory.SinkClass.DECOMPILED_MULTIVER) {
                    throw new IllegalArgumentException("Sink class " + sinkClass + " is not supported for decompiled output");
                }
                return (OutputSinkFactory.Sink<T>) decompiledSourceSink;
            default:
                return ignored -> {};
        }
    }

    public String getResult() {
        StringBuilder sb = new StringBuilder();

        for (SinkReturns.DecompiledMultiVer val : decompiledList) {
            sb.append(val.getJava());
        }

        return sb.toString();
    }
}