package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.model.RestaurantTable;
import com.restaurant.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableRepository tableRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantTable>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(tableRepo.findAll()));
    }
}
