package app.shopgeo.`in`.user.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import app.shopgeo.`in`.user.database.Banners
import app.shopgeo.`in`.user.database.ProductDatabase
import app.shopgeo.`in`.user.database.asSlideMode
import app.shopgeo.`in`.user.network.NetworkApi
import app.shopgeo.`in`.user.network.NetworkProduct
import app.shopgeo.`in`.user.network.asDatabaseModel
import com.denzcoskun.imageslider.models.SlideModel
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.executeWithRetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class HomeRepository (private val database: ProductDatabase){

    val categories: LiveData<List<String>> = database.productsDao.getCategories()

    val categoriesList: LiveData<List<Banners>> = database.bannersDao.getCategoryList()


    val sliderImages : LiveData<List<SlideModel>> = Transformations.map(database.bannersDao.getSliderImages()){
        it.asSlideMode()
    }

    suspend fun refreshProductsList() {
        withContext(Dispatchers.IO) {
            val res = executeWithRetry(times = 5) {
                NetworkApi.RETROFIT_SERVICES.getAllProducts()
            }
            Timber.i("Res: $res")
            when(res){
                is NetworkResponse.Success<List<NetworkProduct>> -> {
                    Timber.i("Success: ${res.body}")
                    val products = res.body
                        database.productsDao.insertAll(products.asDatabaseModel())
                }
                is NetworkResponse.Error -> {
                    Timber.i("Error : ${res.error}")
                }
            }
            val banners = NetworkApi.RETROFIT_SERVICES.getBanners()

            database.bannersDao.insertAll(banners)
        }
    }
}