package th.ku.tander.ui.lobby

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Lobby
import th.ku.tander.model.Restaurant

class LobbyViewModel: ViewModel() {
    private val restaurantJson = MutableLiveData<Restaurant>()
    private val lobbyJson = MutableLiveData<Lobby>()
    private val quitStatus = MutableLiveData<String>().apply { value = null }
    private var hasRestaurant = false
    private var hasLobby = false;
    private val doneStatus = MutableLiveData<Boolean>().apply { value = false }

    fun getRestaurantJson(): LiveData<Restaurant> = restaurantJson
    fun getLobbyJson(): LiveData<Lobby> = lobbyJson
    fun getQuitStatus(): LiveData<String> = quitStatus
    fun getDoneStatus(): LiveData<Boolean> {
        doneStatus.value = hasRestaurant && hasLobby
        return doneStatus
    }

    fun setRestaurantJson(jsonString: String) {
        val json = JSONObject(jsonString)
        restaurantJson.value = Restaurant(json)
        hasRestaurant = true
    }
    fun setLobbyJson(jsonString: String) {
        val json = JSONObject(jsonString)
        lobbyJson.value = Lobby(json)
        hasLobby = true
    }

    fun removeFromLobby(username: String) {
        lobbyJson.value?.participant?.remove(username)

        lobbyJson.value?.participant?.run {
            if (this.size == 0) { // delete lobby instead
                deleteLobby()
            } else {
                val restaurantId = restaurantJson.value?.restaurantId
                val body = lobbyJson.value!!.toJson()
                val url = "https://tander-webservice.an.r.appspot.com/lobbies/restaurantId/$restaurantId"

                RequestManager.postRequestWithBody(url, body,
                    Response.Listener { response ->
                        println(response)
                        quitStatus.value = "QUIT"
                    },
                    Response.ErrorListener { error ->
                        error.printStackTrace()
                    }, 3000, 3, 2f
                )
            }
        }
    }

    fun deleteLobby() {
        var url = "https://tander-webservice.an.r.appspot.com/lobbies/id/"
        lobbyJson.apply {
            url += "${value!!.lobbyId}"
        }

        RequestManager.deleteRequestWithToken(url,
            Response.Listener {
                println(it)
                quitStatus.value = "DELETE"
            }, Response.ErrorListener { error ->
                error.printStackTrace()
            }, 3000, 3, 2f
        )
    }
}