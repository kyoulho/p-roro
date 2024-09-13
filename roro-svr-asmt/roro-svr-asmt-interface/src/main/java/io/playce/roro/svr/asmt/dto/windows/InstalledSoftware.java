package io.playce.roro.svr.asmt.dto.windows;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InstalledSoftware {

    private String displayName;
    private String displayVersion;
    private String publisher;
    private String installDate;

}
