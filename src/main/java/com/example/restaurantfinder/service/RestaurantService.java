package com.example.restaurantfinder.service;

import com.example.restaurantfinder.dtomodel.RestaurantDto;
import com.example.restaurantfinder.algorithm.AlgorithmPipeline;
import com.example.restaurantfinder.entity.RestaurantEntity;
import com.example.restaurantfinder.repo.RestaurantRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantService {


    @Autowired
    private RestaurantRepo repository;

    private static final String API_KEY = System.getenv("GOOGLE_API_KEY");


    private static final String GEOCODE_URL =  "https://maps.googleapis.com/maps/api/geocode/json";



    private double[] geocodeZip(String postNummer) {
        try {
            // Check if API key is set
            if (API_KEY == null || API_KEY.isEmpty()) {
                throw new RuntimeException("Google API Key is not configured. Please set GOOGLE_API_KEY environment variable.");
            }

            RestTemplate rest = new RestTemplate();

            String address = postNummer + " Copenhagen Denmark";
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);

            String url = "https://maps.googleapis.com/maps/api/geocode/json"
                    + "?address=" + encoded
                    + "&key=" + API_KEY;

            System.out.println("Geocode URL: " + url);

            String json = rest.getForObject(url, String.class);
            System.out.println("Geocode response: " + json);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            // Check API response status
            String status = root.path("status").asText();
            if (!"OK".equals(status)) {
                throw new RuntimeException("Geocoding failed with status: " + status +
                    ". This postal code may not exist or be valid for Copenhagen, Denmark.");
            }

            JsonNode results = root.path("results");
            if (results.size() == 0) {
                throw new RuntimeException("No results found for postal code: " + postNummer);
            }

            JsonNode locNode = results.get(0)
                    .path("geometry").path("location");

            double lat = locNode.path("lat").asDouble();
            double lng = locNode.path("lng").asDouble();

            System.out.println("Geocoded to: lat=" + lat + ", lng=" + lng);

            return new double[]{lat, lng};

        } catch (Exception e) {
            System.err.println("Geocoding error for postal code " + postNummer + ": " + e.getMessage());
            throw new RuntimeException("Failed to geocode postal code " + postNummer + ": " + e.getMessage(), e);
        }
    }





    private List<JsonNode> fetchNearbyRestaurants(double lat, double lng) {
        try {
            RestTemplate rest = new RestTemplate();

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                    .queryParam("location", lat + "," + lng)
                    .queryParam("radius", 2000)         // 2km radius
                    .queryParam("type", "restaurant")
                    .queryParam("keyword", "restaurant")
                    .queryParam("key", API_KEY)
                    .build(true)
                    .toUriString();

            String json = rest.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode results = mapper.readTree(json).path("results");

            List<JsonNode> list = new ArrayList<>();
            results.forEach(list::add);
            return list;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch restaurants", e);
        }
    }
    public List<RestaurantDto> fetchAndSaveRestaurants(String postNummer) {

        System.out.println("Geocoding Zip: " + postNummer);

        double[] coords = geocodeZip(postNummer);
        double lat = coords[0];
        double lng = coords[1];

        System.out.println("Coordinates: " + lat + ", " + lng);

        List<JsonNode> results = fetchNearbyRestaurants(lat, lng);

        List<RestaurantEntity> entities = new ArrayList<>();

        for (JsonNode r : results) {

            String placeId = r.path("place_id").asText();


            // Skip duplicates
            if (repository.existsByPlaceId(placeId)) continue;

            String name = r.path("name").asText();
            String address = r.path("vicinity").asText("");
            double rating = r.path("rating").asDouble(0.0);

            double restLat = r.path("geometry").path("location").path("lat").asDouble();
            double restLng = r.path("geometry").path("location").path("lng").asDouble();
            Integer priceLevel = r.path("price_level").asInt(-1);

            RestaurantEntity entity = RestaurantEntity.builder()
                    .placeId(placeId)
                    .name(name)
                    .address(address)
                    .rating(rating)
                    .lat(restLat)
                    .lng(restLng)
                    .priceLevel(priceLevel)
                    .postNummer(postNummer)
                    .build();

            entities.add(entity);
        }

        repository.saveAll(entities);

        return entities.stream()
                .map(e -> new RestaurantDto(
                        e.getName(),
                        e.getAddress(),
                        e.getRating(),
                        e.getLat(),
                        e.getLng(),
                        e.getPriceLevel(),
                        e.getPostNummer()
                ))
                .toList();
    }



    /** Retrieve all stored restaurants from DB */
    public List<RestaurantDto> getAllRestaurants() {
        return repository.findAll().stream()
                .map(e -> new RestaurantDto(e.getName(), e.getAddress(),
                        e.getRating(), e.getLat(), e.getLng(), e.getPriceLevel(), e.getPostNummer()))
                .toList();
    }


    public List<RestaurantDto> getRestaurantsByPostNummer(String postNummer) {
        return repository.findByPostNummer(postNummer)
                .stream()
                .map(e -> new RestaurantDto(
                        e.getName(),
                        e.getAddress(),
                        e.getRating(),
                        e.getLat(),
                        e.getLng(),
                        e.getPriceLevel(),
                        e.getPostNummer()
                ))
                .toList();
    }





    /*algoirthm pipeline*/

    public List<RestaurantDto> sortRestaurants(
            String postNummer,
            String method,
            Double lat,
            Double lng,
            Integer k
    ) {




        method= method.toLowerCase().trim();
        postNummer = postNummer.toLowerCase().trim();
        List<RestaurantDto> restaurants = getRestaurantsByPostNummer(postNummer);

        if ((method.equals("distance") || method.equals("weighted") || method.equals("knn"))
                && (lat == null || lng == null)) {
            throw new IllegalArgumentException("lat and lng are required for method: " + method);
        }


        if(restaurants.isEmpty()){
          return  fetchAndSaveRestaurants(postNummer);

        }

        System.out.println("Sort method='" + method + "', postNummer='" + postNummer + "'");
        System.out.println("Restaurants loaded for sorting: " + restaurants.size());



        switch (method) {

            case "distance":
                return AlgorithmPipeline.MergeSort.sort(
                        restaurants,
                        AlgorithmPipeline.RestaurantComparators.byDistance(lat, lng)
                );

            case "rating":
                return AlgorithmPipeline.MergeSort.sort(
                        restaurants,
                        AlgorithmPipeline.RestaurantComparators.byRating()
                );

            case "price":
                return AlgorithmPipeline.MergeSort.sort(
                        restaurants,
                        AlgorithmPipeline.RestaurantComparators.byPrice()
                );

            case "weighted":
                return AlgorithmPipeline.MergeSort.sort(
                        restaurants,
                        AlgorithmPipeline.RestaurantComparators.weighted(lat, lng)
                );

            case "knn":
                return AlgorithmPipeline.kNearest(restaurants, lat, lng, k);

            default:
                throw new IllegalArgumentException("Unknown sorting method: " + method);
        }
    }


}
