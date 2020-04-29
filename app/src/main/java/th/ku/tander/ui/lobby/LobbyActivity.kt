package th.ku.tander.ui.lobby

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import org.json.JSONObject
import th.ku.tander.R
import th.ku.tander.helper.KeyStoreManager

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
        val lobbyJsonString = intent.getStringExtra("lobbyJsonString")
        val restaurantJsonString = intent.getStringExtra("restaurantJsonString")

        lobbyJsonString.let { lobbyViewModel.setLobbyJson(it) }
        restaurantJsonString.let { lobbyViewModel.setRestaurantJson(it) }

        lobbyViewModel.getLobbyJson().observe(this, Observer { lobbyJson ->
            if (lobbyJson != null) {
                println(lobbyJson)
                Toast.makeText(this, "Got Lobby", Toast.LENGTH_SHORT).show()
            }
        })

        lobbyViewModel.getRestaurantJson().observe(this, Observer { restaurantJson ->
            if (restaurantJson != null) {
                println(restaurantJson)
                Toast.makeText(this, "Got Restaurant", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        println("========== RESUME: LOBBY ==========")
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
            val username = KeyStoreManager.getData("USER")
            username.let { lobbyViewModel.removeFromLobby(it!!) }
            lobbyViewModel.getQuitStatus().observe(this, Observer { isQuit ->
                if (isQuit) {
                    finish()
                }
            })
            false
        }
        else -> {
            false
        }
    }
}
