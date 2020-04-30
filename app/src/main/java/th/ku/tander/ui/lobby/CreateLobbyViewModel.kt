package th.ku.tander.ui.lobby

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Lobby
import th.ku.tander.model.Restaurant

class CreateLobbyViewModel: ViewModel() {
    var restaurant = MutableLiveData<Restaurant>().apply { value = null }; private set
    var lobby = MutableLiveData<Lobby>().apply { value = null }; private set
    var participantCount = MutableLiveData<Int>().apply { value = 2 }
    val maxLengthName = 30
    val minParticipant = 2
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

    fun createLobbyToDatabase(name: String, restaurantId: String, startTime: String,
                              description: String, maxParticipant: Int, participant: HashSet<String>) {

        val tempLobby = Lobby(name, restaurantId, startTime, description, maxParticipant,
            participant, "waiting" )
        tempLobby.participant.add(username)

        val url = "https://tander-webservice.an.r.appspot.com/lobbies/restaurantId/$restaurantId"
        val body = tempLobby.toJson()

        RequestManager.postRequestWithBody(url, body,
            Response.Listener { response ->
                println(response)

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
                println(it.getJSONObject(0))
                lobby.apply { value = Lobby(it.getJSONObject(0)) }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }, 3000, 3, 2f
        )
    }
}