package io.playce.roro.prerequisite.config;

import io.playce.roro.common.exception.RoRoException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "prerequisite")
@Getter
@Setter
public class PrerequisiteConfig {
    private Map<String, String> idLikeMap;
    private List<Software> softwares;

    @Setter
    @Getter
    public static class Common {
        protected String name;
        protected String type;
    }

    @Setter
    @Getter
    @ToString
    public static class Software extends Common {
        private List<String> commands;
    }

    public Software getSoftware(String type, String name) {
        if (softwares == null) {
            throw new RoRoException("check config file - name: prerequisite.yml");
        }

        for (Software software : softwares) {
            if (software.getType().equals(type) && software.getName().equals(name)) {
                return software;
            }
        }
        throw new RoRoException(String.format("check config file - type: %s, name: %s", type, name));
    }
}