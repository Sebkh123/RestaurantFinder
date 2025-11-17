package com.example.restaurantfinder.repo;


import com.example.restaurantfinder.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepo extends JpaRepository<RestaurantEntity, Long> {
    boolean existsByPlaceId(String placeId);
//    List<RestaurantEntity> findByPostNummer(String postNummer);


    @Query("SELECT r FROM RestaurantEntity r WHERE LOWER(r.postNummer) LIKE LOWER(CONCAT('%', :postNummer, '%'))")
    List<RestaurantEntity> findByPostNummer(String postNummer);

}
