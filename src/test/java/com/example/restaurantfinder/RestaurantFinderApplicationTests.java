package com.example.restaurantfinder;

import com.example.restaurantfinder.algorithm.AlgorithmPipeline;
import com.example.restaurantfinder.dtomodel.RestaurantDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    // Test distance calculation
    @Test
    void testDistanceCalculation() {
        double lat1 = 55.6761;
        double lng1 = 12.5683;
        double lat2 = 55.6861;
        double lng2 = 12.5783;

        double distance = AlgorithmPipeline.distance(lat1, lng1, lat2, lng2);

        // Distance should be approximately 0.01414
        assertTrue(distance > 0.01 && distance < 0.02, "Distance should be between 0.01 and 0.02");
    }

    @Test
    void testFastDistanceCalculation() {
        double lat1 = 0.0;
        double lng1 = 0.0;
        double lat2 = 3.0;
        double lng2 = 4.0;

        double fastDist = AlgorithmPipeline.fastDistance(lat1, lng1, lat2, lng2);

        // Fast distance is squared: 3^2 + 4^2 = 25
        assertEquals(25.0, fastDist, 0.001);
    }

    @Test
    void testDistanceIsZeroForSameLocation() {
        double lat = 55.6761;
        double lng = 12.5683;

        double distance = AlgorithmPipeline.distance(lat, lng, lat, lng);
        assertEquals(0.0, distance, 0.0001);
    }

    // Test sorting by distance
    @Test
    void testSortByDistance() {
        double userLat = 55.6761;
        double userLng = 12.5683;

        List<RestaurantDto> input = List.of(
                new RestaurantDto("Far", "addr1", 4.0, 55.7, 12.6, 2, "2400"),
                new RestaurantDto("Close", "addr2", 4.0, 55.676, 12.568, 2, "2400"),
                new RestaurantDto("Medium", "addr3", 4.0, 55.68, 12.57, 2, "2400")
        );

        List<RestaurantDto> result = AlgorithmPipeline.MergeSort.sort(
                input,
                AlgorithmPipeline.RestaurantComparators.byDistance(userLat, userLng)
        );

        assertEquals("Close", result.get(0).getName());
        assertEquals("Medium", result.get(1).getName());
        assertEquals("Far", result.get(2).getName());
    }

    // Test weighted sorting
    @Test
    void testWeightedSorting() {
        double userLat = 55.6761;
        double userLng = 12.5683;

        List<RestaurantDto> input = List.of(
                new RestaurantDto("HighRatingFar", "addr1", 5.0, 55.7, 12.6, 2, "2400"),
                new RestaurantDto("LowRatingClose", "addr2", 2.0, 55.676, 12.568, 1, "2400"),
                new RestaurantDto("GoodBalance", "addr3", 4.5, 55.677, 12.569, 2, "2400")
        );

        List<RestaurantDto> result = AlgorithmPipeline.MergeSort.sort(
                input,
                AlgorithmPipeline.RestaurantComparators.weighted(userLat, userLng)
        );

        // Weighted should prioritize high rating, short distance, low price
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    // Test KNN algorithm
    @Test
    void testKNearestNeighbors() {
        double userLat = 55.6761;
        double userLng = 12.5683;

        List<RestaurantDto> input = List.of(
                new RestaurantDto("R1", "addr1", 4.0, 55.7, 12.6, 2, "2400"),
                new RestaurantDto("R2", "addr2", 4.0, 55.676, 12.568, 2, "2400"),
                new RestaurantDto("R3", "addr3", 4.0, 55.68, 12.57, 2, "2400"),
                new RestaurantDto("R4", "addr4", 4.0, 55.69, 12.58, 2, "2400"),
                new RestaurantDto("R5", "addr5", 4.0, 55.71, 12.59, 2, "2400")
        );

        List<RestaurantDto> result = AlgorithmPipeline.kNearest(input, userLat, userLng, 3);

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getName().equals("R2")));
    }

    @Test
    void testKNNWithKLargerThanListSize() {
        double userLat = 55.6761;
        double userLng = 12.5683;

        List<RestaurantDto> input = List.of(
                new RestaurantDto("R1", "addr1", 4.0, 55.7, 12.6, 2, "2400"),
                new RestaurantDto("R2", "addr2", 4.0, 55.676, 12.568, 2, "2400")
        );

        List<RestaurantDto> result = AlgorithmPipeline.kNearest(input, userLat, userLng, 5);

        assertEquals(2, result.size());
    }

    // Edge case tests
    @Test
    void testSortEmptyList() {
        List<RestaurantDto> input = new ArrayList<>();

        List<RestaurantDto> result = AlgorithmPipeline.MergeSort.sort(
                input,
                AlgorithmPipeline.RestaurantComparators.byRating()
        );

        assertEquals(0, result.size());
    }

    @Test
    void testSortSingleElement() {
        List<RestaurantDto> input = List.of(
                new RestaurantDto("Single", "addr", 4.0, 55.6761, 12.5683, 2, "2400")
        );

        List<RestaurantDto> result = AlgorithmPipeline.MergeSort.sort(
                input,
                AlgorithmPipeline.RestaurantComparators.byRating()
        );

        assertEquals(1, result.size());
        assertEquals("Single", result.get(0).getName());
    }

    @Test
    void testSortWithDuplicateRatings() {
        List<RestaurantDto> input = List.of(
                new RestaurantDto("A", "x", 4.0, 0, 0, 1, "2400"),
                new RestaurantDto("B", "x", 4.0, 0, 0, 1, "2400"),
                new RestaurantDto("C", "x", 4.0, 0, 0, 1, "2400")
        );

        List<RestaurantDto> result = AlgorithmPipeline.MergeSort.sort(
                input,
                AlgorithmPipeline.RestaurantComparators.byRating()
        );

        assertEquals(3, result.size());
        // All have same rating, so order should be stable or maintained
    }

    @Test
    void testSortWithDuplicatePrices() {
        List<RestaurantDto> input = List.of(
                new RestaurantDto("A", "x", 3.5, 0, 0, 2, "2400"),
                new RestaurantDto("B", "x", 4.5, 0, 0, 2, "2400"),
                new RestaurantDto("C", "x", 2.0, 0, 0, 2, "2400")
        );

        List<RestaurantDto> result = AlgorithmPipeline.MergeSort.sort(
                input,
                AlgorithmPipeline.RestaurantComparators.byPrice()
        );

        assertEquals(3, result.size());
        // All have same price level, verify all are present
        assertTrue(result.stream().anyMatch(r -> r.getName().equals("A")));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals("B")));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals("C")));
    }

    @Test
    void testKNNWithSingleRestaurant() {
        double userLat = 55.6761;
        double userLng = 12.5683;

        List<RestaurantDto> input = List.of(
                new RestaurantDto("Only", "addr", 4.0, 55.7, 12.6, 2, "2400")
        );

        List<RestaurantDto> result = AlgorithmPipeline.kNearest(input, userLat, userLng, 3);

        assertEquals(1, result.size());
        assertEquals("Only", result.get(0).getName());
    }

    @Test
    void testSortByRatingWithNegativeRatings() {
        List<RestaurantDto> input = List.of(
                new RestaurantDto("A", "x", 0.0, 0, 0, 1, "2400"),
                new RestaurantDto("B", "x", 5.0, 0, 0, 1, "2400"),
                new RestaurantDto("C", "x", 2.5, 0, 0, 1, "2400")
        );

        List<RestaurantDto> result = AlgorithmPipeline.MergeSort.sort(
                input,
                AlgorithmPipeline.RestaurantComparators.byRating()
        );

        assertEquals("B", result.get(0).getName());
        assertEquals("C", result.get(1).getName());
        assertEquals("A", result.get(2).getName());
    }

    @Test
    void testSortLargeList() {
        List<RestaurantDto> input = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            input.add(new RestaurantDto(
                    "Restaurant" + i,
                    "addr" + i,
                    Math.random() * 5,
                    55.6761 + Math.random() * 0.1,
                    12.5683 + Math.random() * 0.1,
                    (int) (Math.random() * 4) + 1,
                    "2400"
            ));
        }

        List<RestaurantDto> result = AlgorithmPipeline.MergeSort.sort(
                input,
                AlgorithmPipeline.RestaurantComparators.byRating()
        );

        assertEquals(100, result.size());
        // Verify sorting is correct
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getRating() >= result.get(i + 1).getRating());
        }
    }


}
