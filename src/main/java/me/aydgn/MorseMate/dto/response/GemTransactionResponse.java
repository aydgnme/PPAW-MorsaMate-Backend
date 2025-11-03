package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.GemTransaction;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GemTransactionResponse {

    private Long id;
    private Long userId;
    private Integer amount;
    private String transactionType;
    private String source;
    private String description;
    private LocalDateTime createdAt;

    public static GemTransactionResponse from(GemTransaction transaction) {
        if (transaction == null) return null;

        return GemTransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUser() != null ? transaction.getUser().getId() : null)
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType() != null ?
                        transaction.getTransactionType().name() : null)
                .source(transaction.getSource())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
