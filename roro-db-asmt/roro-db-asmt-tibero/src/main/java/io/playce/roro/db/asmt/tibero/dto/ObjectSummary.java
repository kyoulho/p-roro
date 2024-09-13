package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObjectSummary {

    private int databaseLink;
    private int directory;
    private int function;
    private int index;
    private int java;
    private int lob;
    private int packages;
    private int packageBody;
    private int procedure;
    private int sequence;
    private int sqlTranslationProfile;
    private int synonym;
    private int table;
    private int trigger;
    private int type;
    private int typeBody;
    private int view;

}