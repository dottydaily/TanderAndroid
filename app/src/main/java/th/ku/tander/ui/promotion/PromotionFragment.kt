package th.ku.tander.ui.promotion

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_promotion.*
import org.json.JSONArray
import th.ku.tander.R
import th.ku.tander.ui.view_class.PromotionCardView

class PromotionFragment : Fragment() {

    private val promotionViewModel by viewModels<PromotionViewModel> {
        PromotionViewModelFactory(this.requireContext())
    }

    private var cardViewsMap: HashMap<String, CardView> = HashMap()
    private var imageMap: HashMap<String, Bitmap>? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_promotion, container, false)
    }

    override fun onStart() {
        super.onStart()

        println("========== START: Promotion ==========")
    }

    override fun onResume() {
        super.onResume()

        println("========== RESUME: Promotion ==========")

        // observer for image
        val statusObserver = Observer<Boolean> { isDone ->
            if (isDone) {
                println("\nCOMPLETE -------------------------------------------\n")
                val promotionDetails = promotionViewModel.getPromotionDetail().value!!
                this.imageMap = promotionViewModel.getImageMap()
                println("Done getting image : ${imageMap?.size}")
                createPromotionCardViewList(promotionDetails)
            }
        }

        promotionViewModel.fetchPromotion()
        promotionViewModel.getStatus().observe(viewLifecycleOwner, statusObserver)
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE: Promotion ==========")
    }

    override fun onStop() {
        super.onStop()

        println("========== STOP: Promotion ==========")
    }

    override fun onDestroy() {
        super.onDestroy()

        println("========== DESTROY: Promotion ==========")
    }

    ///////////////////
    // Helper Method //
    ///////////////////

    // create each promotion cardview
    private fun createPromotionCardViewList(promotionDetails: JSONArray) {
        for (i in 0 until promotionDetails.length()) {
            val promotion = promotionDetails.getJSONObject(i)
            val id = promotion.getString("_id")
            val title = promotion.getString("promotionName")
            val description = promotion.getString("description")
            val validTime = promotion.getString("validTime")
            val endTime = promotion.getString("endTime")
            val image = imageMap?.get(id)
            val restaurants = promotion.getJSONArray("restaurantApply")

            val restaurantName = ArrayList<String>()
            for (i in 0 until restaurants.length()) {
                val id = restaurants.getString(i)
                val name = promotionViewModel.getRestaurantNameById(id)

                if (!name.isNullOrBlank()) {
                    restaurantName.add(name)
                }
            }

            val promotionCardView = PromotionCardView(requireContext(), id, title
                , description, validTime, endTime, image, restaurantName)

            cardViewsMap[id] = promotionCardView // same as cardViewMap.put(id, promotionCardView)

            promotion_linear_layout_view.addView(promotionCardView)

            if (i == promotionDetails.length()-1) {
                val spinner = activity?.findViewById<ProgressBar>(R.id.loading_spinner_promotion)
                spinner?.visibility = View.INVISIBLE
            }
        }
    }
}