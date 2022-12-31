package com.navaship.api.packages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PackageResponse {
    private Long id;
    private String name;
    private BigDecimal weight;
    private BigDecimal value;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
