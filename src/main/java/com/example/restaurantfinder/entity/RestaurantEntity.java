package com.example.restaurantfinder.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.NoArgsConstructor;




@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "restaurants")
public class RestaurantEntity {





    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String placeId;
    private Integer priceLevel;
    private String name;
    private String address;
    private double rating;
    private double lat;
    private double lng;
    private String postNummer;

    // constructors

    public RestaurantEntity(String name, String address, double rating, double lat, double lng, Integer priceLevel, String postNummer) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.lat = lat;
        this.lng = lng;
        this.priceLevel = priceLevel;
        this.postNummer = postNummer;
    }

}
