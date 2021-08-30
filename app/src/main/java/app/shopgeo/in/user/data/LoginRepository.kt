package app.shopgeo.`in`.user.data

import android.app.Application
import android.content.Context
import app.shopgeo.`in`.user.data.model.LoggedInUser
import app.shopgeo.`in`.user.data.model.LoginCredential

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource, private val application : Application) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
        checkLogin()
    }

    suspend fun logout() {
        user = null
        dataSource.logout(application,get_credentials())
    }

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        // handle login
        if(isLoggedIn) return Result.Success(user!!)
        val result = dataSource.login(username, password,application)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }
        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
    private fun checkLogin(){
        val LoginCredential = get_credentials()
        LoginCredential?.let{
            this.user = LoggedInUser(userId = LoginCredential.username,displayName = LoginCredential.name)
        }
    }
    fun get_credentials() : LoginCredential? {
        val sharedPref = application.getSharedPreferences("app.shopgeo.in.loginCredentials",Context.MODE_PRIVATE) ?: return null
        val username = sharedPref.getString("username",null)
        val password = sharedPref.getString("password",null)
        val deviceId = sharedPref.getString("device_id",null)
        val name = sharedPref.getString("name",null)
        return if(!(username.isNullOrEmpty()||password.isNullOrEmpty()||deviceId.isNullOrEmpty()||name.isNullOrEmpty())){
            LoginCredential(
                username,password,deviceId,name)
        } else null
    }

}