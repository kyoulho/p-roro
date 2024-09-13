package io.playce.roro.jpa.entity.k8s;

import io.playce.roro.jpa.entity.pk.NamespaceObjectLinkId;
import lombok.*;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NamespaceObjectLink {
    @EmbeddedId
    private NamespaceObjectLinkId namespaceObjectLinkId;
}
