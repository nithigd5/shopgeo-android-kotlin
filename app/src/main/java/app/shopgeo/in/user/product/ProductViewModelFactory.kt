package app.shopgeo.`in`.user.product

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.shopgeo.`in`.user.database.ProductDatabase

@Suppress("UNCHECKED_CAST")
class ProductViewModelFactory(private val ProductId: Int, private val database: ProductDatabase,
                              private val application: Application
):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            return ProductViewModel(ProductId, database,application ) as T        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}