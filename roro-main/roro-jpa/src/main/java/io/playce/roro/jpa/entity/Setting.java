package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "setting")
@ToString
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settingId;

    @Column
    private Long parentSettingId;

    @Column
    private String categoryName;

    @Column
    private String propertyName;

    @Column
    private String propertyAliasEng;

    @Column
    private String propertyAliasKor;

    @Column
    private String propertyValue;

    @Column
    private String readOnlyYn;

    @Column
    private String dataType;

    @Column
    private String dataValues;

    @Column
    private String placeholderEng;

    @Column
    private String placeholderKor;

    @Column
    private String tooltipEng;

    @Column
    private String tooltipKor;

    @Column
    private Long displayOrder;

    @Column
    private Long registUserId;

    @Column
    private Date registDatetime;

    @Column
    private Long modifyUserId;

    @Column
    private Date modifyDatetime;

}
