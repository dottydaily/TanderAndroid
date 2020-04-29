package th.ku.tander.ui.promotion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import org.json.JSONArray
import org.json.JSONObject
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Restaurant

class NearbyViewModel : ViewModel() {

    private var status = MutableLiveData<Boolean>().apply { value = false }
    private var lobbyDetailArray = MutableLiveData<JSONArray>().apply { value = JSONArray() }
    private var restaurantIdSet: HashSet<String> = HashSet()
    private var restaurantMap: HashMap<String, Restaurant> = HashMap()
    var joinedLobbyId: String? = null; private set
    private var username: String = KeyStoreManager.getData("USER")!!

    fun getStatus(): LiveData<Boolean> = status
    fun getLobbyDetail(): LiveData<JSONArray> = lobbyDetailArray
    fun getRestaurantJsonById(id: String): JSONObject? = restaurantMap[id]?.restaurantJson

    // fetch lobby info
    fun fetchLobby() {
        clearCurrentData()

        val url = "https://tander-webservice.an.r.appspot.com/lobbies"

        RequestManager.getJsonArrayRequestWithToken(url,
            Response.Listener<JSONArray> { response ->
                lobbyDetailArray.value = response

                // save all restaurantId by using set
                for (i in 0 until response.length()) {
                    val lobby = response.getJSONObject(i)

                    // check if you did join lobby or not
                    if (joinedLobbyId.isNullOrBlank()) {
                        val participants = lobby.getJSONArray("participant")

                        for (j in 0 until participants.length()) {
                            val currentUser = participants.getString(j)

                            if (username == currentUser) {
                                joinedLobbyId = lobby.getString("_id")
                                break
                            }
                        }
                    }

                    val restaurantId = lobby.getString("restaurantId")
                    restaurantIdSet.add(restaurantId)

                }
                println(restaurantIdSet.toString())

                fetchRestaurant()
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }, 3000, 3, 2f)
    }

    // get all restaurants detail
    private fun fetchRestaurant() {
        val url = "https://tander-webservice.an.r.appspot.com/restaurants/id/getByIds"

        val listener = Response.Listener<String> { response ->
            val jsonResponse = JSONArray(response)

            // save all restaurants by using map
            for (i in 0 until jsonResponse.length()) {
                val json = jsonResponse.getJSONObject(i)
                val restaurant = Restaurant(json)

                restaurantMap[restaurant.restaurantId] = restaurant
            }

            status.value = true
            println("DONE FETCHING RESTAURANT : ${jsonResponse.length()} restaurants")
        }

        val errorListener = Response.ErrorListener { error ->
            error.printStackTrace()
        }

        RequestManager.postRequestWithBodyBySet(url, "restaurantIds", restaurantIdSet,
            listener, errorListener, 3000, 3, 2f)
    }

    // reset data before start fetching
    private fun clearCurrentData() {
        status.value = false
        lobbyDetailArray.value = JSONArray()
        restaurantIdSet = HashSet()
        restaurantMap = HashMap()
        joinedLobbyId = null
    }
}