package com.example.restaurantfinder.algorithm;

import com.example.restaurantfinder.Dtomodel.RestaurantDto;

import java.util.*;

public class AlgorithmPipeline {

    // ---------------------------
    // Fast distance (no sqrt)
    // ---------------------------
    public static double fastDistance(double lat1, double lng1, double lat2, double lng2) {
        double dx = lat2 - lat1;
        double dy = lng2 - lng1;
        return dx * dx + dy * dy;
    }

    // ---------------------------
    // Normal Euclidean distance
    // ---------------------------
    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        return Math.sqrt(
                (lat1 - lat2) * (lat1 - lat2) +
                        (lng1 - lng2) * (lng1 - lng2)
        );
    }

    // ---------------------------
    // Comparators
    // ---------------------------
    public static class RestaurantComparators {

        public static Comparator<RestaurantDto> byPrice() {
            return Comparator.comparingInt(RestaurantDto::getPriceLevel);
        }

        public static Comparator<RestaurantDto> byDistance(double userLat, double userLng) {
            return Comparator.comparingDouble(
                    r -> distance(r.getLat(), r.getLng(), userLat, userLng)
            );
        }

        public static Comparator<RestaurantDto> byRating() {
            return Comparator.comparingDouble(RestaurantDto::getRating).reversed();
        }

        public static Comparator<RestaurantDto> weighted(double userLat, double userLng) {
            return Comparator.comparingDouble(r -> {
                double dist = distance(r.getLat(), r.getLng(), userLat, userLng);

                // GOOD = high rating, short distance, low price
                return (5 - r.getRating()) * 2
                        + dist
                        + (r.getPriceLevel() * 0.5);
            });
        }
    }

    // ---------------------------
    // Merge Sort
    // ---------------------------
    public static class MergeSort {

        public static List<RestaurantDto> sort(List<RestaurantDto> list, Comparator<RestaurantDto> comparator) {
            if (list.size() <= 1) return new ArrayList<>(list);

            int mid = list.size() / 2;

            List<RestaurantDto> left = sort(list.subList(0, mid), comparator);
            List<RestaurantDto> right = sort(list.subList(mid, list.size()), comparator);

            return merge(left, right, comparator);
        }

        private static List<RestaurantDto> merge(
                List<RestaurantDto> left,
                List<RestaurantDto> right,
                Comparator<RestaurantDto> comparator) {

            List<RestaurantDto> result = new ArrayList<>();
            int i = 0, j = 0;

            while (i < left.size() && j < right.size()) {
                if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                    result.add(left.get(i++));
                } else {
                    result.add(right.get(j++));
                }
            }

            result.addAll(left.subList(i, left.size()));
            result.addAll(right.subList(j, right.size()));

            return result;
        }
    }

    // ---------------------------
    // K-Nearest Neighbor (KNN)
    // ---------------------------
    public static List<RestaurantDto> kNearest(
            List<RestaurantDto> restaurants,
            double userLat,
            double userLng,
            int k) {

        PriorityQueue<RestaurantDto> pq = new PriorityQueue<>(
                (a, b) -> Double.compare(
                        fastDistance(b.getLat(), b.getLng(), userLat, userLng),
                        fastDistance(a.getLat(), a.getLng(), userLat, userLng)
                )
        );

        for (RestaurantDto r : restaurants) {
            pq.add(r);
            if (pq.size() > k) pq.poll();
        }

        return new ArrayList<>(pq);
    }
}
