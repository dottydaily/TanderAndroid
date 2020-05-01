package th.ku.tander.ui.lobby

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_lobby.*
import org.json.JSONObject
import th.ku.tander.R
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.helper.SocketManager
import th.ku.tander.model.Lobby
import th.ku.tander.ui.view_class.ParticipantLayout

class LobbyActivity : AppCompatActivity() {

    private val EDIT_LOBBY_REQUEST_CODE = 0
    private val lobbyViewModel by viewModels<LobbyViewModel>()
    private var isFromCreatePage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        isFromCreatePage = intent.getBooleanExtra("fromCreate", false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Lobby"

        val lobbyJsonString = intent.getStringExtra("lobbyJsonString")
        val restaurantJsonString = intent.getStringExtra("restaurantJsonString")

        lobbyJsonString.let { lobbyViewModel.setLobbyJson(it) }
        restaurantJsonString.let { lobbyViewModel.setRestaurantJson(it) }

        startSocketUpdateListener()
    }

    override fun onStart() {
        super.onStart()

        println("========== START: LOBBY ==========")
    }

    override fun onResume() {
        super.onResume()

        println("========== RESUME: LOBBY ==========")

        lobby_room_content_layout.visibility = View.GONE

        lobbyViewModel.getDoneStatus().observe(this, Observer { isDone ->
            if (isDone) {
                updateLobbyRoomData()
            }
        })

        lobbyViewModel.getQuitStatus().observe(this, Observer { status ->
            if (status != null) { // quit this lobby
                println("QUIT LOBBY : $status")

                when (status) {
                    "DELETE" -> { // delete lobby, prompt user
                        Toast.makeText(this, "Lobby Deleted.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    "QUIT" -> { // quit lobby, prompt user
                        Toast.makeText(this, "Quited Lobby.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    "EDIT" -> {  // edit lobby, only get this status when press edit
                        lobbyViewModel.clearQuitStatus()

                        val intent = Intent(this, CreateLobbyActivity::class.java)
                        lobbyViewModel.getLobbyJson().run {
                            intent.putExtra("lobbyJson", value!!.toJson().toString())
                            intent.putExtra("isEdit", true)
                        }
                        lobbyViewModel.getRestaurantJson().run {
                            intent.putExtra("restaurantJson", value!!.toJson().toString())
                        }

                        lobby_room_loading_spinner.visibility = View.GONE

                        println("\nQUIT: ${lobbyViewModel.getQuitStatus().value}\n")
                        startActivityForResult(intent, EDIT_LOBBY_REQUEST_CODE)
                    }
                }
            }
        })

        handleMainButtonBehavior()
        handleEditButtonBehavior()
        handleLeaveButtonBehavior()
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE: LOBBY ==========")
    }

    override fun onStop() {
        super.onStop()

        println("========== STOP: LOBBY ==========")
        removeSocketUpdateListener()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        android.R.id.home -> {
            if (lobbyViewModel.getLobbyEatingStatus() == "waiting") {
                leaveLobby()
            } else {
                finish()
            }

            false
        }
        else -> {
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // do fetch new lobby if come from edit page
        if (requestCode == EDIT_LOBBY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Edit successful", Toast.LENGTH_SHORT).show()
                lobbyViewModel.updateLobby()
            }
        }
    }

    ///////////////////
    // helper method //
    ///////////////////

    // get lobby detail from viewModel and update it to UI
    private fun updateLobbyRoomData() {
        val lobby = lobbyViewModel.getLobbyJson().value!!
        val restaurant = lobbyViewModel.getRestaurantJson().value!!

        lobby_room_lobby_title.text = lobby.name
        lobby_room_lobby_title.isSelected = true
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

        // show/hide button
        if (isOwner(lobby.hostUsername)) {
            lobby_room_button_participant_leave.visibility = View.GONE

            if (lobbyViewModel.getLobbyEatingStatus() == "eating") {
                lobby_room_button_owner_main.text = "Finish eating"
                lobby_room_button_owner_edit.visibility = View.GONE
            }
        } else {
            lobby_room_button_owner_main.visibility = View.GONE
            lobby_room_button_owner_edit.visibility = View.GONE

            if (lobbyViewModel.getLobbyEatingStatus() == "eating") {
                lobby_room_button_participant_leave.setTextColor(Color.GRAY)
                lobby_room_button_participant_leave.isClickable = false
            }
        }

        lobby_room_loading_spinner.visibility = View.INVISIBLE
        lobby_room_content_layout.visibility = View.VISIBLE
    }

    private fun handleMainButtonBehavior() {
        lobby_room_button_owner_main.setOnClickListener {
            if (lobbyViewModel.getLobbyEatingStatus() == "waiting") {
                lobbyViewModel.startEating()
            } else if (lobbyViewModel.getLobbyEatingStatus() == "eating") {
                lobbyViewModel.deleteLobby()
                Toast.makeText(this,
                    "Finish Eating.\nThanks for joining me!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun handleEditButtonBehavior() {
        lobby_room_button_owner_edit.setOnClickListener {
            lobbyViewModel.fetchLobbyResult(isForEdit = true)
            lobby_room_loading_spinner.visibility = View.VISIBLE
        }
    }

    private fun handleLeaveButtonBehavior() {
        lobby_room_button_participant_leave.setOnClickListener {
            leaveLobby()
        }
    }

    private fun leaveLobby() {
        lobby_room_loading_spinner.visibility = View.VISIBLE

        val username = KeyStoreManager.getData("USER")
        username.let { lobbyViewModel.removeFromLobby(it!!) }
    }

    private fun isOwner(ownerName: String): Boolean {
        val username = KeyStoreManager.getData("USER")
        username ?: return false

        println("Check $username == $ownerName, Result is ${username == ownerName}")
        return username == ownerName
    }

    private fun startSocketUpdateListener() {
        SocketManager.hasUpdate.observe(this, Observer {
            if (lobbyViewModel.isLobbyExist()) {
                lobbyViewModel.updateLobby()
            } else {
                Toast.makeText(this,
                    "Finish Eating.\nThanks for joining me!", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun removeSocketUpdateListener() {
        SocketManager.hasUpdate.removeObservers(this)
    }
}
