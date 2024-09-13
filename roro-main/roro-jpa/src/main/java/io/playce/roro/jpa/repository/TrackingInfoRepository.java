package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.TrackingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingInfoRepository extends JpaRepository<TrackingInfo, Long> {

    List<TrackingInfo> findByInventoryProcessId(Long inventoryProcessId);
}
