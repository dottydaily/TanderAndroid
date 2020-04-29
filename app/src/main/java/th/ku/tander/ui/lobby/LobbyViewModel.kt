package th.ku.tander.ui.lobby

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import org.json.JSONObject
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Lobby
import th.ku.tander.model.Restaurant

class LobbyViewModel: ViewModel() {
    private val restaurantJson = MutableLiveData<Restaurant>()
    private val lobbyJson = MutableLiveData<Lobby>()
    private val quitStatus = MutableLiveData<Boolean>().apply { false }

    fun getRestaurantJson(): LiveData<Restaurant> = restaurantJson
    fun getLobbyJson(): LiveData<Lobby> = lobbyJson
    fun getQuitStatus(): LiveData<Boolean> = quitStatus

    fun setRestaurantJson(jsonString: String) {
        val json = JSONObject(jsonString)
        restaurantJson.value = Restaurant(json)
    }
    fun setLobbyJson(jsonString: String) {
        val json = JSONObject(jsonString)
        lobbyJson.value = Lobby(json)
    }

    fun removeFromLobby(username: String) {
        lobbyJson.value?.participant?.remove(username)

        val restaurantId = restaurantJson.value?.restaurantId
        val body = lobbyJson.value!!.toJson()
        val url = "https://tander-webservice.an.r.appspot.com/lobbies/restaurantId/$restaurantId"

        RequestManager.postRequestWithBody(url, body,
            Response.Listener { response ->
                println(response)
                quitStatus.value = true
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }, 3000, 3, 2f
        )
    }
}