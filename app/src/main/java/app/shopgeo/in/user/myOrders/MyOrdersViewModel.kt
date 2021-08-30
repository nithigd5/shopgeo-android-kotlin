package app.shopgeo.`in`.user.myOrders

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
import app.shopgeo.`in`.databinding.OrderItemBinding
import app.shopgeo.`in`.user.network.NetworkApi
import app.shopgeo.`in`.user.network.Orders
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MyOrdersViewModel(application: Application) : AndroidViewModel(application) {
    val loginRepository = LoginRepository(dataSource = LoginDataSource(),application)

    val orders = MutableLiveData<List<Orders>?>()

    val isDoneLoading = MutableLiveData<Boolean>()
    var isNetworkError = MutableLiveData<Boolean>()

    init{
        if(loginRepository.isLoggedIn){
            isDoneLoading.value = false
            getOrders()
        }
    }

    private  fun setOrdersUpdated(){
        isDoneLoading.value = true
        isNetworkError.value = false
    }

    private fun getOrders(){
        try {
            viewModelScope.launch {
                val loginCredential = loginRepository.get_credentials()
                Timber.i("Login Credentials: $loginCredential")
                if (loginCredential != null) {
                    withContext(Dispatchers.IO) {
                        val res = NetworkApi.RETROFIT_SERVICES.getOrders(loginCredential)
                        withContext(Dispatchers.Main){
                            when(res){
                                is NetworkResponse.Success<List<Orders>> -> {
                                    orders.value = res.body
                                    Timber.i("Success: $res")
                                }
                                is NetworkResponse.Error -> {
                                    isNetworkError.value = true
                                    Timber.i("Error : $res")
                                }
                            }
                            setOrdersUpdated()
                            Timber.i("Orders :${orders.value}")
                        }
                    }
                }
            }
        }catch (e : Throwable){
            Timber.e("Cannot get Cart : $e")
        }
    }
}


class MyOrdersAdapter (private val clickListener: Listener) : ListAdapter<Orders, MyOrdersAdapter.MyOrdersHolder>(
    ProductDiffCallback()) {

    override fun onBindViewHolder(holder: MyOrdersHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, clickListener)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrdersHolder {
        return MyOrdersHolder.from(parent)
    }

    class MyOrdersHolder private constructor(private val binding: OrderItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Orders, clickListener: Listener) {
            binding.listener = clickListener
            binding.order = item
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): MyOrdersHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = OrderItemBinding.inflate(layoutInflater, parent, false)
                return MyOrdersHolder(binding)
            }
        }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Orders>() {

    override fun areItemsTheSame(oldItem: Orders, newItem: Orders): Boolean {
        return oldItem.product_id == newItem.product_id
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Orders, newItem: Orders): Boolean {
        return oldItem == newItem
    }
}

class Listener(val clickListener: (item: Orders) -> Unit) {
    fun onClick(item: Orders) = clickListener(item)
}

@Suppress("UNCHECKED_CAST")
class MyOrdersViewModelFactory(private val application: Application
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyOrdersViewModel::class.java)) {
            return MyOrdersViewModel(application ) as T        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}