package io.playce.roro.common.dto.hostscan;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.StringTokenizer;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveredHostDto implements Comparable<DiscoveredHostDto> {
    @Schema(title = "Ip Address", description = "발견된 호스트의 Ip Address")
    private String ipAddress;

    @Schema(title = "Os Name", description = "발견된 호스트의 운영체제 이름")
    private String osName;

    @ArraySchema(schema = @Schema(implementation = RegisteredServerDto.class))
    private List<RegisteredServerDto> registeredServers;

    @Override
    public int compareTo(@NotNull DiscoveredHostDto that) {
        StringTokenizer st1 = new StringTokenizer(this.getIpAddress(), ".");
        StringTokenizer st2 = new StringTokenizer(that.getIpAddress(), ".");

        while (st1.hasMoreTokens()) {
            String octet1 = st1.nextToken();
            String octet2 = st2.nextToken();

            if (!octet1.equals(octet2)) {
                return Integer.parseInt(octet1) - Integer.parseInt(octet2);
            }
        }
        return 0;
    }
}
