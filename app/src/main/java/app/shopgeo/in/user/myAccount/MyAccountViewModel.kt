package app.shopgeo.`in`.user.myAccount

import android.app.Application
import android.util.Patterns.EMAIL_ADDRESS
import android.widget.Toast
import androidx.lifecycle.*
import app.shopgeo.`in`.user.data.LoginDataSource
import app.shopgeo.`in`.user.data.LoginRepository
import app.shopgeo.`in`.user.network.*
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.regex.Pattern

class MyAccountViewModel(application: Application) : AndroidViewModel(application) {
    val loginRepository = LoginRepository(dataSource = LoginDataSource(),application)

    val user = MutableLiveData<UserDetail?>()
    private val userAddress = MutableLiveData<List<UserAddress>?>()

    val formattedAddress : (UserAddress) -> String = { address->
        address.name +", "+ address.pinCode+ ", "+ address.landmark +", "+ address.locality + ", " +
                address.city + ", "+ address.state
    }
    val getAddress : (Int) -> List<UserAddress>? = { id->
        userAddress.value?.filter {
            it.address_id == id
        }
    }
    val isDoneLoading = MutableLiveData<Boolean>()
    var isNetworkError = MutableLiveData<Boolean>()
    var currentSelectedAddress = MutableLiveData<UserAddress?>()

    init{
        Timber.i("ViewModel Created")
        if(loginRepository.isLoggedIn){
            getUser()
            getAddress()
        }
    }

    private  fun setUserDetailUpdated(){
        isDoneLoading.value = true
        isNetworkError.value = false
    }

