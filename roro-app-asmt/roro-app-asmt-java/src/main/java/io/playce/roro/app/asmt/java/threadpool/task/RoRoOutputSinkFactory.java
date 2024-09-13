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
 * Dong-Heon Han    Jul 07, 2022		First Draft.
 */

package io.playce.roro.app.asmt.java.threadpool.task;

import org.benf.cfr.reader.api.OutputSinkFactory;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
public class RoRoOutputSinkFactory implements OutputSinkFactory {
    private final PrintStream printStream;

    public RoRoOutputSinkFactory(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> collection) {
        return Collections.singletonList(SinkClass.STRING);
    }

    @Override
    public <T> Sink<T> getSink(SinkType sinkType, SinkClass sinkClass) {
        return sinkType == SinkType.JAVA ? printStream::println : ignore -> {};
    }
}