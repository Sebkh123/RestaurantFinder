let restaurants = [];
let allRestaurants = [];  // Store unfiltered results
let bigMap;
let bigMapMarkers = [];
let userLocation = null;  // Store user location for KNN

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
   LOADING SPINNER
--------------------------------------------------- */
function showLoading() {
    document.getElementById("loadingSpinner").style.display = "flex";
}

function hideLoading() {
    document.getElementById("loadingSpinner").style.display = "none";
}

/* ---------------------------------------------------
   ERROR HANDLING
--------------------------------------------------- */
function showError(message, details = "") {
    const container = document.getElementById("results");
    container.innerHTML = `
        <div class="error-message">
            <strong>⚠️ ${message}</strong>
            ${details ? `<p>${details}</p>` : ""}
        </div>
    `;
}

/* ---------------------------------------------------
   POSTAL CODE VALIDATION
--------------------------------------------------- */
function isValidPostalCode(postNummer) {
    // Danish postal codes are 4 digits between 1000 and 9999
    const pattern = /^\d{4}$/;
    if (!pattern.test(postNummer)) {
        return false;
    }
    const num = parseInt(postNummer);
    return num >= 1000 && num <= 9999;
}

/* ---------------------------------------------------
   SEARCH BUTTON CLICK
--------------------------------------------------- */
document.getElementById("searchBtn").addEventListener("click", async () => {
    const postNummer = document.getElementById("postNummerInput").value.trim();
    const method = document.getElementById("sortSelect").value;

    // Validate postal code is entered
    if (!postNummer) {
        showError("Missing Postal Code", "Please enter a valid postnummer (e.g., 2000, 2100)");
        return;
    }

    // Validate postal code format
    if (!isValidPostalCode(postNummer)) {
        showError("Invalid Postal Code", "Please enter a valid 4-digit Danish postal code between 1000 and 9999.");
        return;
    }

    // Validate KNN requirements
    if (method === "knn") {
        if (!userLocation) {
            showError("Location Required for KNN", "Please click '📍 Use My Location' first to enable K-Nearest Neighbors search.");
            return;
        }
        const k = parseInt(document.getElementById("kValue").value);
        if (k < 1 || k > 50) {
            showError("Invalid K Value", "Please enter a value between 1 and 50.");
            return;
        }
    }

    showLoading();

    try {
        let url = `http://localhost:8080/api/restaurants/sort?postNummer=${postNummer}&method=${method}`;

        // Add location parameters for distance-based methods
        if (method === "distance" || method === "weighted" || method === "knn") {
            if (userLocation) {
                url += `&lat=${userLocation.lat}&lng=${userLocation.lng}`;
            }
        }

        // Add k parameter for KNN
        if (method === "knn") {
            const k = document.getElementById("kValue").value;
            url += `&k=${k}`;
        }

        const response = await fetch(url);

        if (!response.ok) {
            // Try to get error message from server
            let errorMessage = `Server error: ${response.status}`;
            try {
                const errorText = await response.text();
                if (errorText) {
                    errorMessage = errorText;
                }
            } catch (e) {
                // If we can't read the error, use default message
            }

            if (response.status === 404) {
                throw new Error("No restaurants found for this postal code.");
            } else if (response.status === 400) {
                throw new Error(errorMessage);
            } else {
                throw new Error(errorMessage);
            }
        }

        allRestaurants = await response.json();
        restaurants = [...allRestaurants];

        if (restaurants.length === 0) {
            hideLoading();
            showError("No Restaurants Found", `No restaurants available for postal code ${postNummer}.`);
            return;
        }

        // Apply filters
        applyFilters();

        // SHOW MAP FIRST so Leaflet can render properly
        const bigMapDiv = document.getElementById("bigMap");
        bigMapDiv.style.display = "block";

        displayResults(restaurants);
        updateBigMap(restaurants);

        hideLoading();

    } catch (error) {
        hideLoading();
        console.error(error);
        showError("Error Loading Restaurants", error.message);
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

/* ---------------------------------------------------
   GEOLOCATION BUTTON
--------------------------------------------------- */
document.getElementById("geoBtn").addEventListener("click", () => {
    showLoading();
    navigator.geolocation.getCurrentPosition(
        pos => {
            userLocation = {
                lat: pos.coords.latitude,
                lng: pos.coords.longitude
            };
            computeDistances(userLocation.lat, userLocation.lng);
            hideLoading();
            document.getElementById("geoBtn").innerHTML = "✓ Location Set";
            document.getElementById("geoBtn").style.color = "#28a745";
        },
        error => {
            hideLoading();
            showError("Location Error", "Could not get your location. Please enable location services.");
        }
    );
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

/* ---------------------------------------------------
   KNN CONTROLS TOGGLE
--------------------------------------------------- */
document.getElementById("sortSelect").addEventListener("change", (e) => {
    const knnControls = document.getElementById("knnControls");
    if (e.target.value === "knn") {
        knnControls.style.display = "block";
    } else {
        knnControls.style.display = "none";
    }
});

/* ---------------------------------------------------
   FILTERS
--------------------------------------------------- */
function applyFilters() {
    const minRating = parseFloat(document.getElementById("minRating").value);
    const maxPrice = parseInt(document.getElementById("maxPrice").value);

    restaurants = allRestaurants.filter(r => {
        return r.rating >= minRating && r.priceLevel <= maxPrice;
    });

    displayResults(restaurants);
    updateBigMap(restaurants);
}

// Filter change listeners
document.getElementById("minRating").addEventListener("input", (e) => {
    document.getElementById("ratingValue").textContent = e.target.value;
    if (allRestaurants.length > 0) {
        applyFilters();
    }
});

document.getElementById("maxPrice").addEventListener("input", (e) => {
    const priceLabels = ["", "$", "$$", "$$$", "$$$$"];
    document.getElementById("priceValue").textContent = priceLabels[e.target.value];
    if (allRestaurants.length > 0) {
        applyFilters();
    }
});

// Clear filters button
document.getElementById("clearFilters").addEventListener("click", () => {
    document.getElementById("minRating").value = 0;
    document.getElementById("maxPrice").value = 4;
    document.getElementById("ratingValue").textContent = "0";
    document.getElementById("priceValue").textContent = "$$$$";

    if (allRestaurants.length > 0) {
        restaurants = [...allRestaurants];
        displayResults(restaurants);
        updateBigMap(restaurants);
    }
});

/* ---------------------------------------------------
   MODAL
--------------------------------------------------- */
const modal = document.getElementById("restaurantModal");
const closeBtn = document.getElementsByClassName("close")[0];

function openModal(restaurant) {
    const modalBody = document.getElementById("modalBody");

    modalBody.innerHTML = `
        <h2>${restaurant.name}</h2>

        <div class="modal-section">
            <h3>📍 Location</h3>
            <p><strong>Address:</strong> ${restaurant.address}</p>
            <p><strong>Postal Code:</strong> ${restaurant.postNummer}</p>
            <p><strong>Coordinates:</strong> ${restaurant.lat.toFixed(4)}, ${restaurant.lng.toFixed(4)}</p>
        </div>

        <div class="modal-section">
            <h3>⭐ Rating & Price</h3>
            <p><strong>Rating:</strong> ${renderStars(restaurant.rating)}</p>
            <p><strong>Price Level:</strong> ${renderPrice(restaurant.priceLevel)}</p>
            ${restaurant.distance ? `<p><strong>Distance from you:</strong> ${restaurant.distance.toFixed(2)} km</p>` : ""}
        </div>

        <div style="margin-top: 20px;">
            <a class="map-link" href="https://www.google.com/maps?q=${restaurant.lat},${restaurant.lng}" target="_blank">
                Open in Google Maps
            </a>
        </div>
    `;

    modal.style.display = "block";
}

closeBtn.onclick = function() {
    modal.style.display = "none";
}

window.onclick = function(event) {
    if (event.target == modal) {
        modal.style.display = "none";
    }
}

/* ---------------------------------------------------
   UPDATE DISPLAY RESULTS WITH MODAL BUTTON
--------------------------------------------------- */
function displayResults(restaurants) {
    const container = document.getElementById("results");

    if (!restaurants.length) {
        container.innerHTML = `
            <div class="info-message">
                <strong>🔍 No Restaurants Match Your Filters</strong>
                <p>Try adjusting your filters or search for a different postal code.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = "";

    restaurants.forEach((r, index) => {
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

            <button class="view-details-btn" data-index="${index}">View Details</button>
            <a class="map-link"
                href="https://www.google.com/maps?q=${r.lat},${r.lng}"
                target="_blank">
                Open in Google Maps
            </a>
        `;

        container.appendChild(div);
    });

    // Add event listeners to all view details buttons
    document.querySelectorAll(".view-details-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            const index = parseInt(e.target.getAttribute("data-index"));
            openModal(restaurants[index]);
        });
    });
}

/* ---------------------------------------------------
   REAL-TIME INPUT VALIDATION
--------------------------------------------------- */
document.getElementById("postNummerInput").addEventListener("input", (e) => {
    const input = e.target;
    const value = input.value.trim();

    // Remove any previous validation styling
    input.style.borderColor = "";

    // Only validate if user has typed something
    if (value.length > 0) {
        if (isValidPostalCode(value)) {
            input.style.borderColor = "#28a745"; // Green border for valid
        } else if (value.length >= 4) {
            input.style.borderColor = "#dc3545"; // Red border for invalid
        }
    }
});

// Clear validation styling on focus
document.getElementById("postNummerInput").addEventListener("focus", (e) => {
    e.target.style.borderColor = "";
});
