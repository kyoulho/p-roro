/*
 * Copyright 2023 The playce-roro-k8s-assessment Project.
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
 * Dong-Heon Han    Jul 13, 2023		First Draft.
 */

package io.playce.roro.k8s.command.factory;

import io.playce.roro.k8s.command.CommandFactory;
import io.playce.roro.k8s.command.enums.COMMAND_KEY;
import io.playce.roro.k8s.config.Command;

import java.util.List;

public class DatabaseFactory implements CommandFactory {

    @Override
    public List<Command> getCommand(COMMAND_KEY key) {
        return null;
    }
}