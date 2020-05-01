package th.ku.tander.ui.lobby

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_create_lobby.*
import org.json.JSONArray
import org.json.JSONObject
import th.ku.tander.R
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.model.Lobby
import th.ku.tander.model.Restaurant
import java.util.*

class CreateLobbyActivity : AppCompatActivity() {

    private val createLobbyViewModel by viewModels<CreateLobbyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_lobby)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        createLobbyViewModel.isEditMode = intent.getBooleanExtra("isEdit", false)

        if (createLobbyViewModel.isEditMode) {
            supportActionBar?.title = "Edit Lobby"

            val lobbyJson = intent.getStringExtra("lobbyJson")
            lobbyJson.let {
                val json = JSONObject(it)

                createLobbyViewModel.apply {
                    lobby.value = Lobby(json)

                    participantCount.postValue(lobby.value!!.maxParticipant)
                    val participantNumber = lobby.value!!.participant.size
                    minParticipant = if (participantNumber > 2) participantNumber else 2
                }
            }
        } else {
            supportActionBar?.title = "Create Lobby"
        }

        val restaurantJson = intent.getStringExtra("restaurantJson")
        restaurantJson.let {
            val json = JSONObject(it)

            createLobbyViewModel.restaurant.apply { value = Restaurant(json) }
        }
    }

    override fun onStart() {
        super.onStart()

        println("========== START: CREATE LOBBY ==========")

        lobby_create_time_picker.let {
            val text = String.format("Start at: %02d:%02d", it.hour, it.minute)
            lobby_create_start_time_text_view.text = text
        }

        handleTimePickerBehavior()
        handleSubmitButtonBehavior()
        handleDeleteButtonBehavior()
        handleUpButtonBehavior()
        handleDownButtonBehavior()
        handleNameEditTextBehavior()
        handleDescriptionTextBehavior()
    }

    override fun onResume() {
        super.onResume()

        println("========== RESUME: CREATE LOBBY ==========")

        if (createLobbyViewModel.isEditMode) {
            lobby_create_delete_button.isClickable = true
            lobby_create_delete_button.visibility = View.VISIBLE

            fillForm()
        }

        createLobbyViewModel.participantCount.observe(this, Observer {
            lobby_create_participant_number_text_view.text = it.toString()
        })
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE: CREATE LOBBY ==========")
    }

    override fun onStop() {
        super.onStop()

        println("========== STOP: CREATE LOBBY ==========")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            false
        }
        else -> {
            false
        }
    }

    ///////////////////
    // Helper method //
    ///////////////////

    // use it when in edit mode
    private fun fillForm() {
        createLobbyViewModel.lobby.value?.let {
            lobby_create_name_edit_text.setText(it.name)
            lobby_create_description_edit_text.setText(it.description)

            lobby_create_participant_number_text_view.text = it.maxParticipant.toString()

            val currentHour = it.startTime.substring(0, 2)
            val currentMinute = it.startTime.substring(3, 5)
            lobby_create_start_time_text_view.apply {
                text = String.format("Start at: %2s:%2s", currentHour, currentMinute)
            }

            lobby_create_time_picker.apply {
                this.hour = currentHour.toInt()
                this.minute = currentMinute.toInt()
            }
        }
    }

    private fun handleTimePickerBehavior() {
        lobby_create_time_picker.setOnTimeChangedListener { view, hourOfDay, minute ->
            lobby_create_start_time_text_view.apply {
                text = String.format("Start at: %02d:%02d", hourOfDay, minute)
            }
        }
    }

    private fun handleNameEditTextBehavior() {
        val maxLengthName = createLobbyViewModel.maxLengthName
        lobby_create_name_length_text_view.apply {
            text = "${lobby_create_name_edit_text.text.length}/$maxLengthName"
        }

        lobby_create_name_edit_text.addTextChangedListener {
            if (it!!.length > maxLengthName) {
                lobby_create_name_edit_text.apply {
                    setText(it.substring(0, maxLengthName))
                    setSelection(maxLengthName)
                    setTextColor(Color.RED)
                    lobby_create_name_length_text_view.setTextColor(Color.RED)
                }
            } else {
                lobby_create_name_edit_text.setTextColor(Color.BLACK)
                lobby_create_name_length_text_view.setTextColor(Color.GRAY)
            }

            lobby_create_name_length_text_view.apply {
                text = "${lobby_create_name_edit_text.text.length}/$maxLengthName"
            }
        }
    }

    private fun handleDescriptionTextBehavior() {
        lobby_create_description_edit_text.addTextChangedListener {
            if (it!!.length > 100) {
                lobby_create_description_edit_text.apply {
                    setText(it.substring(0, 100))
                    setSelection(100)
                    setTextColor(Color.RED)
                }
            } else {
                lobby_create_description_edit_text.setTextColor(Color.BLACK)
            }
        }
    }

    private fun handleSubmitButtonBehavior() {
        lobby_create_submit_button.setOnClickListener {
            if (validateData()) {
                // show spinner
                lobby_create_loading_spinner.visibility = View.VISIBLE

                createLobbyViewModel.let {
                    convertFormToLobbyAndSend(it.isEditMode)
                }

                if (createLobbyViewModel.isEditMode) {
                    println("========== EDIT: Back to Lobby Page ==========")
                    val intent = Intent()
                    intent.putExtra("editResult",
                        createLobbyViewModel.lobby.value!!.toJson().toString())

                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    println("========== CREATE: Go to Lobby Page ==========")
                    val intent = Intent(this, LobbyActivity::class.java)
                    createLobbyViewModel.restaurant.value.let {
                        intent.putExtra("restaurantJson", it!!.toJson().toString())
                    }

                    createLobbyViewModel.lobby.observe(this, Observer { lobby ->
                        if (lobby != null) {
                            intent.putExtra("lobbyJsonString", lobby.toJson().toString())

                            createLobbyViewModel.restaurant.value.let { restaurant ->
                                intent.putExtra(
                                    "restaurantJsonString",
                                    restaurant!!.toJson().toString()
                                )
                            }

                            intent.putExtra("fromCreate", true)
                            startActivity(intent)
                            finish()
                        }
                    })
                }
            } else {
                Toast.makeText(this, "Please fill all required information.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleDeleteButtonBehavior() {
        lobby_create_delete_button.setOnClickListener {
            // TODO: Implement delete
        }
    }

    private fun handleDownButtonBehavior() {
        lobby_create_down_button.setOnClickListener {
            createLobbyViewModel.decreaseParticipant()
        }
    }

    private fun handleUpButtonBehavior() {
        lobby_create_up_button.setOnClickListener {
            createLobbyViewModel.increaseParticipant()
        }
    }

    private fun validateData(): Boolean {
        val isFilledName = lobby_create_name_edit_text.text.isNotBlank()
        val isFilledDescription = lobby_create_description_edit_text.text.isNotBlank()

        return isFilledName && isFilledDescription
    }

    private fun convertFormToLobbyAndSend(isEdit: Boolean = false) {
        val date = Calendar.getInstance()
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH)
        val day = date.get(Calendar.DAY_OF_MONTH)
        val fullDate = String.format(
            "%04d-%02d-%02dT%02d:%02d:00.013Z",
            year, month, day,
            lobby_create_time_picker.hour, lobby_create_time_picker.minute
        )
        println(fullDate)

        val username = KeyStoreManager.getData("USER")!!

        val participant = if (isEdit) {
            createLobbyViewModel.lobby.value!!.participant
        } else {
            hashSetOf(username)
        }

        val lobby = Lobby(
            lobby_create_name_edit_text.text.toString(),
            createLobbyViewModel.restaurant.value!!.restaurantId,
            fullDate,
            lobby_create_description_edit_text.text.toString(),
            createLobbyViewModel.participantCount.value!!,
            participant, "waiting"
        )

        if (isEdit) {
            Toast.makeText(
                this, "Editing Lobby...", Toast.LENGTH_SHORT).show()
            lobby.lobbyId = createLobbyViewModel.lobby.value!!.lobbyId
            lobby.hostUsername = username
        } else {
            Toast.makeText(
                this, "Creating Lobby...", Toast.LENGTH_SHORT).show()
        }
        createLobbyViewModel.sendLobbyToDatabase(lobby)
    }
}