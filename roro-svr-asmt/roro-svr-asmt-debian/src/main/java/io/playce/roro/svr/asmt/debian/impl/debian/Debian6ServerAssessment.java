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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Hoon Oh          11월 10, 2021		First Draft.
 */
package io.playce.roro.svr.asmt.debian.impl.debian;

import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.config.DistributionConfig;
import io.playce.roro.svr.asmt.dto.result.DebianAssessmentResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Component("DEBIAN6Assessment")
public class Debian6ServerAssessment extends DebianServerAssessment {
    public Debian6ServerAssessment(DistributionConfig config) {
        super(config);
    }

    @Override
    public Map<String, String> generateCommand() {
        return super.generateCommand();
    }

    @Override
    public DebianAssessmentResult assessment(TargetHost targetHost) throws InterruptedException {
        return super.assessment(targetHost);
    }
}
//end of DebianServerAssessment6.java