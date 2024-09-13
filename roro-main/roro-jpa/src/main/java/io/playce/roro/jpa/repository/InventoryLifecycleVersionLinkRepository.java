package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.InventoryLifecycleVersionLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLifecycleVersionLinkRepository extends JpaRepository<InventoryLifecycleVersionLink, Long> {
}
