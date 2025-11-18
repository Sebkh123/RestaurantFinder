package com.example.restaurantfinder.controller;


import com.example.restaurantfinder.dtomodel.RestaurantDto;
import com.example.restaurantfinder.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController

@RequestMapping("/api")
public class RestaurantController {

    @Autowired
    RestaurantService restaurantService;


    @GetMapping("/restaurants" )
    public ResponseEntity<List<RestaurantDto>> getRestaurants(@RequestParam String postNummer) {

        List<RestaurantDto> response =restaurantService.fetchAndSaveRestaurants(postNummer);
        return ResponseEntity.ok(response);
    }


    // Retrieve all saved restaurants
    @GetMapping("/getAll")
    public List<RestaurantDto> getAll() {
        return restaurantService.getAllRestaurants();
    }



    @GetMapping("/restaurants/filter")
    public List<RestaurantDto> getByPostNummer(@RequestParam String postNummer) {
        return restaurantService.getRestaurantsByPostNummer(postNummer);
    }



    // Sort already-saved restaurants
    @GetMapping("/restaurants/sort")
    public List<RestaurantDto> sortRestaurants(
            @RequestParam String postNummer,
            @RequestParam String method,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Integer k
    ) {
        return restaurantService.sortRestaurants(postNummer, method, lat, lng, k);


    }





}
