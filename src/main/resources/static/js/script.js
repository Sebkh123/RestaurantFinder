function quickSort(arr, key) {
    if (arr.length <= 1) return arr;

    const pivot = arr[arr.length - 1];
    const left = arr.filter(item => item[key] < pivot[key]);
    const equal = arr.filter(item => item[key] === pivot[key]);
    const right = arr.filter(item => item[key] > pivot[key]);

    return [...quickSort(left, key), ...equal, ...quickSort(right, key)];
}
function sortRestaurants(option) {
    if (option === "rating") {
        restaurants = quickSort(restaurants, "rating").reverse();
    } else if (option === "price") {
        restaurants = quickSort(restaurants, "price_level");
    } else if (option === "distance") {
        restaurants = quickSort(restaurants, "distance");
    } else if (option === "weighted") {
        restaurants.forEach(r => {
            r.weightedScore = (r.rating * 0.7) + ((1 / r.distance) * 0.3);
        });
        restaurants = quickSort(restaurants, "weightedScore").reverse();
    }
    displayRestaurants(restaurants);
}
