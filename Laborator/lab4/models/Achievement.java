// Achievement.java
package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement extends BaseEntity {

    // id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // name VARCHAR(100) NOT NULL
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // description TEXT
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // icon VARCHAR(50)
    @Size(max = 50)
    @Column(name = "icon", length = 50)
    private String icon;

    // criteria JSONB -- {"type":"streak","value":7}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criteria", columnDefinition = "jsonb")
    private Map<String, Object> criteria;

    // points INTEGER DEFAULT 10
    @Min(0)
    @Column(name = "points", nullable = false)
    @Builder.Default
    private Integer points = 10;

    @Min(0)
    @Column(name = "gem_reward", nullable = false)
    @Builder.Default
    private Integer gemReward = 0;
}