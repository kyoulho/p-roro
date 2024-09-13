package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "discovered_third_party")
@ToString
public class DiscoveredThirdParty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discoveredThirdPartyId;

    @Column
    private Long inventoryProcessId;

    @Column
    private Long thirdPartySearchTypeId;

    @Column
    private String findContents;

}
