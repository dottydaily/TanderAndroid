package th.ku.tander.model

import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

class Restaurant() {
    lateinit var position: LatLng private set
    lateinit var categories: ArrayList<String> private set
    lateinit var promotions: ArrayList<String> private set
    lateinit var restaurantId: String private set
    lateinit var name: String private set
    lateinit var url: String private set
    var startPrice: Double = 0.0; private set
    lateinit var address: String private set
    var isPartner: Boolean = false; private set
    lateinit var restaurantJson: JSONObject private set

    init {
        println("Instantiating a Restaurant object.")
    }

    constructor(json: JSONObject): this() {
        this.restaurantJson = json

        val pos = json.getJSONObject("position")
        position = LatLng(pos.getDouble("lat"), pos.getDouble("lon"))

        val category = json.getJSONArray("categories")
        categories = arrayListOf()
        for (i in 0 until category.length()) {
            categories.add(category.getString(i))
        }

        val promotion = json.getJSONArray("promotions")
        promotions = arrayListOf()
        for (i in 0 until promotion.length()) {
            promotions.add(promotion.getString(i))
        }

        restaurantId = json.getString("_id")
        name = json.getString("name")
        url = json.getString("url")
        startPrice = json.getDouble("startPrice")
        address = json.getString("address")
        isPartner = json.getBoolean("isPartner")
    }

    fun toJson(): JSONObject = restaurantJson

    override fun toString(): String {
        return restaurantJson.toString(2)
    }
}