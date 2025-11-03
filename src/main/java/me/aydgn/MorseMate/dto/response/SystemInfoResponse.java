package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemInfoResponse {
    private String applicationName;
    private String version;
    private String environment;
    private LocalDateTime serverTime;
    private String javaVersion;
    private String springBootVersion;
}
