package app.shopgeo.`in`.user


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import app.shopgeo.`in`.R
import app.shopgeo.`in`.user.data.LoginDataSource
import app.shopgeo.`in`.user.data.LoginRepository
import app.shopgeo.`in`.databinding.ActivityMainBinding
import app.shopgeo.`in`.user.network.NetworkApi
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    protected var mMyApp: ShopgeoApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMyApp = this.applicationContext as ShopgeoApplication
        mMyApp!!.setCurrentActivity( this )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val navController = this.findNavController(R.id.myNavHostFragment)
//        NavigationUI.setupActionBarWithNavController(this,navController, binding.drawerLayout)

        val navView: NavigationView = binding.navView

         NavigationUI.setupWithNavController(navView, this.findNavController(R.id.myNavHostFragment))


        setUserLogin()
//        setupActionBarTitle()

        checkForUpdate(this)
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun openUrl(url : String){
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if(intent.resolveActivity(packageManager)!=null){
            startActivity(intent)
        }
        Timber.i("Url: $url, URI : $uri")
    }

    fun checkForUpdate(activity: MainActivity){
        MainScope().launch {
           withContext(Dispatchers.IO){
               val getUpdatedAppVersion = NetworkApi.RETROFIT_SERVICES.getAppVersion()
                withContext(Dispatchers.Main){
                    val currentAppVersion = packageManager.getPackageInfo(packageName,0)
                    Timber.i("App version: ${currentAppVersion.versionCode} to ${getUpdatedAppVersion.versionCode}")

                    if(getUpdatedAppVersion.versionCode>currentAppVersion.versionCode){
                        Timber.i("Update your app for better experience: ${currentAppVersion.versionName} to ${getUpdatedAppVersion.versionName}")
                        val alert = AlertDialog.Builder(
                            activity
                        ).create()
                        alert.apply{
                            setTitle("New Update Found")
                            setMessage("Please update your app for better experience and features")
                            setButton(DialogInterface.BUTTON_NEUTRAL,"Update",DialogInterface.OnClickListener { dialogInterface, i ->
                                Timber.i("Update Clicked")
                                getUpdatedAppVersion.updateUrl?.let { openUrl(it) }
                            })
                            show()
                        }
                    }
                }
           }
        }
    }

    private fun setUserLogin(){
        val loginRepository = LoginRepository(LoginDataSource(),requireNotNull(this).application)
         if(loginRepository.isLoggedIn) {
             binding.navView.menu.findItem(R.id.login_now).isVisible = false
             binding.navView.menu.findItem(R.id.register).isVisible = false
             binding.navView.getHeaderView(0).findViewById<TextView>(R.id.user).text =
                 getString(R.string.welcome_user, loginRepository.user?.displayName)

         }else{
             binding.navView.menu.findItem(R.id.myAccount).isVisible = false
             binding.navView.menu.findItem(R.id.myCart).isVisible = false
             binding.navView.menu.findItem(R.id.myWishList).isVisible = false
             binding.navView.menu.findItem(R.id.myOrders).isVisible = false
         }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(this.findNavController(R.id.myNavHostFragment), binding.drawerLayout)
    }

    fun setActionBarTitle(title : String) {
        val toolBar = supportActionBar
        toolBar?.title = title
    }

    private fun setupActionBarTitle(){
//        this.findNavController(R.id.myNavHostFragment).addOnDestinationChangedListener { _, destination: NavDestination, _ ->
//            val toolBar = supportActionBar ?: return@addOnDestinationChangedListener
//            toolBar.navigationIcon = resources.getDrawable(R.drawable.ic_baseline_arrow_back_ios_24)
//            when(destination.id) {
//                R.id.home2 -> {
//                    toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_baseline_menu_24)
//                    toolbar.title = resources.getString(R.string.app_name)
//                    binding.searchBar.visibility = View.VISIBLE
//
//                }
//                R.id.searchFragment ->{
//                    toolbar.visibility = View.GONE
//                }
//                else -> {
////                    toolBar.setDisplayShowTitleEnabled(false)
////                    binding.search.visibility = View.GONE
//                }
//            }
//        }
    }
//    private fun setupNavigation(id : Int): Boolean {
//        return when(id){
//            R.id.login_now -> {
//                startActivity(intent)
//                true
//            }
//            R.id.myCart ->{
//                val myCartFragment = MyCart()
//                show(myCartFragment)
//                true
//            }
//            else -> false
//        }
//    }



    }