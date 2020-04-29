package th.ku.tander.ui.lobby

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_lobby.*
import th.ku.tander.R
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.ui.view_class.ParticipantLayout

class LobbyActivity : AppCompatActivity() {

    private val lobbyViewModel by viewModels<LobbyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Lobby"
    }

    override fun onStart() {
        super.onStart()

        println("========== START: LOBBY ==========")
    }

    override fun onResume() {
        super.onResume()

        println("========== RESUME: LOBBY ==========")

        val lobbyJsonString = intent.getStringExtra("lobbyJsonString")
        val restaurantJsonString = intent.getStringExtra("restaurantJsonString")

        lobbyJsonString.let { lobbyViewModel.setLobbyJson(it) }
        restaurantJsonString.let { lobbyViewModel.setRestaurantJson(it) }

        lobbyViewModel.getDoneStatus().observe(this, Observer { isDone ->
            if (isDone) {
                updateLobbyRoomData()
            }
        })
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE: LOBBY ==========")
    }

    override fun onStop() {
        super.onStop()

        println("========== STOP: LOBBY ==========")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        android.R.id.home -> {
            leaveLobby()
            false
        }
        else -> {
            false
        }
    }

    // get lobby detail from viewModel and update it to UI
    private fun updateLobbyRoomData() {
        val lobby = lobbyViewModel.getLobbyJson().value!!
        val restaurant = lobbyViewModel.getRestaurantJson().value!!

        lobby_room_lobby_title.text = lobby.name
        lobby_room_restaurant_title.text = restaurant.name
        lobby_room_description_text_view.text = lobby.description

        val currentParticipant = lobby.participant.size
        lobby_room_participant_number.text = "$currentParticipant/${lobby.maxParticipant}"
        lobby_room_host_title.text = "Host by " + lobby.hostUsername

        // clear all children view
        lobby_room_participant_layout.removeAllViews()

        // set participant list view
        val participantList = lobby.participant.toList()
        for (i in participantList.indices) {

            // create list view
            val participantListView = ParticipantLayout(this, participantList[i])

            // check owner
            val isParticipantOwner = participantList[i] == lobby.hostUsername
            participantListView.setOwnerImageVisible(isParticipantOwner)

            // add it to layout
            lobby_room_participant_layout.addView(participantListView)

            // if this is not the last member, add divider
            if (i < lobby.participant.size-1) {
                val divider = View(this)
                divider.setBackgroundColor(Color.GRAY)
                val layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1
                )
                layoutParams.setMargins(40, 0, 40, 0)
                divider.layoutParams = layoutParams

                lobby_room_participant_layout.addView(divider)
            }
        }

        lobby_room_start_time_text_view.text = "Start at ${lobby.startTime}"

        if (isOwner(lobby.hostUsername)) {
            lobby_room_button_participant_layout.visibility = View.GONE
        } else {
            lobby_room_button_owner_layout.visibility = View.GONE

            lobby_room_button_participant_leave.setOnClickListener {
                leaveLobby()
            }
        }

        lobby_room_loading_spinner.visibility = View.INVISIBLE
    }

    private fun leaveLobby() {
        lobby_room_loading_spinner.visibility = View.VISIBLE
        val username = KeyStoreManager.getData("USER")
        username.let { lobbyViewModel.removeFromLobby(it!!) }
        lobbyViewModel.getQuitStatus().observe(this, Observer { isQuit ->
            if (isQuit) {
                finish()
            }
        })
    }

    private fun isOwner(ownerName: String): Boolean {
        val username = KeyStoreManager.getData("USER")
        username ?: return false

        println("Check $username == $ownerName, Result is ${username == ownerName}")
        return username == ownerName
    }
}
