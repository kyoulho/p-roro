package io.playce.roro.common.dto.migration;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MigrationProcessListResponseDto {

    private Data data;

    @Getter
    @Setter
    public static class Data {

        private Long totalCount;
        private List<MigrationJobDto> contents;

    }

}
