package app.shopgeo.`in`.user.home

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.shopgeo.`in`.R
import app.shopgeo.`in`.user.data.LoginDataSource
import app.shopgeo.`in`.user.data.LoginRepository
import app.shopgeo.`in`.user.database.DatabaseProduct
import app.shopgeo.`in`.user.database.ProductDatabase
import app.shopgeo.`in`.user.network.NetworkApi
import app.shopgeo.`in`.user.network.UpdateWishlist
import app.shopgeo.`in`.user.network.Wishlist
import app.shopgeo.`in`.user.notification.sendNotification
import app.shopgeo.`in`.user.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import timber.log.Timber
import java.net.SocketTimeoutException

class HomeViewModel(
    database : ProductDatabase,
    application: Application) : AndroidViewModel(application) {

    private val homeRepository = HomeRepository(database)

    val loginRepository = LoginRepository(dataSource = LoginDataSource(),application)

    lateinit var adapter : HomeAdapter

    private val _navToProduct = MutableLiveData<Int?>()
    val navToProduct : LiveData<Int?>
        get() = _navToProduct


    val products : (String) -> List<DatabaseProduct> = { category : String ->
        val list = database.productsDao.getProductsByCategory(category)
        list
    }
    private var _eventNetworkError = MutableLiveData<Boolean?>()

    val eventNetworkError: LiveData<Boolean?>
        get() = _eventNetworkError

    private var _isNetworkErrorShown = MutableLiveData(false)

    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown

    val categories  = homeRepository.categories

   val sliderImages = homeRepository.sliderImages

    val categoryList = homeRepository.categoriesList

    var recyclerViewList = MutableLiveData<List<DataItem?>>()

    val isDoneLoading = MutableLiveData<Boolean>()
    var fetchAttempt = 0

    val setWishListUpdate = MutableLiveData<Boolean>()

    private val _wishList = MutableLiveData<List<Wishlist>?>()
    val wishList: LiveData<List<Wishlist>?>
        get() = _wishList

    private val notificationManager = ContextCompat.getSystemService(
        application.applicationContext,
        NotificationManager::class.java
    ) as NotificationManager

    init {
        refreshRepository()

        if(loginRepository.isLoggedIn){
            getWishlist()
        }else _wishList.value = null

        createChannel(
            application.getString(R.string.notification_channel_id_offers),
            application.getString(R.string.notification_channel_offers)
        )

        notificationManager.sendNotification("Thank you for using shopgeo", application.applicationContext)
    }
    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Time for breakfast"

            val notificationManager = getApplication<Application>().getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun setWishListUpdated(){
        isDoneLoading.value = false
        _eventNetworkError.value = false
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
                            _eventNetworkError.value = false
                        }
                    }
                }
            }
        }catch (e : Throwable){
            Timber.e("Cannot get Cart : $e")
            _eventNetworkError.value = true
        }
    }
    fun updateWishlist(id : Int){
        if(!loginRepository.isLoggedIn){
            return
        }
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
                _eventNetworkError.value = true
            }
        }
    }
    fun onProductClicked(id : Int){
        _navToProduct.value = id
    }

    fun navigatedToProduct(){
        _navToProduct.value = null
    }
    fun onNetworkErrorShown(){
        _isNetworkErrorShown.value = true
    }

    private fun refreshRepository(){
        viewModelScope.launch {
            try {
                homeRepository.refreshProductsList()
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false
                isDoneLoading.value = true

            } catch (networkError: SocketTimeoutException) {
                // Show a Toast error message and hide the progress bar.
                if(categories.value.isNullOrEmpty())
                    _eventNetworkError.value = true
                fetchAttempt += 1
                isDoneLoading.value = true
                if(fetchAttempt<3) {
                    isDoneLoading.value = false
                    refreshRepository()
                }
                else{
                    val timer = object : CountDownTimer(100000,1000){
                        override fun onTick(millisUntilFinished: Long) {
                            Timber.i("Retrying: $millisUntilFinished")
                        }
                        override fun onFinish() {
                            isDoneLoading.value = false
                            refreshRepository()
                        }

                    }
                    timer.start()
                }
                Timber.i("Error Refreshing Repository %s",networkError.toString())
            }
        }
    }
}