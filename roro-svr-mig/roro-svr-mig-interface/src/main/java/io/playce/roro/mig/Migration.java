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
 * SangCheon Park   Mar 10, 2022		    First Draft.
 */
package io.playce.roro.mig;

import io.playce.roro.common.dto.inventory.process.MigrationProgressQueueItem;
import io.playce.roro.common.dto.migration.MigrationProcessDto;

import java.util.concurrent.BlockingQueue;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public interface Migration {

    MigrationProcessDto migration(MigrationProcessDto migration, BlockingQueue<MigrationProgressQueueItem> migrationProgressQueue) throws Exception;
}
//end of Migration.java