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

import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.discover.middleware.dto.DetectResultInfo;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public interface MiddlewareDetector {

    DetectResultInfo generateMiddleware(TargetHost targetHost, InventoryProcessConnectionInfo connectionInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException;
}
//end of MiddlewareValidator.java