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
 * SangCheon Park   Feb 22, 2022		    First Draft.
 */
package io.playce.roro.app.asmt.java.threadpool.task;

import io.playce.roro.app.asmt.java.policy.Policy;
import io.playce.roro.app.asmt.result.ApplicationAssessmentResult;
import io.playce.roro.common.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class DependencyCheckTaskTest {

    public static void main(String[] args) throws IOException {
        String path = DependencyCheckTaskTest.class.getResource("/Test.txt").getPath();

        System.err.println(path);

        File f = new File(path);
        Policy policy = new Policy(new ArrayList<>());
        ApplicationAssessmentResult result = new ApplicationAssessmentResult();

        DependencyCheckTask task = new DependencyCheckTask(f, "txt", f.getParentFile().getAbsolutePath(), policy, result, new ArrayList<>());

        task.taskRun();

        System.err.println(JsonUtil.objToJson(result, true));
    }
}
//end of DependencyCheckTaskTest.java