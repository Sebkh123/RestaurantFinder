package com.example.restaurantfinder;

import com.example.restaurantfinder.algorithm.AlgorithmPipeline;
import com.example.restaurantfinder.dtomodel.RestaurantDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlgorithmBenchmark {

    private static final Random random = new Random(42);

    // Copenhagen area coordinates for realistic test data
    private static final double BASE_LAT = 55.6761;
    private static final double BASE_LNG = 12.5683;
    private static final double COORD_RANGE = 0.5;

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("RESTAURANT FINDER - ALGORITHM BENCHMARK");
        System.out.println("=".repeat(80));
        System.out.println();

        // Run benchmarks with different data sizes
        int[] dataSizes = {10, 50, 100, 500, 1000, 5000};

        for (int size : dataSizes) {
            System.out.println("\n" + "─".repeat(80));
            System.out.println("DATA SIZE: " + size + " restaurants");
            System.out.println("─".repeat(80));

            List<RestaurantDto> testData = generateTestData(size);
            runAllBenchmarks(testData);
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("BENCHMARK COMPLETE");
        System.out.println("=".repeat(80));
    }

    private static void runAllBenchmarks(List<RestaurantDto> testData) {
        double userLat = BASE_LAT;
        double userLng = BASE_LNG;

        // Benchmark: MergeSort with byPrice comparator
        benchmarkMergeSort(testData, "Price",
            AlgorithmPipeline.RestaurantComparators.byPrice());

        // Benchmark: MergeSort with byRating comparator
        benchmarkMergeSort(testData, "Rating",
            AlgorithmPipeline.RestaurantComparators.byRating());

        // Benchmark: MergeSort with byDistance comparator
        benchmarkMergeSort(testData, "Distance",
            AlgorithmPipeline.RestaurantComparators.byDistance(userLat, userLng));

        // Benchmark: MergeSort with weighted comparator
        benchmarkMergeSort(testData, "Weighted",
            AlgorithmPipeline.RestaurantComparators.weighted(userLat, userLng));

        // Benchmark: KNN with different k values
        int[] kValues = {5, 10, 20, Math.min(50, testData.size() / 2)};
        for (int k : kValues) {
            if (k < testData.size()) {
                benchmarkKNN(testData, userLat, userLng, k);
            }
        }

        // Benchmark: Distance calculations
        benchmarkDistanceCalculations(testData, userLat, userLng);
    }

    private static void benchmarkMergeSort(List<RestaurantDto> data,
                                          String comparatorName,
                                          java.util.Comparator<RestaurantDto> comparator) {
        // Warm-up
        for (int i = 0; i < 3; i++) {
            AlgorithmPipeline.MergeSort.sort(data, comparator);
        }

        // Benchmark
        int iterations = 10;
        long totalTime = 0;

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            AlgorithmPipeline.MergeSort.sort(data, comparator);
            long end = System.nanoTime();
            totalTime += (end - start);
        }

        double avgTimeMs = (totalTime / iterations) / 1_000_000.0;
        System.out.printf("  MergeSort (%-10s): %8.3f ms (avg of %d runs)%n",
            comparatorName, avgTimeMs, iterations);
    }

    private static void benchmarkKNN(List<RestaurantDto> data,
                                    double userLat,
                                    double userLng,
                                    int k) {
        // Warm-up
        for (int i = 0; i < 3; i++) {
            AlgorithmPipeline.kNearest(data, userLat, userLng, k);
        }

        // Benchmark
        int iterations = 10;
        long totalTime = 0;

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            AlgorithmPipeline.kNearest(data, userLat, userLng, k);
            long end = System.nanoTime();
            totalTime += (end - start);
        }

        double avgTimeMs = (totalTime / iterations) / 1_000_000.0;
        System.out.printf("  KNN (k=%-4d)        : %8.3f ms (avg of %d runs)%n",
            k, avgTimeMs, iterations);
    }

    private static void benchmarkDistanceCalculations(List<RestaurantDto> data,
                                                     double userLat,
                                                     double userLng) {
        int operations = data.size() * 100;

        // Benchmark: fastDistance
        long start = System.nanoTime();
        for (int i = 0; i < operations; i++) {
            RestaurantDto r = data.get(i % data.size());
            AlgorithmPipeline.fastDistance(r.getLat(), r.getLng(), userLat, userLng);
        }
        long end = System.nanoTime();
        double fastTimeMs = (end - start) / 1_000_000.0;

        // Benchmark: normal distance
        start = System.nanoTime();
        for (int i = 0; i < operations; i++) {
            RestaurantDto r = data.get(i % data.size());
            AlgorithmPipeline.distance(r.getLat(), r.getLng(), userLat, userLng);
        }
        end = System.nanoTime();
        double normalTimeMs = (end - start) / 1_000_000.0;

        System.out.printf("  Distance (fast)    : %8.3f ms (%d operations)%n",
            fastTimeMs, operations);
        System.out.printf("  Distance (normal)  : %8.3f ms (%d operations)%n",
            normalTimeMs, operations);
        System.out.printf("  Speedup factor     : %.2fx faster without sqrt%n",
            normalTimeMs / fastTimeMs);
    }

    private static List<RestaurantDto> generateTestData(int count) {
        List<RestaurantDto> restaurants = new ArrayList<>();
        String[] names = {
            "Pizza Place", "Sushi Bar", "Burger Joint", "Taco Stand",
            "Italian Bistro", "French Cafe", "Thai Restaurant", "Chinese Wok",
            "Indian Curry House", "Greek Taverna", "Mexican Cantina", "Steakhouse"
        };

        for (int i = 0; i < count; i++) {
            String name = names[i % names.length] + " #" + i;
            String address = "Street " + i + ", Copenhagen";
            double rating = 1.0 + (random.nextDouble() * 4.0);
            double lat = BASE_LAT + (random.nextDouble() - 0.5) * COORD_RANGE;
            double lng = BASE_LNG + (random.nextDouble() - 0.5) * COORD_RANGE;
            int priceLevel = 1 + random.nextInt(4);
            String postNummer = String.format("%04d", 2000 + random.nextInt(1000));

            restaurants.add(new RestaurantDto(name, address, rating, lat, lng, priceLevel, postNummer));
        }

        return restaurants;
    }

    public static class ComplexityAnalysis {

        public static void runComplexityAnalysis() {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("COMPLEXITY ANALYSIS - Growth Rate Verification");
            System.out.println("=".repeat(80));

            int[] sizes = {100, 200, 400, 800, 1600, 3200};
            double userLat = BASE_LAT;
            double userLng = BASE_LNG;

            System.out.println("\nMergeSort Expected: O(n log n)");
            System.out.println("Size\tTime(ms)\tRatio\tExpected(n log n)");
            System.out.println("-".repeat(60));

            double prevTime = 0;
            int prevSize = 0;

            for (int size : sizes) {
                List<RestaurantDto> data = generateTestData(size);

                long start = System.nanoTime();
                for (int i = 0; i < 5; i++) {
                    AlgorithmPipeline.MergeSort.sort(data,
                        AlgorithmPipeline.RestaurantComparators.byRating());
                }
                long end = System.nanoTime();

                double avgTime = ((end - start) / 5) / 1_000_000.0;

                if (prevTime > 0) {
                    double actualRatio = avgTime / prevTime;
                    double sizeRatio = (double) size / prevSize;
                    double expectedRatio = (size * Math.log(size)) / (prevSize * Math.log(prevSize));

                    System.out.printf("%d\t%.3f\t\t%.2f\t%.2f%n",
                        size, avgTime, actualRatio, expectedRatio);
                } else {
                    System.out.printf("%d\t%.3f\t\t-\t-%n", size, avgTime);
                }

                prevTime = avgTime;
                prevSize = size;
            }

            System.out.println("\nKNN Expected: O(n log k)");
            System.out.println("Size\tTime(ms)\tRatio\tExpected(n log k)");
            System.out.println("-".repeat(60));

            prevTime = 0;
            prevSize = 0;
            int k = 10;

            for (int size : sizes) {
                List<RestaurantDto> data = generateTestData(size);

                long start = System.nanoTime();
                for (int i = 0; i < 5; i++) {
                    AlgorithmPipeline.kNearest(data, userLat, userLng, k);
                }
                long end = System.nanoTime();

                double avgTime = ((end - start) / 5) / 1_000_000.0;

                if (prevTime > 0) {
                    double actualRatio = avgTime / prevTime;
                    double sizeRatio = (double) size / prevSize;
                    double expectedRatio = (size * Math.log(k)) / (prevSize * Math.log(k));

                    System.out.printf("%d\t%.3f\t\t%.2f\t%.2f%n",
                        size, avgTime, actualRatio, expectedRatio);
                } else {
                    System.out.printf("%d\t%.3f\t\t-\t-%n", size, avgTime);
                }

                prevTime = avgTime;
                prevSize = size;
            }
        }
    }

    public static class MemoryAnalysis {

        public static void runMemoryAnalysis() {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("MEMORY USAGE ANALYSIS");
            System.out.println("=".repeat(80));

            Runtime runtime = Runtime.getRuntime();
            int[] sizes = {100, 500, 1000, 5000, 10000};
            double userLat = BASE_LAT;
            double userLng = BASE_LNG;

            System.out.println("\nSize\tMergeSort(KB)\tKNN(KB)\t\tOriginal(KB)");
            System.out.println("-".repeat(60));

            for (int size : sizes) {
                List<RestaurantDto> data = generateTestData(size);

                runtime.gc();
                long baseMemory = runtime.totalMemory() - runtime.freeMemory();

                long memBefore = runtime.totalMemory() - runtime.freeMemory();
                List<RestaurantDto> sorted = AlgorithmPipeline.MergeSort.sort(data,
                    AlgorithmPipeline.RestaurantComparators.byRating());
                long memAfter = runtime.totalMemory() - runtime.freeMemory();
                long mergeSortMem = (memAfter - memBefore) / 1024;

                runtime.gc();

                memBefore = runtime.totalMemory() - runtime.freeMemory();
                List<RestaurantDto> knn = AlgorithmPipeline.kNearest(data, userLat, userLng, 10);
                memAfter = runtime.totalMemory() - runtime.freeMemory();
                long knnMem = (memAfter - memBefore) / 1024;

                long originalSize = (data.size() * 100) / 1024;

                System.out.printf("%d\t%d\t\t%d\t\t%d%n",
                    size, mergeSortMem, knnMem, originalSize);
            }
        }
    }
}