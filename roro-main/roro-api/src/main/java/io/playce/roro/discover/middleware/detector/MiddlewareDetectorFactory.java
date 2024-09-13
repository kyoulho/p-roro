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
 * Hoon Oh       1월 27, 2022            First Draft.
 */
package io.playce.roro.discover.middleware.detector;

import io.playce.roro.discover.middleware.MiddlewareTypeChecker;
import io.playce.roro.discover.middleware.detector.impl.*;
import io.playce.roro.svr.asmt.dto.common.processes.Process;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class MiddlewareDetectorFactory {

    public static MiddlewareDetector getDetector(Process process) {
        MiddlewareTypeChecker checker = MiddlewareTypeChecker.isContain(process.getCmd());

        MiddlewareDetector detector = null;
        if (checker != null) {
            if (checker.equals(MiddlewareTypeChecker.TOMCAT)) {
                detector = new TomcatDetector(process);
            } else if (checker.equals(MiddlewareTypeChecker.JEUS)) {
                detector = new JeusDetector(process);
            } else if (checker.equals(MiddlewareTypeChecker.WEBLOGIC)) {
                detector = new WebLogicDetector(process);
            } else if (checker.equals(MiddlewareTypeChecker.WEBSPHERE)) {
                detector = new WebSphereDetector(process);
            } else if (checker.equals(MiddlewareTypeChecker.APACHE)) {
                detector = new ApacheDetector(process);
            } else if (checker.equals(MiddlewareTypeChecker.WEBTOB)) {
                detector = new WebTobDetector(process);
            } else if (checker.equals(MiddlewareTypeChecker.JBOSS)) {
                detector = new JBossDetector(process);
            } else if (checker.equals(MiddlewareTypeChecker.NGINX)) {
                detector = new NginxDetector(process);
            }
        }

        return detector;
    }


}
//end of MiddlewareCheckerFactory.java