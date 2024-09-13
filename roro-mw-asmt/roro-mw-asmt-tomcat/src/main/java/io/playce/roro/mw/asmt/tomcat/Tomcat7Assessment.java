/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Jaeeon Bae       11월 12, 2021            First Draft.
 */
package io.playce.roro.mw.asmt.tomcat;

import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.tomcat.component.ProcessJson;
import io.playce.roro.mw.asmt.tomcat.component.ProcessLocal;
import io.playce.roro.mw.asmt.tomcat.component.ProcessRemote;
import org.springframework.stereotype.Component;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Component("TOMCAT7Assessment")
public class Tomcat7Assessment extends TomcatAssessment {

    public Tomcat7Assessment(ProcessRemote processRemote, ProcessLocal processLocal, ProcessJson processJson, CommandConfig commandConfig) {
        super(processRemote, processLocal, processJson, commandConfig);
    }
}