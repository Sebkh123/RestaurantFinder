package com.example.restaurantfinder;

import com.example.restaurantfinder.algorithm.AlgorithmPipeline;
import com.example.restaurantfinder.dtomodel.RestaurantDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RestaurantFinderApplicationTests {

    @Test
    void contextLoads() {
    }


    @Test
    void testSortByRating() {

        List<RestaurantDto> input = List.of(
                new RestaurantDto("A", "x", 3.5, 0, 0, 1, "2400"),
                new RestaurantDto("B", "x", 4.5, 0, 0, 1, "2400"),
                new RestaurantDto("C", "x", 2.0, 0, 0, 1, "2400")
        );

        List<RestaurantDto> result = AlgorithmPipeline.MergeSort.sort(
                input,
                AlgorithmPipeline.RestaurantComparators.byRating()
        );
        assertEquals("B", result.get(0).getName());
        assertEquals("A", result.get(1).getName());
        assertEquals("C", result.get(2).getName());

    }

    @Test
    void testSortByPrice() {

        List<RestaurantDto> input = List.of(
                new RestaurantDto("Cheap", "x", 3.5, 0, 0, 1, "2400"),
                new RestaurantDto("Medium", "x", 4.2, 0, 0, 2, "2400"),
                new RestaurantDto("Expensive", "x", 4.7, 0, 0, 4, "2400")
        );

        List<RestaurantDto> result = AlgorithmPipeline.MergeSort.sort(
                input,
                AlgorithmPipeline.RestaurantComparators.byPrice()
        );

        assertEquals("Cheap", result.get(0).getName());
        assertEquals("Medium", result.get(1).getName());
        assertEquals("Expensive", result.get(2).getName());
    }


}
