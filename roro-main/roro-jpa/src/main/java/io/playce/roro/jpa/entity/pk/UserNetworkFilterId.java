package io.playce.roro.jpa.entity.pk;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Setter @Getter
public class UserNetworkFilterId implements Serializable {
    private static final long serialVersionUID = -4409855869353413413L;
    @Column(name = "USER_ID", nullable = false)
    private Long userId;
    @Column(name = "RESOURCE_TYPE_CODE", nullable = false, length = 8)
    private String resourceTypeCode;
    @Column(name = "RESOURCE_ID", nullable = false)
    private Long resourceId;

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, userId, resourceTypeCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserNetworkFilterId entity = (UserNetworkFilterId) o;
        return Objects.equals(this.resourceId, entity.resourceId) &&
                Objects.equals(this.userId, entity.userId) &&
                Objects.equals(this.resourceTypeCode, entity.resourceTypeCode);
    }
}