    private fun getAddress(){
        try {
            isDoneLoading.value = false
            viewModelScope.launch {
                val loginCredential = loginRepository.get_credentials()
                if (loginCredential != null) {
                    withContext(Dispatchers.IO) {
                        val address = NetworkApi.RETROFIT_SERVICES.getAddress(loginCredential)
                        Timber.i("Address: $address")
                        withContext(Dispatchers.Main){
                            userAddress.value = address
                            isDoneLoading.value = true
                            currentSelectedAddress.value = userAddress.value?.filter {
                                it.is_default == '1'
                            }?.get(0)
                        }
                    }

                }
            }
        }catch (e : Throwable){
            Timber.e("Cannot get Cart : $e")
        }
    }
    private fun getUser(){
        try {
            isDoneLoading.value = false
            viewModelScope.launch {
                val loginCredential = loginRepository.get_credentials()
                if (loginCredential != null) {
                    withContext(Dispatchers.IO) {
                        val item = NetworkApi.RETROFIT_SERVICES.getUser(loginCredential)
                        withContext(Dispatchers.Main){
                            user.value = item
                            setUserDetailUpdated()
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
     fun updatePhone(phone : String){
        Timber.i("Phone Updated to : $phone")

         val phonePattern = "[6-9][0-9]{9}"
         if(!Pattern.matches(phonePattern,phone)){
             Toast.makeText(getApplication<Application>().applicationContext,"Invalid Mobile Number",Toast.LENGTH_LONG).show()
             return
         }
         isDoneLoading.value = false

         viewModelScope.launch {
             val loginCredential = loginRepository.get_credentials()
             if (loginCredential != null) {
                 withContext(Dispatchers.IO) {
                     val res = NetworkApi.RETROFIT_SERVICES.updateUser(
                         UpdateUser(
                         loginCredential,
                             type="update_phone",
                             phone = phone,
                             email = null,
                             name = null
                         )
                     )
                     withContext(Dispatchers.Main){
                         when(res){
                             is NetworkResponse.Success<*> -> {
                                 Timber.i("Success: ${res.body}")
                                 val body = res.body as Response
                                 if(body.result=="success"){
                                     getUser()
                                     Toast.makeText(getApplication<Application>().applicationContext,"Successfully Changed Phone Number",Toast.LENGTH_LONG).show()
                                 }else{
                                     Toast.makeText(getApplication<Application>().applicationContext,"Unable to change Mobile No",Toast.LENGTH_LONG).show()
                                 }
                             }
                             is NetworkResponse.Error -> {
                                 Timber.i("Error : ${res.error}")
                                 isNetworkError.value = true
                             }
                         }
                         setUserDetailUpdated()
                     }
                 }
             }
         }

    }

    fun verifyOTP(otp : String){
//        if(otp.length!=6){
//            Toast.makeText(getApplication<Application>().applicationContext,"Invalid OTP",Toast.LENGTH_LONG).show()
//            return
//        }
        isDoneLoading.value = false

        viewModelScope.launch {
            val loginCredential = loginRepository.get_credentials()
            if (loginCredential != null) {
                withContext(Dispatchers.IO) {
                    val res = NetworkApi.RETROFIT_SERVICES.updateUser(
                        UpdateUser(
                            loginCredential,
                            type="update_email",
                            phone = null,
                            email = null,
                            name = null,
                            otp  = otp,
                            sessionId = sessionId
                        )
                    )
                    withContext(Dispatchers.Main){
                        when(res){
                            is NetworkResponse.Success<*> -> {
                                Timber.i("Success: ${res.body}")
                                val body = res.body as Response
                                if(body.result=="success"){
                                    getUser()
                                    Toast.makeText(getApplication<Application>().applicationContext,"Email Changed",Toast.LENGTH_LONG).show()
                                }else if(body.result=="wrongotp"){
                                    Toast.makeText(getApplication<Application>().applicationContext,"Wrong OTP",Toast.LENGTH_LONG).show()
                                    openEmailDialog.value = true
                                }else if(body.result=="limit_reached"){
                                    Toast.makeText(getApplication<Application>().applicationContext,"Maximum attempt reached",Toast.LENGTH_LONG).show()
                                    openEmailDialog.value = false
                                }
                            }
                            is NetworkResponse.Error -> {
                                Timber.i("Error : ${res.error}")
                                isNetworkError.value = true
                            }
                        }
                        setUserDetailUpdated()
                    }
                }
            }
        }
    }
    val openEmailDialog = MutableLiveData<Boolean?>()

    private var sessionId : String? = null

    fun updateEmail(email : String){
         if(!EMAIL_ADDRESS.matcher(email).matches()){
             Toast.makeText(getApplication<Application>().applicationContext,"Invalid Email",Toast.LENGTH_LONG).show()
             return
         }
         isDoneLoading.value = false

         viewModelScope.launch {
             val loginCredential = loginRepository.get_credentials()
             if (loginCredential != null) {
                 withContext(Dispatchers.IO) {
                     val res = NetworkApi.RETROFIT_SERVICES.updateUser(
                         UpdateUser(
                         loginCredential,
                             type="update_email",
                             phone = null,
                             email = email,
                             name = null,
                         )
                     )
                     withContext(Dispatchers.Main){
                         when(res){
                             is NetworkResponse.Success<*> -> {
                                 Timber.i("Success: ${res.body}")
                                 val body = res.body as Response
                                 if(body.result=="waiting"){
                                     openEmailDialog.value = true
                                     sessionId = body.reason!!
                                     Toast.makeText(getApplication<Application>().applicationContext,"OTP Sent to your email",Toast.LENGTH_LONG).show()
                                 }else{
                                     Toast.makeText(getApplication<Application>().applicationContext,"Unable to send OTP",Toast.LENGTH_LONG).show()
                                 }
                             }
                             is NetworkResponse.Error -> {
                                 Timber.i("Error : ${res.error}")
                                 isNetworkError.value = true
                             }
                         }
                         setUserDetailUpdated()
                     }
                 }
             }
         }

    }
    fun updateName(name : String){

        if(name.length<5){
            Toast.makeText(getApplication<Application>().applicationContext,"Please Enter atleast 4 letters",
                Toast.LENGTH_LONG).show()
            return
        }
        isDoneLoading.value = false

        viewModelScope.launch {
            val loginCredential = loginRepository.get_credentials()
            if (loginCredential != null) {
                withContext(Dispatchers.IO) {
                    val res = NetworkApi.RETROFIT_SERVICES.updateUser(
                        UpdateUser(
                            loginCredential,
                            type="update_name",
                            phone = null,
                            email = null,
                            name = name
                        )
                    )
                    withContext(Dispatchers.Main){
                        when(res){
                            is NetworkResponse.Success<*> -> {
                                Timber.i("Success: ${res.body}")
                                val body = res.body as Response
                                if(body.result=="success"){
                                    getUser()
                                    Toast.makeText(getApplication<Application>()
                                        .applicationContext,"Successfully Changed your Namer",Toast.LENGTH_LONG).show()
                                }else{
                                    Toast.makeText(getApplication<Application>().applicationContext,
                                        "Unable to change your name",Toast.LENGTH_LONG).show()
                                }
                            }
                            is NetworkResponse.Error -> {
                                Timber.i("Error : ${res.error}")
                                isNetworkError.value = true
                            }
                        }
                        setUserDetailUpdated()
                    }
                }
            }
        }
    }
    var isLoggedOut = MutableLiveData<Boolean?>()
    fun logout(){
        isDoneLoading.value = false
        viewModelScope.launch {
            loginRepository.logout()
            with(Dispatchers.Main){
                isDoneLoading.value = true
                isLoggedOut.value = true
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        Timber.i("ViewModel Cleared")
    }
}

@Suppress("UNCHECKED_CAST")
class MyAccountViewModelFactory(private val application: Application
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyAccountViewModel::class.java)) {
            return MyAccountViewModel(application) as T        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}