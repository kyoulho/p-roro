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
public class DiskMonitoringId implements Serializable {
    private static final long serialVersionUID = -5215912758705610406L;
    @Column(name = "SERVER_INVENTORY_ID", nullable = false)
    private Long serverInventoryId;

    @Column(name = "MONITORING_DATETIME", nullable = false)
    private Date monitoringDatetime;

    @Column(name = "DEVICE_NAME", nullable = false, length = 200)
    private String deviceName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DiskMonitoringId entity = (DiskMonitoringId) o;
        return Objects.equals(this.monitoringDatetime, entity.monitoringDatetime) &&
                Objects.equals(this.serverInventoryId, entity.serverInventoryId) &&
                Objects.equals(this.deviceName, entity.deviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monitoringDatetime, serverInventoryId, deviceName);
    }

}