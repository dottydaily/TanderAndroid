package th.ku.tander.ui.lobby

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import th.ku.tander.model.Restaurant

class CreateLobbyViewModel: ViewModel() {
    var restaurant = MutableLiveData<Restaurant>().apply { value = null }; private set
}