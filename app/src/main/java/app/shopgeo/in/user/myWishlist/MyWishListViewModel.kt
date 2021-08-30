package app.shopgeo.`in`.user.myWishlist

import android.annotation.SuppressLint
import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.shopgeo.`in`.user.data.LoginDataSource
import app.shopgeo.`in`.user.data.LoginRepository
import app.shopgeo.`in`.databinding.WishItemBinding
import app.shopgeo.`in`.user.network.NetworkApi
import app.shopgeo.`in`.user.network.UpdateCart
import app.shopgeo.`in`.user.network.UpdateWishlist
import app.shopgeo.`in`.user.network.Wishlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import timber.log.Timber

class MyWishListViewModel(application: Application) : AndroidViewModel(application) {

    val loginRepository = LoginRepository(dataSource = LoginDataSource(),application)

    private val _wishList = MutableLiveData<List<Wishlist>?>()
    val wishList: LiveData<List<Wishlist>?>
        get() = _wishList

    val isDoneLoading = MutableLiveData<Boolean>()
    var isNetworkError = MutableLiveData<Boolean>()

    val setWishListUpdate = MutableLiveData<Boolean>()

    val isCartUpdated = MutableLiveData<Boolean?>()
    init{
        if(loginRepository.isLoggedIn){
            isDoneLoading.value = false
            getWishlist()
        }
    }

    fun setWishListUpdated(){
        isDoneLoading.value = false
        isNetworkError.value = false
        setWishListUpdate.value = true
        getWishlist()
    }

    fun updateWishlist(id : Int){
        viewModelScope.launch {
            try {
                isDoneLoading.value = false
                withContext(Dispatchers.IO){
                    val response = loginRepository.get_credentials()?.let {
                        UpdateWishlist(
                            id = id,
                            loginCredential = it,
                        )
                    }?.let { NetworkApi.RETROFIT_SERVICES.updateWishList(it) }
                    withContext(Dispatchers.Main){
                        if(response?.result=="success")
                            setWishListUpdated()
                    }
                }
            } catch (networkError: IOException) {
                Timber.e("Error :$networkError")
                isNetworkError.value = true
            }
        }
    }

    private fun getWishlist(){
        try {
            isDoneLoading.value = true
            viewModelScope.launch {
                val loginCredential = loginRepository.get_credentials()
                if (loginCredential != null) {
                    withContext(Dispatchers.IO) {
                        val items = NetworkApi.RETROFIT_SERVICES.getWishList(loginCredential)
                        withContext(Dispatchers.Main){
                            _wishList.value = items
                            isDoneLoading.value = true
                            isNetworkError.value = false
                        }
                    }
                }
            }
        }catch (e : Throwable){
            Timber.e("Cannot get Cart : $e")
        }
    }
    fun addToCart(id : Int){
        viewModelScope.launch {
            try {
                isDoneLoading.value = false
                withContext(Dispatchers.IO){
                    val response = loginRepository.get_credentials()?.let {
                        UpdateCart(
                            id = id,
                            qty = 1,
                            detail = null,
                            loginCredential = it,
                            type = "add"
                        )
                    }?.let { NetworkApi.RETROFIT_SERVICES.updateCart(it) }
                    withContext(Dispatchers.Main){
                        if(response?.result=="success")
                            isDoneLoading.value = true
                            isNetworkError.value = false
                            isCartUpdated.value = true
                    }
                    throw IOException("Failed")
                }
            } catch (networkError: IOException) {
                Timber.i("Retrying")
                isNetworkError.value = true
                isCartUpdated.value = false
            }
        }
    }
}


class MyWishAdapter (private val clickListener: Listener) : ListAdapter<Wishlist, MyWishAdapter.MyWLHolder>(
    ProductDiffCallback()) {

    override fun onBindViewHolder(holder: MyWLHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, clickListener)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyWLHolder {
        return MyWLHolder.from(parent)
    }

    class MyWLHolder private constructor(private val binding: WishItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Wishlist, clickListener: Listener) {
            binding.wishList = item
            binding.listener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyWLHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = WishItemBinding.inflate(layoutInflater, parent, false)
                return MyWLHolder(binding)
            }
        }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Wishlist>() {

    override fun areItemsTheSame(oldItem: Wishlist, newItem: Wishlist): Boolean {
        return oldItem.product_id == newItem.product_id
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Wishlist, newItem: Wishlist): Boolean {
        return oldItem == newItem
    }
}

class Listener(val clickListener: (item: Wishlist) -> Unit, val removeItem: (item: Wishlist) -> Unit,
               val addToCart: (item: Wishlist) -> Unit) {
    fun onClick(item: Wishlist) = clickListener(item)
    fun remove(item : Wishlist) = removeItem(item)
    fun add(item : Wishlist) = addToCart(item)
}

@Suppress("UNCHECKED_CAST")
class MyWishlistViewModelFactory(private val application: Application
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyWishListViewModel::class.java)) {
            return MyWishListViewModel(application ) as T        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}