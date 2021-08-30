package app.shopgeo.`in`.user.database

import androidx.lifecycle.LiveData
import androidx.room.*
import app.shopgeo.`in`.user.category.CategoryItem

@Dao
interface ProductsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: DatabaseProduct)

    @Update
    fun update(product: DatabaseProduct)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( Products: List<DatabaseProduct>)

    @Query("SELECT *from Products where id = :id ")
    fun get(id : Int) : LiveData<DatabaseProduct>

    @Query("DELETE FROM Products")
    fun clear()

    @Query("SELECT *from Products")
    fun getAll()  : LiveData<List<DatabaseProduct>>

    @Query("SELECT *from Products where id = :id ")
    fun getProduct(id : Int) : LiveData<List<DatabaseProduct>>

    @Query("SELECT DISTINCT Category from products")
    fun getCategories() : LiveData<List<String>>

    @Query("SELECT DISTINCT Category,Images from products")
    fun getCategoryList() : LiveData<List<CategoryItem>>

    @Query("SELECT *from products where Category = :cat ")
    fun getProductsByCategory(cat : String) : List<DatabaseProduct>
}

@Dao
interface BannersDao{
    @Insert
    fun insert(product: Banners)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( Products: List<Banners>)

    @Query("SELECT *FROM banners where image_type = 'slider' ")
    fun getSliderImages() : LiveData<List<Banners>>

    @Query("SELECT *FROM banners where image_type = 'banner' ")
    fun getBannerImages() : LiveData<List<Banners>>

     @Query("SELECT *FROM banners where image_type = 'category_list' ")
    fun getCategoryList() : LiveData<List<Banners>>


}
