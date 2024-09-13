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
 * Dong-Heon Han    Jul 28, 2023		First Draft.
 */

package io.playce.roro.k8s.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class K8sJsonUtil {

    public static String getStringValue(JsonNode node, String childName) {
        JsonNode child = getJsonNode(node, childName);
        if (child == null) return null;
        return child.textValue();
    }

    public static Long getLongValue(JsonNode node, String childName) {
        JsonNode child = getJsonNode(node, childName);
        if (child == null) return null;
        return child.asLong();
    }

    public static Integer getIntValue(JsonNode node, String childName) {
        JsonNode child = getJsonNode(node, childName);
        if (child == null) return null;
        return child.asInt();
    }

    @Nullable
    private static JsonNode getJsonNode(JsonNode node, String childName) {
        JsonNode child = node.get(childName);
        if(child instanceof MissingNode) {
            log.error("{} child is missingNode", node);
            return null;
        }
        return child;
    }
}