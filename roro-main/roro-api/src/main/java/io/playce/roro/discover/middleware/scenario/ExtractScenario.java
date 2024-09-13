/*
 * Copyright 2022 The Playce-WASUP Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       1월 27, 2022            First Draft.
 */
package io.playce.roro.discover.middleware.scenario;

import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.svr.asmt.dto.common.processes.Process;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public abstract class ExtractScenario {
    private ExtractScenario next;
    protected TargetHost targetHost;
    protected List<String> ignoreCases;
    protected String result;

    public ExtractScenario() {
        ignoreCases = new ArrayList<>();
    }

    public static void setChain(TargetHost targetHost, ExtractScenario... scenarios) {
        ExtractScenario pre = null;
        for (ExtractScenario scenario : scenarios) {
            scenario.setTargetHost(targetHost);
            if (pre != null) {
                pre.setNext(scenario);
            }
            pre = scenario;
        }
    }

    public ExtractScenario setNext(ExtractScenario next) {
        this.next = next;
        return next;
    }

    public String execute(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        if (extract(process, commandConfig, strategy) && validate()) {
            return result;
        } else if (next != null) {
            return next.execute(process, commandConfig, strategy);
        } else {
            return "";
        }
    }

    private boolean validate() {
        if (ignoreCases == null)
            return true;

        return ignoreCases.stream().noneMatch(u -> u.equals(result));
    }

    public void setIgnoreCases(List<String> ignoreCases) {
        this.ignoreCases = ignoreCases;
    }

    public void setTargetHost(TargetHost targetHost) {
        this.targetHost = targetHost;
    }

    public TargetHost getTargetHost() {
        return this.targetHost;
    }

    protected abstract boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException;


}
//end of ExtractEnginePath.java