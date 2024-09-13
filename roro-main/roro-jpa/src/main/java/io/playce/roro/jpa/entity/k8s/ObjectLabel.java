package io.playce.roro.jpa.entity.k8s;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ObjectLabel {
    @Id
    Long objectId;
    private String name;
    private String value;
}
