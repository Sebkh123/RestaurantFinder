let restaurants = [];
let bigMap;
let bigMapMarkers = [];

/* ---------------------------------------------------
   QUICK SORT
--------------------------------------------------- */
function quickSort(arr, key) {
    if (arr.length <= 1) return arr;

    const pivot = arr[arr.length - 1];
    const left = arr.filter(item => item[key] < pivot[key]);
    const equal = arr.filter(item => item[key] === pivot[key]);
    const right = arr.filter(item => item[key] > pivot[key]);

    return [...quickSort(left, key), ...equal, ...quickSort(right, key)];
}

/* ---------------------------------------------------
   REAL DISTANCE CALCULATION
--------------------------------------------------- */
function getDistanceFromLatLng(lat1, lng1, lat2, lng2) {
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLng = (lng2 - lng1) * Math.PI / 180;

    const a =
        Math.sin(dLat / 2) ** 2 +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLng / 2) ** 2;

    return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
}

/* ---------------------------------------------------
   COMPUTE DISTANCES USING USER LOCATION
--------------------------------------------------- */
function computeDistances(userLat, userLng) {
    restaurants = restaurants.map(r => ({
        ...r,
        distance: getDistanceFromLatLng(userLat, userLng, r.lat, r.lng)
    }));

    const sortType = document.getElementById("sortSelect").value;
    sortRestaurants(sortType);
}

/* ---------------------------------------------------
   SEARCH BUTTON CLICK
--------------------------------------------------- */
document.getElementById("searchBtn").addEventListener("click", async () => {
    const postNummer = document.getElementById("postNummerInput").value.trim();

    if (!postNummer) {
        alert("Please enter a postnummer.");
        return;
    }

    const method = "rating";
    const url = `http://localhost:8080/api/restaurants/sort?postNummer=${postNummer}&method=${method}`;

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error("Failed to fetch restaurants");

        restaurants = await response.json();

        // SHOW MAP FIRST so Leaflet can render properly
        const bigMapDiv = document.getElementById("bigMap");
        bigMapDiv.style.display = "block";


        displayResults(restaurants);
        updateBigMap(restaurants);

        // Auto-sort after loading
        const currentSort = document.getElementById("sortSelect").value;
        sortRestaurants(currentSort);

    } catch (error) {
        console.error(error);
        document.getElementById("results").innerHTML =
            `<p class="error">Error loading restaurants.</p>`;
    }
});

/* ---------------------------------------------------
   SORTING FUNCTION
--------------------------------------------------- */
function sortRestaurants(option) {
    if (!restaurants.length) return;

    if (option === "rating") {
        restaurants = quickSort(restaurants, "rating").reverse();
    }

    else if (option === "price") {
        restaurants = quickSort(restaurants, "priceLevel");
    }

    else if (option === "distance") {
        restaurants = quickSort(restaurants, "distance");
    }

    else if (option === "weighted") {
        restaurants = restaurants.map(r => ({
            ...r,
            weightedScore: (r.rating * 0.7) + ((1 / r.distance) * 0.3)
        }));
        restaurants = quickSort(restaurants, "weightedScore").reverse();
    }

    displayResults(restaurants);
    updateBigMap(restaurants);
}

/* Dropdown listener */
document.getElementById("sortSelect").addEventListener("change", (e) => {
    sortRestaurants(e.target.value);
});

/* ---------------------------------------------------
   DISPLAY RESULTS
--------------------------------------------------- */
function displayResults(restaurants) {
    const container = document.getElementById("results");

    if (!restaurants.length) {
        container.innerHTML = `<p>No restaurants found.</p>`;
        return;
    }

    container.innerHTML = "";

    restaurants.forEach(r => {
        const div = document.createElement("div");
        div.className = "restaurant-item";

        div.innerHTML = `
            <h2>${r.name}</h2>

            <p><strong>📍 Address:</strong> ${r.address}</p>
            <p><strong>⭐ Rating:</strong> ${renderStars(r.rating)}</p>
            <p><strong>💲 Price Level:</strong> ${renderPrice(r.priceLevel)}</p>

            ${
            r.distance
                ? `<p><strong>📏 Distance:</strong> ${r.distance.toFixed(2)} km</p>`
                : ""
        }

            <p><strong>Postnummer:</strong> ${r.postNummer}</p>

            <a class="map-link"
                href="https://www.google.com/maps?q=${r.lat},${r.lng}"
                target="_blank">
                Open in Google Maps
            </a>
        `;

        container.appendChild(div);
    });
}

/* ---------------------------------------------------
   GEOLOCATION BUTTON
--------------------------------------------------- */
document.getElementById("geoBtn").addEventListener("click", () => {
    navigator.geolocation.getCurrentPosition(pos => {
        computeDistances(pos.coords.latitude, pos.coords.longitude);
    });
});

/* ---------------------------------------------------
   LEAFLET MAP
--------------------------------------------------- */
function initBigMap() {
    if (!bigMap) {
        bigMap = L.map('bigMap').setView([55.6761, 12.5683], 12);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors'
        }).addTo(bigMap);
    }
}

function updateBigMap(restaurants) {
    initBigMap();

    // Remove old markers
    bigMapMarkers.forEach(marker => bigMap.removeLayer(marker));
    bigMapMarkers = [];

    restaurants.forEach(r => {
        if (r.lat && r.lng) {
            const marker = L.marker([r.lat, r.lng])
                .addTo(bigMap)
                .bindPopup(`<strong>${r.name}</strong><br>${r.address}`);

            bigMapMarkers.push(marker);
        }
    });

    // Zoom to marker bounds
    const group = new L.featureGroup(bigMapMarkers);
    if (bigMapMarkers.length > 0) {
        bigMap.fitBounds(group.getBounds(), { padding: [50, 50] });
    }
}

/* ---------------------------------------------------
   HELPER FUNCTIONS
--------------------------------------------------- */
function renderStars(rating) {
    const full = Math.floor(rating);
    const half = (rating % 1) >= 0.5 ? 1 : 0;
    const empty = 5 - full - half;

    return (
        "⭐".repeat(full) +
        (half ? "✩" : "") +
        "☆".repeat(empty)
    ) + ` (${rating.toFixed(1)})`;
}

function renderPrice(priceLevel) {
    if (priceLevel < 0) return "N/A";
    return "$".repeat(priceLevel + 1);
}
