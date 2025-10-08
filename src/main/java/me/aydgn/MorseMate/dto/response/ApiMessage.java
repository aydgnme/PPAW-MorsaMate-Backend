package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMessage {
    private String message;

    public static ApiMessage of(String msg) {
        return ApiMessage.builder().message(msg).build();
    }
}