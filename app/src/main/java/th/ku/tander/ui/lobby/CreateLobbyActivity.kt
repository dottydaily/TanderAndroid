package th.ku.tander.ui.lobby

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
import org.json.JSONObject
import th.ku.tander.R
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.model.Restaurant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class CreateLobbyActivity : AppCompatActivity() {

    private val createLobbyViewModel by viewModels<CreateLobbyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_lobby)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Lobby"

        val restaurantJson = intent.getStringExtra("restaurantJson")

        restaurantJson.let {
            val json = JSONObject(it)

            createLobbyViewModel.restaurant.apply { value = Restaurant(json) }
        }
    }

    override fun onStart() {
        super.onStart()

        println("========== START: CREATE LOBBY ==========")

        lobby_create_time_picker.apply {
            val text = String.format("Start at: %02d:%02d", hour, minute)

            lobby_create_start_time_text_view.text = text
        }

        handleTimePickerBehavior()
        handleSubmitButtonBehavior()
        handleUpButtonBehavior()
        handleDownButtonBehavior()
        handleNameEditTextBehavior()
        handleDescriptionTextBehavior()
    }

    override fun onResume() {
        super.onResume()

        println("========== RESUME: CREATE LOBBY ==========")

        createLobbyViewModel.participantCount.observe(this, Observer {
            lobby_create_participant_number_text_view.text = it.toString()
        })

        createLobbyViewModel.restaurant.run {
            println(value?.toJson().toString())
        }
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE: CREATE LOBBY ==========")
    }

    override fun onStop() {
        super.onStop()

        println("========== STOP: CREATE LOBBY ==========")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
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

                createLobby()

                val intent = Intent(this, LobbyActivity::class.java)
                createLobbyViewModel.restaurant.value.let {
                    intent.putExtra("restaurantJson", it!!.toJson().toString())
                }

                createLobbyViewModel.lobby.observe(this, Observer { lobby ->
                    if (lobby != null) {
                        intent.putExtra("lobbyJsonString", lobby.toJson().toString())

                        createLobbyViewModel.restaurant.value.let { restaurant ->
                            intent.putExtra("restaurantJsonString",
                                restaurant!!.toJson().toString())
                        }

                        intent.putExtra("fromCreate", true)
                        startActivity(intent)
                        finish()
                    }
                })
            } else {
                Toast.makeText(this,
                    "Please fill all required information.", Toast.LENGTH_SHORT).show()
            }
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

    private fun createLobby() {
        Toast.makeText(this, "Creating Lobby...",
            Toast.LENGTH_SHORT).show()

        val date = Calendar.getInstance()
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH)
        val day = date.get(Calendar.DAY_OF_MONTH)
        val fullDate = String.format("%04d-%02d-%02dT%02d:%02d:00.013Z",
            year, month, day,
            lobby_create_time_picker.hour, lobby_create_time_picker.minute )
        println(fullDate)

        val username = KeyStoreManager.getData("USER")!!
        val participant = hashSetOf(username)
        createLobbyViewModel.createLobbyToDatabase(
            name = lobby_create_name_edit_text.text.toString(),
            restaurantId = createLobbyViewModel.restaurant.value!!.restaurantId,
            startTime = fullDate,
            description = lobby_create_description_edit_text.text.toString(),
            maxParticipant = createLobbyViewModel.participantCount.value!!,
            participant = participant
        )
    }
}
