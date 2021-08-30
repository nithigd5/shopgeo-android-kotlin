package app.shopgeo.`in`.user.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.shopgeo.`in`.user.database.Banners
import app.shopgeo.`in`.user.database.DatabaseProduct
import app.shopgeo.`in`.databinding.CategoryImageBinding
import app.shopgeo.`in`.databinding.ProductItemBinding
import app.shopgeo.`in`.user.network.Wishlist

class ProductsAdapter (private val clickListener: ProductListener, private val wishList: List<Wishlist>?) : ListAdapter<DatabaseProduct, ProductsAdapter.ProductViewHolder>(
    ProductDiffCallback()) {

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val productItem = getItem(position)
            holder.bind(productItem, clickListener,wishList)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
               return ProductViewHolder.from(parent)
        }

        class ProductViewHolder private constructor(private val binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root){

            fun bind(item: DatabaseProduct, clickListener: ProductListener, wishList: List<Wishlist>?) {
                binding.product = item
                binding.clickListener = clickListener
                wishList?.forEach {
                    if(it.product_id == item.id) {
                        binding.wishList = true
                        return@forEach
                    }
                }
                binding.executePendingBindings()
            }
            companion object {
                fun from(parent: ViewGroup): ProductViewHolder {
                    val layoutInflater = LayoutInflater.from(parent.context)
                    val binding = ProductItemBinding.inflate(layoutInflater, parent, false)
                    return ProductViewHolder(binding)
                }
            }
        }
}

class ProductDiffCallback : DiffUtil.ItemCallback<DatabaseProduct>() {

    override fun areItemsTheSame(oldItem: DatabaseProduct, newItem: DatabaseProduct): Boolean {
        return oldItem.id == newItem.id
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DatabaseProduct, newItem: DatabaseProduct): Boolean {
        return oldItem == newItem
    }
}
class ProductListener(val clickListener: (productId: Int) -> Unit,val wishListener: (productId: Int) -> Unit) {
    fun onClick(product: DatabaseProduct) = clickListener(product.id)

    fun onWish(product: DatabaseProduct) = clickListener(product.id)

}

class CategoryListAdapter (private val listener: CategoryListener) : ListAdapter<Banners, CategoryListAdapter.CategoryViewHolder>(
    CategoryDiffCallback()) {

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item,listener)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder.from(parent)
    }

    class CategoryViewHolder private constructor(private val binding: CategoryImageBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Banners, clickListener: CategoryListener) {
            binding.banner = item
            binding.listener = clickListener
        }
        companion object {
            fun from(parent: ViewGroup): CategoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CategoryImageBinding.inflate(layoutInflater, parent, false)
                return CategoryViewHolder(binding)
            }
        }
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<Banners>() {

    override fun areItemsTheSame(oldItem: Banners, newItem: Banners): Boolean {
        return oldItem.id == newItem.id
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Banners, newItem: Banners): Boolean {
        return oldItem == newItem
    }
}
class CategoryListener(val clickListener: (productId: String) -> Unit) {
    fun onClick(product: Banners) = clickListener(product.image_type)
}