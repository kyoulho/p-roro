package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long>, JpaSpecificationExecutor<Setting> {

    List<Setting> findByParentSettingIdOrderByDisplayOrder(Long parentSettingId);

}
