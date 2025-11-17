package com.example.restaurantfinder.dtomodel;

import lombok.*;




@Data


public class RestaurantDto {


    private final String name;
    private final String address;
    private final double rating;
    private final double lat;
    private final double lng;
    private final Integer priceLevel;
    private final String postNummer;


    public RestaurantDto(String name, String address, double rating, double lat, double lng, Integer priceLevel, String postnummer) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.lat = lat;
        this.lng = lng;
        this.priceLevel = priceLevel;
        this.postNummer = postnummer;
    }
}


