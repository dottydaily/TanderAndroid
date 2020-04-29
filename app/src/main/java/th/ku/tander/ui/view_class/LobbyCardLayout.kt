package th.ku.tander.ui.view_class

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.android.volley.Response
import kotlinx.android.synthetic.main.lobby_card_view.view.*
import org.json.JSONObject
import th.ku.tander.R
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Lobby
import th.ku.tander.ui.lobby.LobbyActivity

class LobbyCardLayout: FrameLayout {
    private val lobbyTitle: String
    private val restaurantName: String
    private var hostUsername: String
    private var startTime: String
    private val description: String
    private var participants: String
    private var isExpanded: Boolean = false
    private var maxParticipant: Int
    private val restaurantJson: JSONObject
    private var lobbyJson: JSONObject
    private var currentParticipantNumber: Int
    private val primaryColorInt: Int

    // view
    private val lobbyTitleTextView: TextView
    private val restaurantNameTextView: TextView
    private val participantNumberTextView: TextView
    private val startTimeTextView: TextView
    private val descriptionTextView: TextView
    private val hostUsernameTextView: TextView
    private val participantsTextView: TextView
    private val expandButton: Button
    private val joinButton: Button

    // expandable
    private var expandLayout: LinearLayout
    private var expandDivider: View

    constructor(context: Context, lobbyJsonString: String, restaurantJsonString: String)
            : super(context, null, 0) {

        // inflate cardView into this framelayout
        LayoutInflater.from(context).inflate(R.layout.lobby_card_view, this)

        // parse JSONString into JSONObject
        lobbyJson = JSONObject(lobbyJsonString)
        restaurantJson = JSONObject(restaurantJsonString)

        this.lobbyTitle = lobbyJson.getString("lobbyName")
        this.restaurantName = restaurantJson.getString("name")
        this.hostUsername = lobbyJson.getString("hostUsername")
        this.startTime = "Start at : " + lobbyJson.getString("startTime").substring(11, 16)
        this.description = lobbyJson.getString("description")
        this.maxParticipant = lobbyJson.getInt("maxParticipant")
        val userJson = lobbyJson.getJSONArray("participant")

        // custom participants string
        this.participants = ""
        for (i in 0 until userJson.length()) {
            val name = userJson.getString(i)
            if (!name.isNullOrBlank()) {
                participants += "- $name"
            }

            if (i < userJson.length()-1) {
                participants += "\n"
            }
        }

        this.currentParticipantNumber = userJson.length()

        // get primary color id for make it easy to use
        this.primaryColorInt = ContextCompat.getColor(this.context, R.color.colorPrimary)

        // set this card view each element by id
        lobbyTitleTextView = this.lobby_title
        restaurantNameTextView = this.lobby_restaurant_name
        hostUsernameTextView = this.lobby_participant_host_username_title
        participantNumberTextView = this.lobby_participant_number
        startTimeTextView = this.lobby_start_time
        descriptionTextView = this.lobby_description
        participantsTextView = this.lobby_participant_list
        expandButton = this.lobby_expand_button
        joinButton = this.lobby_join_button

        this.expandLayout = this.lobby_layout_expand
        this.expandDivider = this.lobby_card_view_detail_divider

        // hide expand layout as first
        expandLayout.visibility = View.GONE

        // set behavior
        setExpandButtonBehavior()
        setJoinButtonBehavior()
        updateValue()
    }

    private fun updateValue() {
        lobbyTitleTextView.text = lobbyTitle
        restaurantNameTextView.text = restaurantName
        hostUsernameTextView.text = "Host : $hostUsername"
        participantNumberTextView.text = "$currentParticipantNumber/$maxParticipant"
        startTimeTextView.text = startTime
        descriptionTextView.text = description
        participantsTextView.text = participants
    }

    private fun setExpandButtonBehavior() {
        expandButton.setOnClickListener {
            isExpanded = !isExpanded

            if (isExpanded) {
                expandButton.setText(R.string.lobby_expand_button_text_when_show)
                expandLayout.visibility = View.VISIBLE
            } else {
                expandButton.setText(R.string.lobby_expand_button_text)
                expandLayout.visibility = View.GONE
            }
        }
    }

    private fun setJoinButtonBehavior() {
        joinButton.setOnClickListener {
            val username = KeyStoreManager.getData("USER")

            val lobby = Lobby(lobbyJson)
            username.let { lobby.participant.add(it!!) }
            lobbyJson = lobby.toJson()

            println(lobbyJson.toString(4))
            println(restaurantJson.toString(4))

            val restaurantId = restaurantJson.getString("_id")
            val url = "https://tander-webservice.an.r.appspot.com/lobbies/restaurantId/$restaurantId"

            RequestManager.postRequestWithBody(url, lobbyJson,
                Response.Listener { response ->
                    println(response)

                    Toast.makeText(this.context, "Joined!", Toast.LENGTH_SHORT).show()

                    val lobbyPage = Intent(this.context, LobbyActivity::class.java)
                    lobbyPage.putExtra("lobbyJsonString", lobbyJson.toString())
                    lobbyPage.putExtra("restaurantJsonString", restaurantJson.toString())
                    context.startActivity(lobbyPage)
                }, Response.ErrorListener { error ->
                    error.printStackTrace()
                }, 3000, 3, 2f)
        }
    }
}