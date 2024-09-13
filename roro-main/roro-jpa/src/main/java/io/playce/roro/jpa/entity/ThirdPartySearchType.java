package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "third_party_search_type")
@Where(clause = "delete_yn='N'")
@ToString
public class ThirdPartySearchType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long thirdPartySearchTypeId;

    @Column
    private Long thirdPartySolutionId;

    @Column
    private String searchType;

    @Column
    private String searchValue;

    @Column
    private String inventoryTypeCode;

    @Column
    private String windowsYn;

    @Column
    private String deleteYn;

}
