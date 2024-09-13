package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.HiddenField;
import io.playce.roro.jpa.entity.pk.HiddenFieldId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HiddenFieldRepository extends JpaRepository<HiddenField, HiddenFieldId> {
}