package app.shopgeo.`in`.user

import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import app.shopgeo.`in`.R
import app.shopgeo.`in`.user.database.DatabaseProduct
import app.shopgeo.`in`.user.network.Cart
import app.shopgeo.`in`.user.network.Orders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt


@BindingAdapter("Title")
fun TextView.setProductTitle(item: String?) {
    item?.let {
        text = item.split(" ").joinToString(" ") {
            it.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
        }
        Timber.i("Text: $text")
    }
}

@BindingAdapter("Description")
fun TextView.setProductDescription(item: String?) {
    item?.let {
        text = item.split(" ").joinToString(" ") {
            it.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
        }
    }
}

@BindingAdapter("rupee")
fun TextView.setProductPrice(item: DatabaseProduct?) {

    item?.let {
        text = item.Price.toInr()
    }
}

@BindingAdapter("rupee")
fun TextView.setProductPrice(item: Int?) {

    item?.let {
        text = item.toInr()
    }
}

@BindingAdapter("prod_rating")
fun TextView.setProductRating(rating: Double?) {
    rating?.let {
        text = if(rating>0)
            resources.getString(R.string.rating,rating)
        else "+"
    }
}
@BindingAdapter("cart_item_price")
fun TextView.setCartItemPrice(cart: Cart?) {
    cart?.let {
        text = resources.getString(R.string.cart_price,it.price.toInr())
    }
}
@BindingAdapter("cart_total_price")
fun TextView.setCartTotalPrice(cart: Cart?) {
    cart?.let {
        text = resources.getString(R.string.cart_total_price,(it.price*it.quantity).toInr())
    }
}

@BindingAdapter("order_total_price")
fun TextView.setOrderTotalPrice(order: Orders?) {
    order?.let {
        text = resources.getString(R.string.cart_total_price,(it.price*it.quantity).toInr())
    }
}

@BindingAdapter("mrp")
fun TextView.setMRP(item: DatabaseProduct?) {
    item?.let {
        text = item.MRP.toInr()
        paintFlags = STRIKE_THRU_TEXT_FLAG
    }
}

@BindingAdapter("discount")
fun TextView.setProductDiscount(item: DatabaseProduct?) {
    item?.let {
        val discount = (100-(item.Price / (item.MRP) * 100)).roundToInt()
        text = resources.getString(R.string.prod_discount,discount)
    }
}

@BindingAdapter("ImageUrl")
fun ImageView.setImageUrl( ImageUrl: String?) {
    ImageUrl?.let{
        val imgUri = ImageUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(this.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image))
            .into(this)
    }
}

@BindingAdapter("NetworkStatus")
fun bindStatus(statusImageView: ImageView,
               status: Boolean?) {
    when (status) {
        true -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
        }
        false -> {
            statusImageView.visibility = View.GONE
        }
        else -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
        }
    }
}
@BindingAdapter("isFavourite")
fun CheckBox.setFavourite(wishlist: Boolean?){
    wishlist?.let{
        this.isChecked = wishlist==true
    }
}

fun Int.toInr(): String {
    val indiaLocale = Locale("en","IN")
    val inr : NumberFormat = NumberFormat.getCurrencyInstance(indiaLocale)
    return inr.format(this)
}
fun Double.toInr(): String {
    val indiaLocale = Locale("en","IN")
    val inr : NumberFormat = NumberFormat.getCurrencyInstance(indiaLocale)
    return inr.format(this.toInt())
}