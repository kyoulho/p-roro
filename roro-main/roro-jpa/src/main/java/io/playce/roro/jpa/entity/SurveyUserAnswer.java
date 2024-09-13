package io.playce.roro.jpa.entity;

import io.playce.roro.jpa.entity.pk.SurveyUserAnswerId;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "survey_user_answer")
public class SurveyUserAnswer {
    @EmbeddedId
    private SurveyUserAnswerId id;

//TODO [JPA Buddy] generate columns from DB
}