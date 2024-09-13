/*
 * Copyright 2023 The playce-roro Project.
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
 * Dong-Heon Han    Jul 27, 2023		First Draft.
 */

package io.playce.roro.k8s.parser.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.playce.roro.k8s.parser.Parser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VersionParser implements Parser {
    private final ObjectMapper objectMapper;

    @Override
    public JsonNode parse(String result) {
        if(result == null) return null;

        String[] lines = result.split("\n");
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for(String line: lines) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            String[] split = line.split(": ");
            if(split.length != 2) continue;

            objectNode.set(split[0], objectNode.textNode(split[1]));
            arrayNode.add(objectNode);
        }
        return arrayNode;
    }
}