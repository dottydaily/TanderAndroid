package th.ku.tander.model

import com.beust.klaxon.Json
import com.google.android.gms.maps.model.LatLng

class Restaurant{
    @Json(ignored = true) var position: LatLng = LatLng(0.0, 0.0)
    var categories: ArrayList<String>
    @Json(name = "_id") var objectID: String = "DEFAULT_OBJECT_ID"
    var placeID: String = "DEFAULT_PLACE_ID"
    var name: String = "DEFAULT_NAME"
    var url: String = "DEFAULT_URL"
    var startPrice: Double = 0.0
    var address: String = "DEFAULT_ADDRESS"
    var isPartner: Boolean = false

    init {
        println("Instantiating a Restaurant object.")
    }

    constructor(categories: ArrayList<String>,
                objectID: String, placeID: String, name: String, url: String,
                startPrice: Double, address: String, isPartner: Boolean) {
        this.categories = categories
        this.objectID = objectID
        this.placeID = placeID
        this.name = name
        this.url = url
        this.startPrice = startPrice
        this.address = address
        this.isPartner = isPartner
    }

    override fun toString(): String {
        return """
            {
                "position": {"lat": ${position.latitude}, "lon": ${position.longitude}},
                "categories": ${categories},
                "_id": "${objectID}",
                "placeId": "${placeID}",
                "name": "${name}",
                "url": "${if (url == "DEFAULT_URL") "" else url}",
                "startPrice": ${startPrice},
                "address": "${address}",
                "isPartner": ${isPartner}
            }
        """.trimIndent()
    }
}