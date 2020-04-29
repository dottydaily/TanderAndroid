package th.ku.tander.ui.view_class

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.lobby_room_participant_layout.view.*
import th.ku.tander.R

class ParticipantLayout: FrameLayout {
    private var username: String

    // view
    private val participantTextView: TextView
    private val ownerImageView: ImageView

    constructor(context: Context, username: String) : super(context, null, 0) {
        // inflate cardView into this framelayout
        LayoutInflater.from(context).inflate(R.layout.lobby_room_participant_layout, this)
        this.username = username

        participantTextView = this.participant_list_text_view
        ownerImageView = this.participant_list_owner_image_view

        this.setPadding(40, 20, 40, 20)

        updateData()
    }

    // update data into view
    private fun updateData() {
        participantTextView.text = username
    }

    fun setOwnerImageVisible(isOnwer: Boolean) {
        if (isOnwer) {
            ownerImageView.visibility = View.VISIBLE
        } else {
            ownerImageView.visibility = View.INVISIBLE
        }
    }
}