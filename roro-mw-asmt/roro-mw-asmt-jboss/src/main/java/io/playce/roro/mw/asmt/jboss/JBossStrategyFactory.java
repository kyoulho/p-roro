/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Jhpark       8월 13, 2022            First Draft.
 */
package io.playce.roro.mw.asmt.jboss;

import io.playce.roro.mw.asmt.jboss.strategy.ServerModeStrategy;
import io.playce.roro.mw.asmt.jboss.strategy.enums.StrategyName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *
 * </pre>
 *
 * @author jhpark
 * @version 3.0
 */

/**
 *  JBossStrategyFactory 다른 스프링 bean으로 만들고,
 *  모든 전략을 factory에 주입한다.
 *  여기서 JBossStrategyFactory 구성할 때 전략들을 Map을 이용해서 저장한다.
 *  Standalne mode 와 Domain mode  분리 저장하는게 핵심
 */
@Component
public class JBossStrategyFactory {
    private Map<StrategyName, ServerModeStrategy> strategies;

    @Autowired
    public JBossStrategyFactory(Set<ServerModeStrategy> strategySet) {
        createStrategy(strategySet);
    }

    public ServerModeStrategy findServerModeStrategy(StrategyName strategyName) {
        return strategies.get(strategyName);
    }
    private void createStrategy(Set<ServerModeStrategy> strategySet) {
        strategies = new HashMap<StrategyName, ServerModeStrategy>();
        strategySet.forEach(
                strategy ->strategies.put(strategy.getModeName(), strategy));
    }
}