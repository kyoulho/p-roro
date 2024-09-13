package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class ExcludedExcelSheet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long excludedExcelSheetId;
    private Long inventoryId;
    private String sheetName;
}
