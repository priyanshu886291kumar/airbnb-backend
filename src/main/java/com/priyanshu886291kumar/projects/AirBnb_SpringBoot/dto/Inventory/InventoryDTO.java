package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Inventory;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventoryDTO {
    private Long id;
    private LocalDate date;
    private Integer bookedCount;
    private Integer reservedCount;
    private Integer totalCount;
    private BigDecimal surgeFactor;
    private BigDecimal price; //Price = basePrice*surgeFactor
    private Boolean closed;
    private LocalDateTime createdAt;
    private  LocalDateTime updatedAt;
}
