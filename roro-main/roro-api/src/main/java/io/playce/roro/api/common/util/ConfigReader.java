package io.playce.roro.api.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.playce.roro.common.dto.k8s.ClusterConfig;

import java.io.IOException;

public class ConfigReader {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static ClusterConfig readClusterConfigFromJson(String config) {
        yamlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            return yamlMapper.readValue(config, ClusterConfig.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read the cluster config from the given JSON string", e);
        }
    }
}
