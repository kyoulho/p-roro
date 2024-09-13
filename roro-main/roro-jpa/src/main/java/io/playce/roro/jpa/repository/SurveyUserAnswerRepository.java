package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.SurveyUserAnswer;
import io.playce.roro.jpa.entity.pk.SurveyUserAnswerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyUserAnswerRepository extends JpaRepository<SurveyUserAnswer, SurveyUserAnswerId> {

    @Modifying
    @Query(value = "DELETE FROM survey_user_answer WHERE survey_process_id = :surveyProcessId", nativeQuery = true)
    void deleteAllBySurveyProcessId(Long surveyProcessId);
}