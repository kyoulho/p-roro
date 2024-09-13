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
package io.playce.roro.mw.asmt.jeus;

import io.playce.roro.common.windows.JeusExtract;
import io.playce.roro.mw.asmt.component.CommandConfig;
import org.springframework.stereotype.Component;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Component("JEUS7Assessment")
public class Jeus7Assessment extends JeusAssessment {

    public Jeus7Assessment(CommandConfig commandConfig, JeusExtract jeusExtract) {
        super(commandConfig, jeusExtract);
    }
}
//end of JeusAssessment7.java