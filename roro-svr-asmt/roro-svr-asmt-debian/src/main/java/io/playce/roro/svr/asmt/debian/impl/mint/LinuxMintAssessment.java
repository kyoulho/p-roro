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
 * SangCheon Park   Nov 29, 2022		    First Draft.
 */
package io.playce.roro.svr.asmt.debian.impl.mint;

import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.config.DistributionConfig;
import io.playce.roro.svr.asmt.debian.impl.ubuntu.UbuntuServerAssessment;
import io.playce.roro.svr.asmt.dto.result.UbuntuAssessmentResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Component("LINUXMINTAssessment")
public class LinuxMintAssessment extends UbuntuServerAssessment {

    public LinuxMintAssessment(DistributionConfig config) {
        super(config);
    }

    @Override
    public Map<String, String> generateCommand() {
        return super.generateCommand();
    }

    @Override
    public UbuntuAssessmentResult assessment(TargetHost targetHost) throws InterruptedException {
        return super.assessment(targetHost);
    }
}