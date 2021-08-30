package app.shopgeo.`in`.user.myOrders

import android.widget.TextView
import androidx.databinding.BindingAdapter
import app.shopgeo.`in`.R
import app.shopgeo.`in`.user.network.Orders
import app.shopgeo.`in`.user.toInr
import timber.log.Timber


@BindingAdapter("order_price")
fun TextView.setOrderPrice(item: Orders?) {
    item?.let{
        text = (it.price/it.quantity).toInr()
    }
}

@BindingAdapter("order_total")
fun TextView.setOrderTotalPrice(item: Orders?) {
    item?.let{
        text = (it.price).toInr()
    }
}

@BindingAdapter("order_id")
fun TextView.setOrderId(item: String?) {
    item?.let{
        text = resources.getString(R.string.orderId,it)
    }
}
@BindingAdapter("payment_id")
fun TextView.setPaymentd(item: String?) {
    item?.let{
        text = resources.getString(R.string.payId,it)
    }
}
@BindingAdapter("payment_mode")
fun TextView.setPaymentMode(item: String?) {
    item?.let{
        text = resources.getString(R.string.paymode,it)
    }
}
@BindingAdapter("order_date")
fun TextView.setOrderDate(item: Long?) {
    item?.let{
//        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
//
//        val localDateTime = LocalDateTime.from(it)
//
//        val output = formatter.format(localDateTime)
        Timber.i("Date : $it")

//        text = resources.get/String(R.string.orderId,output)
        text = it.toString()
    }
}