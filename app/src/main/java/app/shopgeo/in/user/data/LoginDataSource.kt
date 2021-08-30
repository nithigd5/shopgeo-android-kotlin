package app.shopgeo.`in`.user.data

import android.app.Application
import android.content.Context
import android.widget.Toast
import app.shopgeo.`in`.user.data.model.LoggedInUser
import app.shopgeo.`in`.user.data.model.LoginCredential
import app.shopgeo.`in`.user.network.NetworkApi
import app.shopgeo.`in`.user.network.UserLoginDetails
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    suspend fun login(
        username: String,
        password: String,
        activity: Application?
    ): Result<LoggedInUser> {
        return try {
            withContext(Dispatchers.IO) {
                val loginDetails = NetworkApi.RETROFIT_SERVICES.login(
                    UserLoginDetails(
                        username = username,
                        password = password
                    )
                )
                Timber.i("Login Details: $loginDetails")
                if (loginDetails.status == "Success") {
                    val user = LoggedInUser(username, loginDetails.name!!)
                    val sharedPref = activity?.getSharedPreferences(
                        "app.shopgeo.in.loginCredentials",
                        Context.MODE_PRIVATE
                    )
                    with(sharedPref?.edit()) {
                        this?.let {
                            putString("username", loginDetails.username)
                            putString("password", loginDetails.password)
                            putString("device_id", loginDetails.device_id)
                            putString("name", loginDetails.name)
                            apply()
                        }
                    }
                    Result.Success(user)
                } else {
                    Result.Error(IOException("Invalid Credentials"))
                }
            }
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        } catch (e: IOException) {
            Result.Error(IOException("Network Error", e))
        }
    }

    suspend fun logout(activity: Application?, loginCredentials: LoginCredential?) {

        with(Dispatchers.IO) {

            loginCredentials?.let {
                val res = NetworkApi.RETROFIT_SERVICES.logout(loginCredentials)

                withContext(Dispatchers.Main) {
                    when (res) {
                        is NetworkResponse.Success<*> -> {

                            Toast.makeText(
                                    activity?.applicationContext,
                            "Successfully Logged Out",
                            Toast.LENGTH_LONG
                            ).show()

                        }
                        is NetworkResponse.Error -> {
                            Toast.makeText(
                                activity?.applicationContext,
                                "Error While Logging Out",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                activity?.applicationContext,
                                "Error While Logging Out",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                    val sharedPref =
                        activity?.getSharedPreferences(
                            "app.shopgeo.in.loginCredentials",
                            Context.MODE_PRIVATE
                        )
                    with(sharedPref?.edit()) {
                        this?.let {
                            remove("username")
                            remove("password")
                            remove("device_id")
                            remove("name")
                            apply()
                        }
                    }
                }
            }
        }

    }
}