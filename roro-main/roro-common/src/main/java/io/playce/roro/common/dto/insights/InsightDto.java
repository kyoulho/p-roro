package io.playce.roro.common.dto.insights;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class InsightDto {
    @JsonIgnore
    private String solutionType;
    private String solutionName;
    private String vendor;
    private String version;
    private Date gaDatetime;
    private Date eolDatetime;
    private Date eosDatetime;
    @Getter(AccessLevel.NONE)
    private Integer affectedResources;
    private String eol;
    private String eos;
    @Getter(AccessLevel.NONE)
    private List<Resource> resources;

    public Integer getAffectedResources() {
        return getResources().size();
    }

    public List<Resource> getResources() {
        if (resources == null) {
            resources = new ArrayList<>();
        }

        return resources;
    }
}

