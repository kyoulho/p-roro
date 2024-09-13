package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.SurveyProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SurveyProcessRepository extends JpaRepository<SurveyProcess, Long> {
    Optional<SurveyProcess> findBySurveyIdAndServiceId(Long surveyId, Long serviceId);
}