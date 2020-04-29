package th.ku.tander.model

import org.json.JSONArray
import org.json.JSONObject

class Lobby() {
    lateinit var participant: HashSet<String>
    lateinit var lobbyId: String
    lateinit var name: String
    lateinit var restaurantId: String
    lateinit var startFullTime: String
    lateinit var startTime: String
    lateinit var startDate: String
    lateinit var description: String
    var maxParticipant: Int = 0
    lateinit var lobbyStatus: String
    lateinit var hostUsername: String
    private lateinit var lobbyJson: JSONObject

    init {
        println("Instantiating a Lobby object.")
    }

    constructor(json: JSONObject): this() {
        lobbyJson = json
        lobbyId = json.getString("_id")
        name = json.getString("lobbyName")
        restaurantId = json.getString("restaurantId")
        startFullTime = json.getString("startTime")
        startDate = startFullTime.substring(0, 10)
        startTime = startFullTime.substring(11, 16)
        description = json.getString("description")
        maxParticipant = json.getInt("maxParticipant")
        lobbyStatus = json.getString("lobbyStatus")
        hostUsername = json.getString("hostUsername")

        val list = json.getJSONArray("participant")
        participant = hashSetOf()
        for (i in 0 until list.length()) {
            participant.add(list.getString(i))
        }
    }

    fun toJson(): JSONObject {
        updateJson()
        return lobbyJson
    }

    private fun updateJson() {
        val jsonArray = JSONArray(participant)
        lobbyJson.put("participant", jsonArray)
        lobbyJson.put("lobbyName", name)
        lobbyJson.put("startTime", startFullTime)
        lobbyJson.put("description", description)
        lobbyJson.put("maxParticipant", maxParticipant)
        lobbyJson.put("lobbyStatus", lobbyStatus)
    }

    override fun toString(): String {
        return lobbyJson.toString(2)
    }
}