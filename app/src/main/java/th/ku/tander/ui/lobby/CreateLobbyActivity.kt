package th.ku.tander.ui.lobby

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import kotlinx.android.synthetic.main.activity_create_lobby.*
import org.json.JSONObject
import th.ku.tander.R
import th.ku.tander.model.Restaurant

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

        setTimePickerBehavior()
    }

    override fun onResume() {
        super.onResume()

        println("========== RESUME: CREATE LOBBY ==========")

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

    private fun setTimePickerBehavior() {
        lobby_create_time_picker.setOnTimeChangedListener { view, hourOfDay, minute ->
            lobby_create_start_time_text_view.apply {
                text = String.format("Start at: %02d:%02d", hourOfDay, minute)
            }
        }
    }
}
