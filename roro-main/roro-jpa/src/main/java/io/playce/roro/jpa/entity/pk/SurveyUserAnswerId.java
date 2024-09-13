package io.playce.roro.jpa.entity.pk;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class SurveyUserAnswerId implements Serializable {
    private static final long serialVersionUID = 7229311300700753504L;
    @Column(name = "ANSWER_ID", nullable = false)
    private Long answerId;

    @Column(name = "SURVEY_ID", nullable = false)
    private Long surveyId;

    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId;

    @Column(name = "SURVEY_PROCESS_ID", nullable = false)
    private Long surveyProcessId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SurveyUserAnswerId entity = (SurveyUserAnswerId) o;
        return Objects.equals(this.answerId, entity.answerId) &&
                Objects.equals(this.surveyId, entity.surveyId) &&
                Objects.equals(this.questionId, entity.questionId) &&
                Objects.equals(this.surveyProcessId, entity.surveyProcessId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answerId, surveyId, questionId, surveyProcessId);
    }

}