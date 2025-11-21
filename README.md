En full-stack webapplikation, der hjælper brugere med at finde og sortere restauranter i nærheden af københavnske postnumre ved hjælp af forskellige algoritmer og filtreringsmuligheder.

Kernefunktionalitet
•	Søg efter postnummer: Find restauranter i København baseret på postnummer
•	Flere sorteringsalgoritmer:
          -	Bedømmelse (Høj → Lav)
          -	Prisniveau (Lav → Høj)
          -	Distance (Tæt på → Længere væk)
          -	Vægtet score (kombinerer bedømmelse og distance)
          -	K-Nearest Neighbors (KNN)

Avancerede funktioner
          •	Filtrering i realtid: Filtrer efter minimumsbedømmelse og maksimal pris
          •	Geolokation: Brug din nuværende position til distanceberegning
          •	Interaktivt kort: Se alle restauranter på et interaktivt Leaflet-kort
          •	Restaurantdetaljer–modal: Klik på en restaurant for at se detaljeret information
          •	Inputvalidering: Validering i realtid med visuel feedback (grønne/røde kanter)
          •	Fejlhåndtering: Omfattende fejlbeskeder ved ugyldige inputs og API-fejl

Implementerede algoritmer
          1. Merge Sort
Fil: AlgorithmPipeline.java
Bruges til alle sorteringsoperationer (rating, pris, distance, vægtet score).

          2. Quick Sort
Fil: script.js
Bruges til klient-side sortering i frontend.

          3. K-Nearest Neighbors (KNN)
Fil: AlgorithmPipeline.java
Finder de K nærmeste restauranter baseret på geografisk distance.
