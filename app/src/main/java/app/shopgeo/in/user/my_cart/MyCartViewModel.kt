package app.shopgeo.`in`.user.my_cart

import android.annotation.SuppressLint
import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.shopgeo.`in`.user.data.LoginDataSource
import app.shopgeo.`in`.user.data.LoginRepository
import app.shopgeo.`in`.databinding.CartItemBinding
import app.shopgeo.`in`.user.network.Cart
import app.shopgeo.`in`.user.network.NetworkApi
import app.shopgeo.`in`.user.network.UpdateCart
import app.shopgeo.`in`.user.network.UserAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MyCartViewModel(application : Application) : AndroidViewModel(application) {
    val loginRepository = LoginRepository(dataSource = LoginDataSource(),application)
    val cartItems = MutableLiveData<List<Cart>?>()
    val cartTotalCost = Transformations.map(cartItems){ list ->
        var totalCost = 0
        list?.let{
            list.forEach {
                totalCost += it.price*it.quantity
            }
        }
        totalCost
    }

    private val _userAddress  = MutableLiveData<List<UserAddress>?>()
    val userAddress : LiveData<List<UserAddress>?>
        get() = _userAddress
    val getAddress : (Int) -> List<UserAddress>? = { id->
        _userAddress.value?.filter {
            it.address_id == id
        }
    }

    val formatedAddress : (UserAddress) -> String = { address->
        address.name +", "+ address.pinCode+ ", "+ address.landmark +", "+ address.locality + ", " +
                address.city + ", "+ address.state
    }
    var currentSelectedAddress = MutableLiveData<UserAddress?>()

    val isDoneLoading = MutableLiveData<Boolean>()
    var isNetworkError = MutableLiveData<Boolean>()

    init{
        if(loginRepository.isLoggedIn){
            getCart()
            getAddress()
        }else{
            isDoneLoading.value = true
        }
    }
    fun onAddressChanged(id: Int){
        currentSelectedAddress.value = getAddress(id)?.get(0)
    }

    private fun getAddress(){
        try {
            viewModelScope.launch {
                val loginCredential = loginRepository.get_credentials()
                if (loginCredential != null) {
                    withContext(Dispatchers.IO) {
                        val address = NetworkApi.RETROFIT_SERVICES.getAddress(loginCredential)
                        withContext(Dispatchers.Main){
                            _userAddress.value = address
                            currentSelectedAddress.value = _userAddress.value?.filter {
                                it.is_default == '1'
                            }?.get(0)
                        }
                    }

                }
            }
        }catch (e : Throwable){
            Timber.e("Cannot get Cart : $e")
//                getCart()
//            loginRepository.logout()
        }
    }
    private  fun setCartUpdated(){
        isDoneLoading.value = true
        isNetworkError.value = false
    }

    fun deleteFromCart(id : Int){
        viewModelScope.launch {
            try {
                isDoneLoading.value = false
                withContext(Dispatchers.IO){
                    val response = loginRepository.get_credentials()?.let {
                        UpdateCart(
                            id = id,
                            loginCredential = it,
                            type = "delete",
                            detail = null
                        )
                    }?.let { NetworkApi.RETROFIT_SERVICES.updateCart(it) }
                }
                setCartUpdated()
                cartItems.value = cartItems.value?.filter {
                    Timber.i("ProductId : $id and ${it.product_id}")
                    it.product_id!=id
                }
            } catch (networkError: Throwable) {
                Timber.i("Retrying")
                isNetworkError.value = true
            }
        }
    }

    fun updateCart(productId : Int,qty: Int){
        viewModelScope.launch {
            try {
                isDoneLoading.value = false
                withContext(Dispatchers.IO){
                    val response = loginRepository.get_credentials()?.let {
                        UpdateCart(
                            id = productId,
                            qty = qty,
                            detail = null,
                            loginCredential = it,
                            type = "add"
                        )
                    }?.let { NetworkApi.RETROFIT_SERVICES.updateCart(it) }
                }
                setCartUpdated()

            } catch (networkError: Throwable) {
                Timber.i("Retrying")
                isNetworkError.value = true
            }
        }
    }
    private fun getCart(){
        try {
            viewModelScope.launch {
                val loginCredential = loginRepository.get_credentials()
                if (loginCredential != null) {
                    withContext(Dispatchers.IO) {
                        val cartitems = NetworkApi.RETROFIT_SERVICES.getCart(loginCredential)
                        withContext(Dispatchers.Main){
                            cartItems.value = cartitems
                            setCartUpdated()
                        }
                    }
                }
            }
        }catch (e : Throwable){
            Timber.e("Cannot get Cart : $e")
//                getCart()
//            loginRepository.logout()
        }
    }
}

class CartAdapter (private val clickListener: Listener) : ListAdapter<Cart, CartAdapter.CartViewHolder>(
    ProductDiffCallback()) {

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cart = getItem(position)
        holder.bind(cart, clickListener)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        return CartViewHolder.from(parent)
    }

    class CartViewHolder private constructor(private val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Cart, clickListener: Listener) {
            binding.listener = clickListener
            binding.cart = item
            binding.quantity.hint = item.quantity.toString()
            binding.quantity.doOnTextChanged { text, start, before, count ->
                Timber.i("Quantity Changed : $text")
            }
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): CartViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CartItemBinding.inflate(layoutInflater, parent, false)
                return CartViewHolder(binding)
            }
        }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Cart>() {

    override fun areItemsTheSame(oldItem: Cart, newItem: Cart): Boolean {
        return oldItem.product_id == newItem.product_id
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Cart, newItem: Cart): Boolean {
        return oldItem == newItem
    }
}
class Listener(val clickListener: (item: Cart) -> Unit, val delete: (item: Cart) -> Unit) {
    fun onClick(item: Cart) = clickListener(item)

    fun onDelete(item : Cart) = delete(item)
}

@Suppress("UNCHECKED_CAST")
class MyCartViewModelFactory(private val application: Application
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyCartViewModel::class.java)) {
            return MyCartViewModel(application) as T        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}