package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "third_party_solution")
@Where(clause = "delete_yn='N'")
@ToString
public class ThirdPartySolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long thirdPartySolutionId;

    @Column
    private String thirdPartySolutionName;

    @Column
    private String vendor;

    @Column
    private String description;

    @Column
    private String deleteYn;

    @Column
    private Long registUserId;

    @Column
    private Date registDatetime;

    @Column
    private Long modifyUserId;

    @Column
    private Date modifyDatetime;

}
