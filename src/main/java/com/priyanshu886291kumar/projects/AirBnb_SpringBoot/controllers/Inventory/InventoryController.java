package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.controllers.Inventory;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Inventory.InventoryDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Inventory.UpdateInventoryRequestDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.inventory.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final IInventoryService inventoryService;

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<InventoryDTO>> getAllInventoryByRoom(@PathVariable Long roomId){
        return  ResponseEntity.ok(inventoryService. getAllInventoryByRoom(roomId));
    }

    @PatchMapping("/rooms/{roomId}")
    public ResponseEntity<Void>updateInventory(@PathVariable Long roomId, @RequestBody UpdateInventoryRequestDTO inventoryRequestDTO){
        inventoryService.updateInventory(roomId,inventoryRequestDTO);
        return ResponseEntity.noContent().build();
    }

}
