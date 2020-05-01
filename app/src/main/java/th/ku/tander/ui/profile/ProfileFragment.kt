package th.ku.tander.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.fragment_profile.*
import th.ku.tander.R
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.ui.login.LogInActivity

class ProfileFragment : Fragment() {

    private val profileViewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onStart() {
        super.onStart()

        handleSignOutButtonBehavior()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    ///////////////////
    // Helper method //
    ///////////////////

    private fun handleSignOutButtonBehavior() {
        sign_out_button.setOnClickListener {
            KeyStoreManager.clearAll()

            startActivity(Intent(requireContext(), LogInActivity::class.java))
            requireActivity().finish()
        }
    }
}
