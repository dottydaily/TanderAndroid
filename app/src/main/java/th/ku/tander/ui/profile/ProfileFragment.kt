package th.ku.tander.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_profile.*
import org.json.JSONObject
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
        profileViewModel.fetchUserData()
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onStart() {
        super.onStart()

        println("========== START: PROFILE ==========")

        handleSignOutButtonBehavior()
    }

    override fun onResume() {
        super.onResume()

        println("========== RESUME: PROFILE ==========")

        profileViewModel.isDoneLoaded.observe(this, Observer { isDone ->
            if (isDone) updateDataUI()
        })
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE: PROFILE ==========")
    }

    override fun onStop() {
        super.onStop()

        println("========== STOP: PROFILE ==========")
    }

    ///////////////////
    // Helper method //
    ///////////////////

    private fun updateDataUI() {
        profileViewModel.userJson.value?.let {
            profile_user_name_text_view.text = it.getString("username")
            profile_first_name_text_view.text = it.getString("firstname")
            profile_last_name_text_view.text = it.getString("lastname")
            profile_birth_date_text_view.text = it.getString("birthdate").substring(0, 10)
            profile_email_text_view.text = it.getString("email")
            profile_telephone_text_view.text = it.getString("telephone")
            profileViewModel.profileImage.value?.let { image ->
                profile_image_view.setImageBitmap(image)
            }

            profile_layout.visibility = View.VISIBLE
            loading_spinner_profile.visibility = View.INVISIBLE
        }
    }

    private fun handleSignOutButtonBehavior() {
        sign_out_button.setOnClickListener {
            KeyStoreManager.clearAll()

            startActivity(Intent(requireContext(), LogInActivity::class.java))
            requireActivity().finish()
        }
    }
}
