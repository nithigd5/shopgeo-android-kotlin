package app.shopgeo.`in`.user.network

import app.shopgeo.`in`.user.data.model.LoginCredential
import app.shopgeo.`in`.user.database.Banners
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val BASE_URL = "https://shopgeo.in/app/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addCallAdapterFactory(NetworkResponseAdapterFactory())
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ShopgeoApiServices {
    @GET("getAllProducts")
    suspend fun getAllProducts(): NetworkResponse<List<NetworkProduct>, ErrorResponse>

    @GET("getCategories")
    suspend fun getCategories(): NetworkResponse<List<Banners>, ErrorResponse>

    @GET("getAllBanners")
    suspend fun getBanners() : List<Banners>

    @GET("getProduct")
    suspend fun getProduct(@Query("id") id : Int) : List<NetworkProduct>

    @GET("search")
    suspend fun search(@Query("keywords") keywords : String) : List<NetworkProduct>

    @POST("getCart")
    suspend fun getCart(@Body user : LoginCredential) : List<Cart>

    @POST("getUser")
    suspend fun getUser(@Body user : LoginCredential) : UserDetail

    @POST("getWishlist")
    suspend fun getWishList(@Body user : LoginCredential) : List<Wishlist>

    @POST("getOrders")
    suspend fun getOrders(@Body user : LoginCredential) : NetworkResponse<List<Orders>, ErrorResponse>

    @POST("getOrder")
    suspend fun getOrder(@Body user : GetOrder) : NetworkResponse<List<Orders>, ErrorResponse>

   @POST("getAddress")
    suspend fun getAddress(@Body user : LoginCredential) : List<UserAddress>

    @POST("login")
    suspend fun login(@Body user : UserLoginDetails) : LoginResponse

    @POST("logout")
    suspend fun logout(@Body user : LoginCredential) : NetworkResponse<Response, ErrorResponse>

    @POST("update_cart")
    suspend fun updateCart(@Body user : UpdateCart) : Response

    @POST("updateWishlist")
    suspend fun updateWishList(@Body user : UpdateWishlist) : Response

    @GET("getAppVersion")
    suspend fun getAppVersion() : AppVersion

    @POST("update_user")
    suspend fun updateUser(@Body userInput : UpdateUser) : NetworkResponse<Response, ErrorResponse>
}

object NetworkApi {
    val RETROFIT_SERVICES : ShopgeoApiServices by lazy {
        retrofit.create(ShopgeoApiServices :: class.java)
    }
}
