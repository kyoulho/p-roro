package io.playce.roro.jpa.entity.pk;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Embeddable
@Setter @Getter
@ToString
public class CpuMonitoringId implements Serializable {
    private static final long serialVersionUID = -1410004283660279223L;
    @Column(name = "SERVER_INVENTORY_ID", nullable = false)
    private Long serverInventoryId;

    @Column(name = "MONITORING_DATETIME", nullable = false)
    private Date monitoringDatetime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CpuMonitoringId entity = (CpuMonitoringId) o;
        return Objects.equals(this.monitoringDatetime, entity.monitoringDatetime) &&
                Objects.equals(this.serverInventoryId, entity.serverInventoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monitoringDatetime, serverInventoryId);
    }

}