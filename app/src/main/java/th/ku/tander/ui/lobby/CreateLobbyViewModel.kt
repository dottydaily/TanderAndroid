package th.ku.tander.ui.lobby

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import org.json.JSONObject
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Lobby
import th.ku.tander.model.Restaurant

class CreateLobbyViewModel: ViewModel() {
    var restaurant = MutableLiveData<Restaurant>().apply { value = null }; private set
    var lobby = MutableLiveData<Lobby>().apply { value = null }; private set
    var participantCount = MutableLiveData<Int>().apply { value = 2 }
    var isEditMode = false
    val maxLengthName = 30
    var minParticipant = 2
    val maxParticipant = 10
    private var username: String = KeyStoreManager.getData("USER")!!

    fun decreaseParticipant() {
        participantCount.apply {
            if (value!! > minParticipant) value = value!! - 1
        }
    }

    fun increaseParticipant() {
        participantCount.apply {
            if (value!! < maxParticipant) value = value!! + 1
        }
    }

    fun sendLobbyToDatabase(lobby: Lobby) {
        val url = "https://tander-webservice.an.r.appspot.com/lobbies/restaurantId/${lobby.restaurantId}"
        val body = lobby.toJson()

        RequestManager.postRequestWithBody(url, body,
            Response.Listener { response ->
                println("\nUpdated: $response\n")

                getLobbyResult()
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }, 3000, 3, 2f
        )
    }

    private fun getLobbyResult() {
        val username = KeyStoreManager.getData("USER")
        val url = "https://tander-webservice.an.r.appspot.com/lobbies/users/$username"

        RequestManager.getJsonArrayRequestWithToken(url,
            Response.Listener {
                if (it.length() != 0) {
                    println(it.getJSONObject(0))
                    lobby.apply { value = Lobby(it.getJSONObject(0)) }
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