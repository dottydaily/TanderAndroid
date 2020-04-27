package th.ku.tander.ui.view_class

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import th.ku.tander.R

class PromotionCardView : CardView {
    var isExpanded = false
    var id: String
    var title: String
    var description: String
    var validTime: String
    var endTime: String
    var image: Bitmap? = null
    var time: String
    var restaurantListText: String

    val primaryColorInt: Int

    // view
    var contentLayout: LinearLayout
    var imageView: ImageView
    var titleTextView: TextView
    var descriptionTextView: TextView
    var expandButton: Button

    // expandable view
    var timeTextView: TextView
    var expandDivider: View
    var restaurantListTextView: TextView

    constructor(context: Context, id: String, title: String,
                description: String, validTime: String, endTime: String,
                image: Bitmap?, restaurantName: ArrayList<String>)
            : super(context, null, 0) {
        this.id = id
        this.title = title
        this.description = description
        this.validTime = validTime.substring(0, 10)
        this.endTime = endTime.substring(0, 10)
        this.image = image
        this.time = "Valid: ${this.validTime} Until ${this.endTime}"

        // convert restaurant name from array to list text
        var listText = ""
        for (i in 0 until restaurantName.size) {
            listText = "- ${restaurantName[i]}"

            if (i < restaurantName.size-1) {
                listText += "\n"
            }
        }
        restaurantListText = listText

        this.primaryColorInt = ContextCompat.getColor(this.context, R.color.colorPrimary)

        imageView = createImageView()
        titleTextView = createTitle()
        descriptionTextView = createDescription(description, View.VISIBLE)
        expandButton = createExpandButton()
        timeTextView = createTimeTextView()
        expandDivider = createDivider(50)
        restaurantListTextView = createDescription(restaurantListText, View.GONE)

        contentLayout = createLinearLayout()
        setCardView()
    }

    private fun setCardView() {
        this.radius = 20F
        this.foregroundGravity = Gravity.CENTER_HORIZONTAL
        this.setBackgroundResource(R.drawable.promotion_card_view_background)

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(50, 30, 50, 30)
        this.layoutParams = layoutParams

        this.addView(contentLayout)
    }

    private fun createLinearLayout(): LinearLayout {
        val linearLayout = LinearLayout(this.context)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.gravity = Gravity.CENTER_HORIZONTAL
        linearLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        linearLayout.addView(imageView)
        linearLayout.addView(createDivider(0))
        linearLayout.addView(titleTextView)
        linearLayout.addView(descriptionTextView)
        linearLayout.addView(createDivider(50))
        linearLayout.addView(timeTextView)
        linearLayout.addView(restaurantListTextView)
        linearLayout.addView(expandDivider)
        linearLayout.addView(expandButton)


        return linearLayout
    }

    private fun createImageView(): ImageView {
        val imageView = ImageView(this.context)

        if (image != null) {
            imageView.setImageBitmap(image)
            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 800
            )
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            imageView.setImageResource(R.drawable.ic_splash_logo)
            imageView.imageAlpha = 200
            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        return imageView
    }

    private fun createTitle(): TextView {
        val titleTextView = TextView(this.context)
        titleTextView.text = title.capitalize()
        titleTextView.setTextColor(Color.BLACK)
        titleTextView.textSize = 18f
        titleTextView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        titleTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        titleTextView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        titleTextView.setPadding(50, 50, 50, 5)

        return titleTextView
    }

    private fun createDescription(text: String, visibleMode: Int): TextView {
        val descriptionTextView = TextView(this.context)
        descriptionTextView.text = text
        descriptionTextView.textSize = 16f
        descriptionTextView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        descriptionTextView.setTextColor(Color.GRAY)
        descriptionTextView.maxLines = 10
        descriptionTextView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        descriptionTextView.setPadding(50, 5, 50, 50)
        descriptionTextView.visibility = visibleMode

        return descriptionTextView
    }

    private fun createDivider(margin: Int): View {
        val divider = View(this.context)
        divider.setBackgroundColor(Color.GRAY)

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 1
        )
        layoutParams.setMargins(margin, 0, margin, 0)
        divider.layoutParams = layoutParams

        return divider
    }

    private fun createExpandButton(): Button {
        val expandButton = Button(this.context)
        expandButton.text = "Tap for more detail"
        expandButton.textSize = 18f
        expandButton.setTextColor(Color.WHITE)
        expandButton.setBackgroundColor(primaryColorInt)
        expandButton.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        expandButton.isAllCaps = false
        expandButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        expandButton.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        expandButton.setPadding(50, 50, 50, 50)

        expandButton.setOnClickListener {
            isExpanded = !isExpanded

            println("$id expand = $isExpanded")
            updateCardViewAppearance()
        }

        return expandButton
    }

    private fun createTimeTextView(): TextView {
        val timeTextView = TextView(this.context)
        timeTextView.text = time
        timeTextView.textSize = 18f
        timeTextView.setTextColor(Color.BLACK)
        timeTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        timeTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        timeTextView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        timeTextView.setPadding(50, 50, 50, 50)
        timeTextView.visibility = View.GONE

        return timeTextView
    }

    private fun updateCardViewAppearance() {
        if (isExpanded) {
            expandButton.text = "Tap to hide detail"
            expandButton.setBackgroundColor(Color.WHITE)
            expandButton.setTextColor(primaryColorInt)
            timeTextView.visibility = View.VISIBLE
            if (!restaurantListText.isBlank()) {
                restaurantListTextView.visibility = View.VISIBLE
            }
            expandDivider.visibility = View.VISIBLE
        } else {
            expandButton.text = "Tap for more detail"
            expandButton.setBackgroundColor(primaryColorInt)
            expandButton.setTextColor(Color.WHITE)
            timeTextView.visibility = View.GONE
            restaurantListTextView.visibility = View.GONE
            if (!restaurantListText.isBlank()) {
                restaurantListTextView.visibility = View.GONE
            }
            expandDivider.visibility = View.GONE
        }
    }
}