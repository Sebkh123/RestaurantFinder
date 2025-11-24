# RestaurantFinder

RestaurantFinder er en full-stack webapplikation udviklet i Spring Boot, JavaScript og MySQL/H2. Formålet er at hente restauranter fra Google Places API, gemme dem i en database og sortere dem efter forskellige algoritmer såsom rating, distance, weighted score og k-nearest neighbors (KNN). Frontend-delen indeholder et interaktivt Leaflet-kort og dynamisk sortering i browseren.

---

## Indholdsfortegnelse
- [Formål](#formål)
- [Funktioner](#funktioner)
- [Teknologier](#teknologier)
- [Projektstruktur](#projektstruktur)
- [Setup og Installation](#setup-og-installation)
- [Brug af Applikationen](#brug-af-applikationen)
- [API Endpoints](#api-endpoints)
- [Database](#database)
- [Algoritmer](#algoritmer)

---

## Formål

RestaurantFinder gør det muligt at:
- Søge efter restauranter i et givent københavnsk postnummer  
- Sortere restauranter ud fra flere algoritmer (rating, distance, weighted score, KNN)  
- Placere restauranter på et interaktivt kort  
- Beregne afstand til restauranter baseret på brugerens geolokation  

Projektet er designet til at demonstrere arbejde med datastrukturer, algoritmer, REST API'er samt frontend/backend-integration.

---

## Funktioner

### Backend-funktionalitet
- Hentning af restauranter via Google Places API  
- Lagring af data i database  
- Sortering af resultater via MergeSort og KNN  
- Distanceberegning via Euclidisk afstand (backend)  

### Frontend-funktionalitet
- Søg efter restauranter via postnummer  
- Sortering efter:
  - Rating
  - Distance (kræver geolokation)
  - Weighted score (kombination af rating og distance)
  - KNN (find de k nærmeste)
- Interaktivt Leaflet-kort med restaurant-markers  
- Visualisering af rating som stjerner  
- Visning af prisniveau  
- Direkte link til Google Maps  
- Brug af geolokation til afstandsberegning  

---

## Teknologier

### Backend
- Java 17  
- Spring Boot  
- Spring Data JPA  
- H2 Database  
- Lombok  
- Google Places API og Geocoding API  

### Frontend
- HTML5  
- CSS3  
- JavaScript  
- Leaflet.js til kortvisning  

---

## Projektstruktur

```plaintext
restaurantfinder/
 ├── src/main/java/com/example/restaurantfinder/
 │   ├── controller/         # REST endpoints
 │   ├── service/            # Google API-kald, DTO mapping, algoritmer
 │   ├── repo/               # Database repositories
 │   ├── entity/             # RestaurantEntity
 │   ├── dtomodel/           # RestaurantDto
 │   └── algorithm/          # MergeSort, KNN, distanceberegning
 │
 └── src/main/resources/
     ├── static/             # HTML, CSS, JS, Leaflet maps
     ├── application.properties
     └── data.sql (valgfrit)
```

---

## Setup og Installation

Klon projektet:

```
git clone https://github.com/yourusername/restaurant-finder.git
cd restaurant-finder
```

Tilføj Google API-nøgle:
Som environment variable:

```
GOOGLE_API_KEY=your_api_key_here
```

Byg projektet:

```
mvn clean install
```

Start applikationen:

```
mvn spring-boot:run
```

Åbn applikationen:

```
http://localhost:8080
```

---

## Brug af Applikationen

1. Søg efter restauranter Indtast et københavnsk postnummer (fx 2100) Systemet henter data fra Google Places API og gemmer dem i databasen
2. Brug din position Klik "Use My Location" Browseren spørger om adgang til din placering Afstand og KNN beregnes ud fra dine koordinater
3. Sorter resultater
   * Rating (høj → lav)
   * Distance (tæt på → længere væk)
   * Weighted score (kombinerer rating og distance)
   * KNN (find de k nærmeste)
4. Se detaljer om restauranter Navn, adresse, rating (stjerner), prisniveau "Open in Google Maps" for navigation
5. Interaktivt kort Alle restauranter vises på Leaflet-kort Klik på markers for at se navn og adresse

---

## API Endpoints

```
GET /api/restaurants?postNummer={postcode}
```

Hent og gem restauranter for et postnummer.

```
GET /api/getAll
```

Hent alle gemte restauranter.

```
GET /api/restaurants/filter?postNummer={postcode}
```

Filtrer restauranter på postnummer.

```
GET /api/restaurants/sort?postNummer={postcode}&method={method}&lat={latitude}&lng={longitude}&k={k}
```

Sorter restauranter via backend-algoritmer.
Metoder:
* rating
* distance
* weighted
* knn
Parametre:
* `lat` og `lng` kræves for distance, weighted og knn
* `k` er valgfrit for knn

---

## Database

Restaurants lagres i `restaurants` tabellen med felter:
* id
* place_id
* name
* address
* rating
* lat
* lng
* price_level
* post_nummer
Default-database: H2 (kan skiftes til MySQL)

---

## Algoritmer

MergeSort (backend)
Bruges til sortering af restauranter efter rating, distance eller weighted score Implementeret i `AlgorithmPipeline.java`

QuickSort (frontend)
Bruges til sortering i browseren Implementeret i `script.js`

K-Nearest Neighbors (KNN)
Finder de k nærmeste restauranter baseret på euclidisk afstand Implementeret i `AlgorithmPipeline.java`

Distanceberegning
Backend: Euclidisk afstand (hurtig) Frontend: Euclidisk og Haversine (når præcision er nødvendig)
