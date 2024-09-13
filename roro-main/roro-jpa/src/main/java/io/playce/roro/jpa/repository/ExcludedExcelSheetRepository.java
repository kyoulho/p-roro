package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.ExcludedExcelSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExcludedExcelSheetRepository extends JpaRepository<ExcludedExcelSheet, Long> {
    List<ExcludedExcelSheet> findByInventoryId(Long inventoryId);

    void deleteByInventoryId(Long inventoryId);
}
