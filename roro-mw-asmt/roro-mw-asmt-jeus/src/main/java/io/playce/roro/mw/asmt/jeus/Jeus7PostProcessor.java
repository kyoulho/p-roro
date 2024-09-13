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
 * Dong-Heon Han    Mar 02, 2022		First Draft.
 */

package io.playce.roro.mw.asmt.jeus;

import io.playce.roro.mw.asmt.dto.DiscMiddlewareInstance;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.jeus.dto.JeusAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component("JEUS7PostProcessor")
@Slf4j
public class Jeus7PostProcessor extends JeusPostProcessor {
    @Override
    public List<DiscMiddlewareInstance> getDiscoveredMiddlewareInstances(MiddlewareAssessmentResult result, GetInfoStrategy strategy) {
        if (result== null) return null;

        JeusAssessmentResult.Engine engine = (JeusAssessmentResult.Engine) result.getEngine();
        JeusAssessmentResult.Instance instance = (JeusAssessmentResult.Instance) result.getInstance();
        List<DiscMiddlewareInstance> resultList= new ArrayList<>();

        if(CollectionUtils.isEmpty(instance.getInstances())) return null;

        for (JeusAssessmentResult.Instances ins : instance.getInstances()) {
            DiscMiddlewareInstance discMiddlewareInstance = new DiscMiddlewareInstance();
            discMiddlewareInstance.setMiddlewareInstanceName(ins.getName());
            discMiddlewareInstance.setMiddlewareInstancePath(engine.getPath());//domain home.
            discMiddlewareInstance.setMiddlewareInstanceDetailDivision(engine.getPath() + "|" + ins.getName());

            JeusAssessmentResult.Listeners listeners = ins.getListeners();
            if(listeners != null) {
                Map<String, JeusAssessmentResult.Listener> listenerMap = getListenerMap(listeners.getListeners());
                Map<String, String> protocolMap = getProtocols(ins.getEngines());
                Map<String, String> protocolPortMap = getProtocolPortMap(protocolMap, listenerMap);

                discMiddlewareInstance.setMiddlewareInstanceProtocol(String.join(",", protocolPortMap.keySet()));
                discMiddlewareInstance.setMiddlewareInstanceServicePort(String.join(",", protocolPortMap.values()));
            }
            discMiddlewareInstance.setRunningUser(ins.getRunUser());
            discMiddlewareInstance.setJavaVersion(instance.getJavaVersion());
            discMiddlewareInstance.setJavaVendor(instance.getJavaVendor());
            discMiddlewareInstance.setRuuning(ins.getStatus().contains("Running"));

            resultList.add(discMiddlewareInstance);
        }

        return resultList;
    }

    private Map<String, String> getProtocolPortMap(Map<String, String> protocolMap, Map<String, JeusAssessmentResult.Listener> listenerMap) {
        Map<String, String> resultMap = new HashMap<>();
        for(String key: protocolMap.keySet()) {
            String value = protocolMap.get(key);
            JeusAssessmentResult.Listener listener = listenerMap.get(value);
            if(listener == null) continue;

            resultMap.put(key, listener.getListenPort());
        }
        return resultMap;
    }

    private Map<String, String> getProtocols(JeusAssessmentResult.Engines engines) {
        Map<String, String> listenerNameMap = new HashMap<>();
        if(engines == null) return listenerNameMap;

        List<JeusAssessmentResult.WebEngine> webEngines = engines.getWebEngine();
        if(CollectionUtils.isEmpty(webEngines)) return listenerNameMap;

        for(JeusAssessmentResult.WebEngine webEngine: webEngines) {
            JeusAssessmentResult.WebConnections webConnections = webEngine.getWebConnections();
            if(webConnections == null) continue;

            List<JeusAssessmentResult.HttpListener> httpListeners = webConnections.getHttpListener();
            if(CollectionUtils.isEmpty(httpListeners)) continue;

            for(JeusAssessmentResult.HttpListener httpListener: httpListeners) {
                listenerNameMap.put(httpListener.getName(), httpListener.getServerListenerRef());
            }
        }

        return listenerNameMap;
    }

    private Map<String, JeusAssessmentResult.Listener> getListenerMap(List<JeusAssessmentResult.Listener> listeners) {
        Map<String, JeusAssessmentResult.Listener> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(listeners)) return resultMap;

        for(JeusAssessmentResult.Listener listener: listeners) {
            resultMap.put(listener.getName(), listener);
        }

        return resultMap;
    }
}