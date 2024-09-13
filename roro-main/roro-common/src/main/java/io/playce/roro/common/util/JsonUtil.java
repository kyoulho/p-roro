/*
 * Copyright 2020 The Playce-RoRo Project.
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
 * SangCheon Park   Nov 26, 2020		First Draft.
 */
package io.playce.roro.common.util;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.playce.roro.common.exception.RoRoException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * <pre>
 * json convert utility class.
 * </pre>
 *
 * @author SangCheon Park
 * @version 1.0
 */
@Slf4j
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * <pre>
     * convert json string to list.
     * </pre>
     *
     * @param json
     * @param parametrized
     * @param parameterClasses
     * @return
     */
    public static <T> T jsonToList(String json, Class<?> parametrized, Class<?>... parameterClasses) throws InterruptedException {
        try {
            TypeFactory typeFactory = MAPPER.getTypeFactory();
            return MAPPER.readValue(json, typeFactory.constructParametricType(parametrized, parameterClasses));
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <pre>
     * convert json string to class Object.
     * </pre>
     *
     * @param <T>
     * @param json
     * @param valueType
     * @return
     */
    public static <T> T jsonToObj(String json, Class<T> valueType) throws InterruptedException {
        try {
            return MAPPER.readValue(json, valueType);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <pre>
     * convert JsonNode to class object
     * </pre>
     *
     * @param node
     * @param typeRef
     * @param <T>
     * @return
     */
    public static <T> T treeToObj(JsonNode node, TypeReference<T> typeRef) {
        try {
            return MAPPER.treeToValue(node, (Class<T>) ((ParameterizedType) typeRef.getType()).getRawType());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <pre>
     * convert json string to TypeReference Object.
     * </pre>
     *
     * @param json
     * @param typeRef
     * @return
     */
    public static <T> T jsonToObj(String json, TypeReference<T> typeRef) throws InterruptedException {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <pre>
     * convert Object to json String.
     * </pre>
     *
     * @param obj
     * @return
     */
    public static String objToJson(Object obj) throws IOException {
        return objToJson(obj, false);
    }

    /**
     * <pre>
     * convert Object to json String.
     * </pre>
     *
     * @param obj
     * @return
     */
    public static String objToJson(Object obj, boolean pretty) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonGenerator generator = MAPPER.getFactory().createGenerator(outputStream, JsonEncoding.UTF8);

        if (pretty) {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(generator, obj);
        } else {
            MAPPER.writeValue(generator, obj);
        }

        return outputStream.toString(JsonEncoding.UTF8.getJavaName());
    }

    /**
     * <pre>
     * Method to deserialize JSON content as tree expressed using set of JsonNode instances.
     * </pre>
     *
     * @param json JSON content
     * @return
     */
    public static JsonNode readTree(String json) {
        try {
            return MAPPER.readTree(StringUtils.defaultString(json, "[]"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String writeValueAsString(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <pre>
     * convert object to json node
     * </pre>
     *
     * @param value the value
     * @return json node
     */
    public static JsonNode convertToJsonNode(Object value) {
        return MAPPER.convertValue(value, JsonNode.class);
    }

    /**
     * <pre>
     * Create an array node.
     * </pre>
     *
     * @return array node
     */
    public static ArrayNode createArrayNode() throws InterruptedException {
        try {
            return MAPPER.createArrayNode();
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <pre>
     * Create an object node.
     * </pre>
     *
     * @return object node
     */
    public static ObjectNode createObjectNode() throws InterruptedException {
        try {
            return MAPPER.createObjectNode();
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert value t.
     *
     * @param <T>   the type parameter
     * @param value the value
     * @param clazz the clazz
     * @return the t
     */
    public static <T> T convertValue(Object value, Class<T> clazz) throws InterruptedException {
        try {
            return MAPPER.convertValue(value, clazz);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert value t.
     *
     * @param <T>            the type parameter
     * @param value          the value
     * @param toValueTypeRef the to value type ref
     * @return the t
     */
    public static <T> T convertValue(Object value, TypeReference<?> toValueTypeRef) throws InterruptedException {
        try {
            return (T) MAPPER.convertValue(value, toValueTypeRef);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, String>> getNodeValueFromJsonNode(JsonNode node, List<String> names) {
        List<Map<String, String>> result = new ArrayList<>();
        if (node.isArray()) {
            Iterator<JsonNode> iter = node.elements();
            while (iter.hasNext()) {
                JsonNode n = iter.next();
                createDataMap(names, n, result);
            }
        } else {
            createDataMap(names, node, result);
        }
        return result;
    }

    private static void createDataMap(List<String> names, JsonNode n, List<Map<String, String>> result) {
        Map<String, String> map = new HashMap<>();
        boolean flag = false;
        for (String name : names) {
            String value = getValueFromJsonNode(n, name);
            map.put(name, value);
            if (value != null) {
                flag = true;
            }
        }
        if (flag) {
            result.add(map);
        }
    }

    public static String getValueFromJsonNode(JsonNode node, String name) {
        JsonNode value = node.get(name);
        if (value == null) {
            return null;
        }
        return value.asText();
    }

    public static JSONArray getJsonArray(JSONArray jsonArray) {
        return jsonArray == null ? new JSONArray() : jsonArray;
    }

    public static JSONObject getJsonObject(String xmlToJson) {
        JSONObject jsonObject = null;
        try {
            jsonObject = getJsonObject((JSONObject) new JSONParser().parse(xmlToJson));
        } catch (ParseException e) {
            log.error("xml parse error: {}", e.getMessage(), e);
            throw new RoRoException(e.getMessage());
        }
        return jsonObject;
    }

    public static JSONObject getJsonObject(JSONObject jsonObject) {
        return jsonObject == null ? new JSONObject() : jsonObject;
    }

    public static boolean isJsonArray(String json) {
        return json.startsWith("[");
    }

    public static String writeValueAsStringExcludeFields(Object object, String... fieldNames) {
        JsonNode jsonNode = MAPPER.convertValue(object, JsonNode.class);
        JsonNode modifiedNode = removeFields(jsonNode, fieldNames);
        try {
            return MAPPER.writeValueAsString(modifiedNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode removeFields(JsonNode node, String[] fieldNames) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;

            for (String fieldName : fieldNames) {
                objectNode.remove(fieldName);
            }

            objectNode.fields().forEachRemaining(entry -> {
                JsonNode childNode = entry.getValue();
                removeFields(childNode, fieldNames);
            });
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode childNode : arrayNode) {
                removeFields(childNode, fieldNames);
            }
        }
        return node;
    }
}
//end of JsonUtil.java