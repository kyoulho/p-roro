package io.playce.roro.svr.asmt.dto.linux.security;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Rule {
    private String target;
    private String prot;
    private String opt;
    private String source;
    private String destination;
}
