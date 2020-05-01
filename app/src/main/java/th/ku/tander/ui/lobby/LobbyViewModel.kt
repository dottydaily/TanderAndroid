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
    private val lobby = MutableLiveData<Lobby>()
    private val quitStatus = MutableLiveData<String>().apply { value = null }
    private var hasRestaurant = false
    private var hasLobby = false;
    private val doneStatus = MutableLiveData<Boolean>().apply { value = false }

    fun getRestaurantJson(): LiveData<Restaurant> = restaurantJson
    fun getLobbyJson(): LiveData<Lobby> = lobby
    fun getQuitStatus(): LiveData<String> = quitStatus
    fun getDoneStatus(): LiveData<Boolean> {
        doneStatus.value = hasRestaurant && hasLobby
        return doneStatus
    }
    fun clearQuitStatus() {
        quitStatus.value = null
    }
    fun getLobbyEatingStatus(): String = lobby.value!!.lobbyStatus
    fun isLobbyExist(): Boolean = quitStatus.value != "DELETE"

    fun setRestaurantJson(jsonString: String) {
        val json = JSONObject(jsonString)
        restaurantJson.value = Restaurant(json)
        hasRestaurant = true
    }
    fun setLobbyJson(jsonString: String) {
        val json = JSONObject(jsonString)
        lobby.value = Lobby(json)
        hasLobby = true
    }

    fun updateLobby() {
        val hostUsername = lobby.value!!.hostUsername
        doneStatus.postValue(false)

        val url = "https://tander-webservice.an.r.appspot.com/lobbies/users/$hostUsername"
        RequestManager.getJsonArrayRequestWithToken(url,
            Response.Listener {
                if (it.length() == 0) quitStatus.postValue("DELETE")
                else {
                    val newLobbyDetail = Lobby(it.getJSONObject(0))
                    lobby.postValue(newLobbyDetail)
                    doneStatus.postValue(true)
                }
            },
            Response.ErrorListener {

            }, 3000, 3, 2f
        )
    }

    fun startEating() {
        lobby.apply {
            value?.lobbyStatus = "eating"
        }.also {
            val url = "https://tander-webservice.an.r.appspot.com/lobbies/restaurantId/${it.value!!.restaurantId}"
            val body = it.value!!.toJson()

            RequestManager.postRequestWithBody(url, body,
                Response.Listener { response ->
                    println(response)

                    fetchLobbyResult()
                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                }, 3000, 3, 2f
            )
        }
    }

    fun removeFromLobby(username: String) {
        lobby.value?.participant?.remove(username)

        lobby.value?.participant?.run {
            if (this.size == 0) { // delete lobby instead
                deleteLobby()
            } else {
                val restaurantId = restaurantJson.value?.restaurantId
                val body = lobby.value!!.toJson()
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
        lobby.apply {
            url += "${value!!.lobbyId}"
        }

        RequestManager.deleteRequestWithToken(url,
            Response.Listener {
                println("Deleted this lobby. ${lobby.value?.name}")
                quitStatus.value = "DELETE"
            }, Response.ErrorListener { error ->
                error.printStackTrace()
            }, 3000, 3, 2f
        )
    }

    fun fetchLobbyResult(isForEdit: Boolean = false) {
        val url = "https://tander-webservice.an.r.appspot.com/lobbies/users/${lobby.value!!.hostUsername}"

        RequestManager.getJsonArrayRequestWithToken(url,
            Response.Listener {
                if (it.length() != 0) {
                    println(it.getJSONObject(0))
                    lobby.apply { value = Lobby(it.getJSONObject(0)) }

                    if(isForEdit) {
                        quitStatus.postValue("EDIT")
                    }
                } else {
                    println("Lobby not found. host = ${lobby.value?.hostUsername}")
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }, 3000, 3, 2f
        )
    }
}