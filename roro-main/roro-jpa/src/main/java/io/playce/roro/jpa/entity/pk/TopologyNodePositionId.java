package io.playce.roro.jpa.entity.pk;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@Embeddable
public class TopologyNodePositionId implements Serializable {
    private static final long serialVersionUID = 2001801258564796921L;
    @Column(name = "TYPE", nullable = false, length = 20)
    private String type;

    @Column(name = "TYPE_ID", nullable = false)
    private Long typeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TopologyNodePositionId entity = (TopologyNodePositionId) o;
        return Objects.equals(this.typeId, entity.typeId) &&
                Objects.equals(this.type, entity.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId, type);
    }

}