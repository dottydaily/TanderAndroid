package th.ku.tander.model

data class RestaurantInfoData(
    val restaurantId: String, val name: String, val categories: List<String>, val startPrice: Double, val lat: Double, val lon: Double) {

    var categoryString = ""
    init {
        categories.map {
            categoryString += if (categoryString.isBlank()) {
                "${it.capitalize()}"
            } else {
                ", ${it.capitalize()}"
            }
        }
    }
}