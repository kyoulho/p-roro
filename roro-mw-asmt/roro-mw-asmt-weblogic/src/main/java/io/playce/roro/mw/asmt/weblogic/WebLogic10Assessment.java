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
package io.playce.roro.mw.asmt.weblogic;

import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.weblogic.helper.WebLogicHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
@Component("WEBLOGIC10Assessment")
public class WebLogic10Assessment extends WebLogicAssessment {

    public WebLogic10Assessment(CommandConfig commandConfig, WebLogicHelper webLogicHelper) {
        super(commandConfig, webLogicHelper);
    }

    @Override
    public MiddlewareAssessmentResult assessment(TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        return super.assessment(targetHost, middleware, strategy);
    }
}
//end of WebLogicAssessment10.java