package app.shopgeo.`in`.user.product

import android.annotation.SuppressLint
import android.app.Application
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.shopgeo.`in`.user.data.LoginDataSource
import app.shopgeo.`in`.user.data.LoginRepository
import app.shopgeo.`in`.user.database.DatabaseProduct
import app.shopgeo.`in`.user.database.ProductDatabase
import app.shopgeo.`in`.databinding.ImageItemBinding
import app.shopgeo.`in`.user.network.*
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import timber.log.Timber

class ProductViewModel(private val productId : Int, val database: ProductDatabase,
                       application: Application) : AndroidViewModel(application) {

    private val loginRepository = LoginRepository(dataSource = LoginDataSource(),application)

    var product : LiveData<DatabaseProduct> = database.productsDao.get(productId)

    val isDoneLoading = MutableLiveData<Boolean>()
    var isNetworkError = MutableLiveData<Boolean>()

    var fetchAttempt = 0
    var isloggedIn : Boolean = false
    val images = Transformations.map(product){ product ->
        product?.Images?.map{
            SlideModel(
                imageUrl = it,
                scaleType = ScaleTypes.FIT
            )
        }
    }
    private val _wishList = MutableLiveData<List<Wishlist>?>()
    val wishList : LiveData<Boolean> = Transformations.map(_wishList){ list ->
        var res: Boolean = false
        list?.forEach {
            if(it.product_id==productId){
                res = true
            }
        }
        res
    }

    val isProductAdded = MutableLiveData<Boolean?>()
    val setWishListUpdate = MutableLiveData<Boolean>()

    init {
        refreshProduct()
        isloggedIn = loginRepository.isLoggedIn
        if(isloggedIn) getWishlist()
    }
    private suspend fun setProductRefreshed(){
        isDoneLoading.value = true
        isNetworkError.value = false
        fetchAttempt = 0
    }
    private suspend fun setCartUpdated(){
        isProductAdded.value = true
        isNetworkError.value = false
        fetchAttempt = 0
        isDoneLoading.value = true
    }
    fun setWishListUpdated(){
        isDoneLoading.value = true
        isNetworkError.value = false
        setWishListUpdate.value = true
    }
    private fun getWishlist(){
        try {
            viewModelScope.launch {
                val loginCredential = loginRepository.get_credentials()
                if (loginCredential != null) {
                    withContext(Dispatchers.IO) {
                        val items = NetworkApi.RETROFIT_SERVICES.getWishList(loginCredential)
                        withContext(Dispatchers.Main){
                            _wishList.value = items
                            isDoneLoading.value = true
                            isNetworkError.value = false
                            Timber.i("Orders :${wishList.value}")
                        }
                    }
                }
            }
        }catch (e : Throwable){
            Timber.e("Cannot get Cart : $e")
        }
    }
    fun updateWishlist(id : Int){
        if(!isloggedIn)
            return
        viewModelScope.launch {
            try {
                Timber.i("Product $product")
                isDoneLoading.value = false
                withContext(Dispatchers.IO){
                    val response = loginRepository.get_credentials()?.let {
                        UpdateWishlist(
                            id = id,
                            loginCredential = it,
                        )
                    }?.let { NetworkApi.RETROFIT_SERVICES.updateWishList(it) }
                    withContext(Dispatchers.Main){
                       Timber.i("Response : $response")
                        if(response?.result=="success")
                            setWishListUpdated()
                        else{
                            throw IOException("Failed")
                        }
                    }
                }

            } catch (networkError: IOException) {
                Timber.e("Error :$networkError")
                isNetworkError.value = true
                isDoneLoading.value = true
            }
        }
    }

    fun addToCart(){
        viewModelScope.launch {
            try {
                Timber.i("Product $product")
                isDoneLoading.value = false
                withContext(Dispatchers.IO){
                    val response = loginRepository.get_credentials()?.let {
                        UpdateCart(
                            id = productId,
                            qty = 1,
                            detail = null,
                            loginCredential = it,
                            type = "add"
                        )
                    }?.let { NetworkApi.RETROFIT_SERVICES.updateCart(it) }
                    withContext(Dispatchers.Main){
                        if(response?.result=="success")
                        setCartUpdated()
                    }
                }

            } catch (networkError: IOException) {
                Timber.i("Retrying")
                isNetworkError.value = true
                fetchAttempt += 1
                isDoneLoading.value = true
                if(fetchAttempt<3){
                    isDoneLoading.value = false
                    addToCart()
                }
                else{
                    val timer = object : CountDownTimer(100000,1000){
                        override fun onTick(millisUntilFinished: Long) {
                            Timber.i("Retrying in $millisUntilFinished")
                        }
                        override fun onFinish() {
                            isDoneLoading.value = false
                            addToCart()
                        }
                    }
                    timer.start()
                }
            }
        }
    }
    fun refreshProduct(){
        viewModelScope.launch {
            try {
                isDoneLoading.value = false
                Timber.i("Product $product")
                withContext(Dispatchers.IO){
                    val product = NetworkApi.RETROFIT_SERVICES.getProduct(productId)
                    database.productsDao.insertAll(product.asDatabaseModel())
                }
                setProductRefreshed()
            } catch (networkError: Throwable) {
                Timber.i("Retrying")
                isNetworkError.value = true
                fetchAttempt += 1
                isDoneLoading.value = true
                if(fetchAttempt<3){
                    isDoneLoading.value = false
                    refreshProduct()
                }
                else{
                    val timer = object : CountDownTimer(100000,1000){
                        override fun onTick(millisUntilFinished: Long) {
                            Timber.i("Retrying in $millisUntilFinished")
                        }
                        override fun onFinish() {
                            isDoneLoading.value = false
                            refreshProduct()
                        }
                    }
                    timer.start()
                }
            }
        }
    }

}
class ImageAdapter (private val clickListener: Listener) : ListAdapter<String, ImageAdapter.ImageViewHolder>(
    ProductDiffCallback()) {

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = getItem(position)
        holder.bind(image, clickListener)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder.from(parent)
    }

    class ImageViewHolder private constructor(private val binding: ImageItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: String, clickListener: Listener) {
            binding.listener = clickListener
            binding.image = item
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ImageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ImageItemBinding.inflate(layoutInflater, parent, false)
                return ImageViewHolder(binding)
            }
        }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<String>() {

    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}
class Listener(val clickListener: (image: String) -> Unit) {
    fun onClick(image: String) = clickListener(image)
}
