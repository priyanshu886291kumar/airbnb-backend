package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.strategy;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Inventories.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;


@RequiredArgsConstructor
public class SurgePricingStrategy implements PricingStrategy{
    private final PricingStrategy wrapped;
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        return wrapped.calculatePrice(inventory)
                .multiply(inventory.getSurgeFactor());
    }
}
