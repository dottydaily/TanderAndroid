package th.ku.tander.ui.nearby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_nearby.*
import th.ku.tander.R
import th.ku.tander.ui.promotion.NearbyViewModel
import th.ku.tander.ui.view_class.LobbyCardLayout

class NearbyFragment : Fragment() {

    private val nearbyViewModel by viewModels<NearbyViewModel>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nearby, container, false)
    }

    override fun onStart() {
        super.onStart()

        println("========== START: NearbyLobby ==========")
    }

    override fun onResume() {
        super.onResume()

        println("========== RESUME: NearbyLobby ==========")

        nearbyViewModel.fetchLobby()
        nearbyViewModel.getStatus().observe(viewLifecycleOwner, Observer { isDone ->
            if (isDone) {
                createLobbyCardLayout()
            }
        })
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE: NearbyLobby ==========")
    }

    override fun onStop() {
        super.onStop()

        println("========== STOP: NearbyLobby ==========")
    }

    override fun onDestroy() {
        super.onDestroy()

        println("========== DESTROY: NearbyLobby ==========")
    }

    ///////////////////
    // Helper Method //
    ///////////////////

    private fun createLobbyCardLayout() {
        val lobbyJson = nearbyViewModel.getLobbyDetail().value!! // guarantee not null by load status
        println(lobbyJson.toString())

        val contentLayout = requireActivity().findViewById<LinearLayout>(R.id.lobby_linear_layout_view)

        for (i in 0 until lobbyJson.length()) {
            val lobby = lobbyJson.getJSONObject(i)
            val restaurantId = lobby.getString("restaurantId")
            val restaurantJson = nearbyViewModel.getRestaurantJsonById(restaurantId)!!

            val lobbyCardLayout = LobbyCardLayout(this.requireContext()
                , lobbyJson[i].toString(), restaurantJson.toString())

            contentLayout.addView(lobbyCardLayout)
        }

        val spinner = requireActivity().loading_spinner_nearby
        spinner.visibility = View.GONE
    }
}
