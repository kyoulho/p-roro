package io.playce.roro.jpa.entity.pk;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NamespaceObjectLinkId implements Serializable {
    private Long namespaceId;
    private Long objectId;
}
