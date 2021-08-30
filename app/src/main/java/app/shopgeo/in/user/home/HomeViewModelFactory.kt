package app.shopgeo.`in`.user.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.shopgeo.`in`.user.database.ProductDatabase

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(private val dataSource: ProductDatabase,
                           private val application: Application) :ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(dataSource,application) as T        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}