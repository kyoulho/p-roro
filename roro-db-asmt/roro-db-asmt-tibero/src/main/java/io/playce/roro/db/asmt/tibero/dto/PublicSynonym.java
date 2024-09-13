package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicSynonym {

    public String owner;
    public String synonymName;
    public String orgObjectOwner;
    public String orgObjectName;

}