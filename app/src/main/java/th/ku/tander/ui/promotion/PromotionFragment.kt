package th.ku.tander.ui.promotion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import th.ku.tander.R

class PromotionFragment : Fragment() {

    private lateinit var promotionViewModel: PromotionViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        promotionViewModel =
                ViewModelProviders.of(this).get(PromotionViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_promotion, container, false)
        val textView: TextView = root.findViewById(R.id.text_promotion)
        promotionViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}
