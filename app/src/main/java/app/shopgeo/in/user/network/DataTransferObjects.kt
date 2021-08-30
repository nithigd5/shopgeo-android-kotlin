package app.shopgeo.`in`.user.network
import app.shopgeo.`in`.user.data.model.LoginCredential
import app.shopgeo.`in`.user.database.DatabaseProduct
import app.shopgeo.`in`.user.domain.Detail
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkProduct(
    var product_id : Int,
    var Title : String, var category : String,
    var sub_category : String,
    var Images : List<String>,
    var Price : Double,
    var MRP : Double,
    var rating : Double,
    var date : Long,
    var views : Int,
    var Details : List<Detail>?,
    var Descrption : String?,
    var manufacturer : String?,
    var vendor : String?,
    var mpn : String?,
    var in_stock : String?,
    var Size : List<String>?
)

fun List<NetworkProduct>.asDatabaseModel(): List<DatabaseProduct>{
    return map{ networkProduct ->
        DatabaseProduct(
               id = networkProduct.product_id,
               Title = networkProduct.Title,
               sub_category =  networkProduct.sub_category,
                Description = networkProduct.Descrption,
               category =  networkProduct.category,
               Images = networkProduct.Images,
               Price = networkProduct.Price,
               DateTime = networkProduct.date,
               views = networkProduct.views,
               rating = networkProduct.rating,
               MRP = networkProduct.MRP,
            vendor = networkProduct.vendor,
            Manufacturer = networkProduct.manufacturer,
            Details = networkProduct.Details?.map{
                                                 Detail(name = it.name, value = it.value)
            },
            in_stock = networkProduct.in_stock?.toInt(),
           Size = networkProduct.Size
        )
    }
}

data class Cart(
    val product_id: Int,
    val quantity : Int,
    val title : String,
    val image : String,
    val price : Int
)
data class Wishlist(
    val product_id: Int,
    val title : String,
    val image : String,
    val price : Int,
    val date : Long
)
data class Orders(
    val product_id: Int,

    @Json(name="ORDER_ID")
    val orderId: String,

    @Json(name="payment_id")
    val payment_id : String?,

    @Json(name="Quantity")
    val quantity : Int,

    val title : String,
    val image : String,

    @Json(name="Price")
    val price : Int,

    @Json(name="order_date")
    val orderDate : Long,

    @Json(name="Delivery_status")
    val deliveryStatus : String,

    @Json(name="payment_status")
    val paymentStatus : String,

    @Json(name="Payment_Mode")
    val paymentMode : String,

    val address : UserAddress?,

    val orderStatus : String
)

data class UserLoginDetails(
    val username : String,
    val password : String
)

data class LoginResponse(
    val status : String,
    val username : String?,
    val password : String?,
    val device_id : String?,
    val name : String?,
    val reason : String?
)

data class UserAddress(
    val address_id : Int,

    val name : String,

    @Json(name = "Locality")
    val locality : String,

    @Json(name="Landmark")
    val landmark : String,

    @Json(name="City")
    val city : String,

    @Json(name="State")
    val state : String,

    @Json(name="Pincode")
    val pinCode : String,

    @Json(name="Phone")
    val Phone : String,

    @Json(name="Type")
    val type : String,

    @Json(name="default_address")
    var is_default : Char
)

data class Response (val result: String, val reason : String?)

data class AppVersion(val versionCode : Int, val versionName : String, val updateUrl  : String?)

data class UpdateCart(
    val loginCredential: LoginCredential,
    val id : Int,
    val type : String,
    val qty : Int = 1,
    val detail : String?,
)
data class UpdateWishlist(
    val loginCredential: LoginCredential,
    val id : Int,
)

data class GetOrder(
    val loginCredential: LoginCredential,
    val orderId : String,
)

data class UserDetail(
    val phone : String,
    val name:String,
    val email : String,
    val username : String
)

data class ErrorResponse(val message: String)

data class UpdateUser(
    val loginCredential: LoginCredential,
    val type: String,
    val name: String?,
    val phone: String?,
    val email: String?,
    val otp : String?=null,

    @Json(name="session_id")
    val sessionId: String?=null
)
