package io.playce.roro.common.dto.insights;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Getter
@Setter
public class InsightListDto {
    @Getter(AccessLevel.NONE)
    private List<InsightDto> operatingSystems;
    @Getter(AccessLevel.NONE)
    private List<InsightDto> middlewares;
    @Getter(AccessLevel.NONE)
    private List<InsightDto> java;
    @Getter(AccessLevel.NONE)
    private List<InsightDto> databases;

    @JsonIgnore
    public List<InsightDto> getAllList() {
        return Stream.of(
                        Optional.ofNullable(operatingSystems).orElse(Collections.emptyList()),
                        Optional.ofNullable(middlewares).orElse(Collections.emptyList()),
                        Optional.ofNullable(java).orElse(Collections.emptyList()),
                        Optional.ofNullable(databases).orElse(Collections.emptyList())
                ).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<InsightDto> getOperatingSystems() {
        if (operatingSystems == null) {
            operatingSystems = new ArrayList<>();
        }

        return operatingSystems;
    }

    public List<InsightDto> getMiddlewares() {
        if (middlewares == null) {
            middlewares = new ArrayList<>();
        }

        return middlewares;
    }

    public List<InsightDto> getJava() {
        if (java == null) {
            java = new ArrayList<>();
        }

        return java;
    }

    public List<InsightDto> getDatabases() {
        if (databases == null) {
            databases = new ArrayList<>();
        }

        return databases;
    }
}
