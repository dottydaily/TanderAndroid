package th.ku.tander.ui.promotion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PromotionViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is promotion Fragment"
    }
    val text: LiveData<String> = _text
}