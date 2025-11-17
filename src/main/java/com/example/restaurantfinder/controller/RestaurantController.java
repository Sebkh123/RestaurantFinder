package com.example.restaurantfinder.controller;


import com.example.restaurantfinder.Dtomodel.RestaurantDto;
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

    @GetMapping("/getAll")
    public List<RestaurantDto> getAll() {
        return restaurantService.getAllRestaurants();
    }



    @GetMapping("/restaurants/filter")
    public List<RestaurantDto> getByPostNummer(@RequestParam String postNummer) {
        return restaurantService.getRestaurantsByPostNummer(postNummer);
    }



/*


Right now your /getAll returns the entire DB — bad if you scale.

Add:

GET /restaurants?page=0&size=20

GET /restaurants/filter?postNummer=2200&page=0&size=20

Use Spring’s Pageable.
 */




}
