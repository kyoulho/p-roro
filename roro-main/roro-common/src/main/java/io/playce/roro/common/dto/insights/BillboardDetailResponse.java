package io.playce.roro.common.dto.insights;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BillboardDetailResponse {
    List<BillboardDetail> server;
    List<BillboardDetail> middleware;
    List<BillboardDetail> database;
    List<BillboardDetail> java;

}
