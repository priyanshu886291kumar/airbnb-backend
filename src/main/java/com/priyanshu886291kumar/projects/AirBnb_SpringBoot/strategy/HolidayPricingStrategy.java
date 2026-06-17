package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.strategy;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Inventories.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{
    private final PricingStrategy wrapped;
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = inventory.getPrice();
        boolean isTodayHoliday = true; // Call an API or check with local data
        if(isTodayHoliday){
            price = price.multiply(BigDecimal.valueOf(1.25));
        }
        return price;

    }
}
