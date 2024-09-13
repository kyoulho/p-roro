package io.playce.roro.jpa.entity;

import io.playce.roro.jpa.entity.pk.HiddenFieldId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "HIDDEN_FIELD")
@Setter @Getter
@ToString
public class HiddenField {
    @EmbeddedId
    private HiddenFieldId id;

    @Column(name = "FIELD_NAMES")
    private String fieldNames;
}