package io.playce.roro.common.dto.prerequisite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class CheckStatus {
  @Schema(description = "icon status")
  private Icon icon;
  @Schema(description = "분석 결과 상태")
  private Result status;
  @Schema(description = "분석 중 오류메시지")
  private String message;

  public String toExcel() {
    return String.format("icon: %s\nstatus: %s\nmessage: %s", icon.name(), status.name(), message);
  }

  public enum Icon {
      SUCCESS("success"), WARN("warn"), FAIL("fail");

    private final String message;

    Icon(String message) {
      this.message = message;
    }

    public String toString() {
      return message;
    }
  }

  public enum Result {
      Okay("Okay"), Failed("Failed"), Enable("Enable"), Disable("Disable"), Installed("Installed"), NotInstalled("Not Installed");

    private final String message;

    Result(String message) {
      this.message = message;
    }

    public String toString() {
      return message;
    }
  }
}
