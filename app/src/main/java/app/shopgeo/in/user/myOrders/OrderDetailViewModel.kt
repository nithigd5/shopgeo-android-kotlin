package app.shopgeo.`in`.user.myOrders

import android.app.Application
import androidx.lifecycle.*
import app.shopgeo.`in`.user.data.LoginDataSource
import app.shopgeo.`in`.user.data.LoginRepository
import app.shopgeo.`in`.user.network.GetOrder
import app.shopgeo.`in`.user.network.NetworkApi
import app.shopgeo.`in`.user.network.Orders
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.executeWithRetry
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class OrderDetailViewModel(private  val orderId : String , application: Application) : AndroidViewModel(application) {
    val loginRepository = LoginRepository(dataSource = LoginDataSource(),application)

    val order = MutableLiveData<Orders?>()

    val isDoneLoading = MutableLiveData<Boolean>()
    var isNetworkError = MutableLiveData<Boolean>()


    init{
        if(loginRepository.isLoggedIn){
            isDoneLoading.value = false
            getOrder()
        }
    }

    private  fun setOrdersUpdated(){
        isDoneLoading.value = true
        isNetworkError.value = false
    }

    private fun getOrder(){
        try {
            viewModelScope.launch {
                val loginCredential = loginRepository.get_credentials()
                if (loginCredential != null) {
                    withContext(Dispatchers.IO) {
                        val res = executeWithRetry(times = 5) {
                           NetworkApi.RETROFIT_SERVICES.getOrder(
                                GetOrder(
                                    loginCredential = loginCredential,
                                    orderId = orderId
                                )
                            )
                        }

                        withContext(Dispatchers.Main){
                            when(res){
                                is NetworkResponse.Success<List<Orders>> -> {
                                    Timber.i("Success: ${res.body}")
                                    if(res.body.isNotEmpty()){
                                        order.value = res.body[0]
                                    }
                                }
                                is NetworkResponse.Error -> {
                                    Timber.i("Error : ${res.error}")
                                }
                            }
                            setOrdersUpdated()
                        }
                    }
                }
            }
        }catch (e : JsonDataException){
            Timber.e("Cannot parse Order : $e")
            Timber.e("Cannot get Order : $e")
        }
    }
}

@Suppress("UNCHECKED_CAST")
class OrderDetailViewModelFactory(private val orderId: String, private val application: Application
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderDetailViewModel::class.java)) {
            return OrderDetailViewModel(orderId, application ) as T        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}