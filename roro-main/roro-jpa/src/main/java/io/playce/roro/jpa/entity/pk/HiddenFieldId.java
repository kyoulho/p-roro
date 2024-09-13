package io.playce.roro.jpa.entity.pk;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Setter @Getter
@ToString
public class HiddenFieldId implements Serializable {
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    @Column(name = "INVENTORY_TYPE_CODE", nullable = false)
    private String inventoryTypeCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        HiddenFieldId entity = (HiddenFieldId) o;
        return Objects.equals(this.userId, entity.userId) &&
                Objects.equals(this.projectId, entity.projectId) &&
                Objects.equals(this.inventoryTypeCode, entity.inventoryTypeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, projectId, inventoryTypeCode);
    }

}