package th.ku.tander.model

import org.json.JSONArray
import org.json.JSONObject

class Lobby() {
    lateinit var participant: HashSet<String>
    var lobbyId: String = ""
    lateinit var name: String
    lateinit var restaurantId: String
    lateinit var startFullTime: String
    lateinit var startTime: String
    lateinit var startDate: String
    lateinit var description: String
    var maxParticipant: Int = 0
    lateinit var lobbyStatus: String
    var hostUsername: String = ""
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

    constructor(name: String, restaurantId: String, startTime: String, description: String,
                maxParticipant: Int, participant: HashSet<String>, lobbyStatus: String): this() {
        this.name = name
        this.restaurantId = restaurantId
        this.startFullTime = startTime
        this.description = description
        this.maxParticipant = maxParticipant
        this.participant = participant
        this.lobbyStatus = lobbyStatus

        lobbyJson = JSONObject()

        lobbyJson.put("restaurantId", restaurantId)
        updateJson()
    }

    fun toJson(): JSONObject {
        updateJson()
        return lobbyJson
    }

    private fun updateJson() {
        val jsonArray = JSONArray(participant)
        lobbyJson.put("startTime", startFullTime)
        lobbyJson.put("participant", jsonArray)
        lobbyJson.put("lobbyName", name)
        lobbyJson.put("description", description)
        lobbyJson.put("maxParticipant", maxParticipant)
        lobbyJson.put("lobbyStatus", lobbyStatus)

        if (!lobbyId.isBlank()) {
            lobbyJson.put("_id", lobbyId)
        }

        if (!hostUsername.isBlank()) {
            lobbyJson.put("hostUsername", hostUsername)
        }
    }

    override fun toString(): String {
        return lobbyJson.toString(2)
    }
}