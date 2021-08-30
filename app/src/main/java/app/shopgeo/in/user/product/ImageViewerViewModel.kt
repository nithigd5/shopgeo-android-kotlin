package app.shopgeo.`in`.user.product

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ImageViewerViewModel : ViewModel() {

    val isOnZoom = MutableLiveData<Boolean>()

    init{
        isOnZoom.value = true
    }
}
@Suppress("UNCHECKED_CAST")
class ImageViewerViewModelFactory(
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            return ImageViewerViewModelFactory() as T        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}