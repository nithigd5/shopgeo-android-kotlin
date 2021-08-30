package app.shopgeo.`in`.user.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.shopgeo.`in`.R
import app.shopgeo.`in`.user.database.Banners
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import timber.log.Timber

private const val SLIDER_ITEM = 0
private const val CATEGORY_ITEM = 1
private const val CATEGORY_LIST = 2

class HomeAdapter(private val viewModel: HomeViewModel) : ListAdapter<DataItem, RecyclerView.ViewHolder>(
    ViewDiffCallback()) {
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Category -> CATEGORY_ITEM
            is DataItem.Slider -> SLIDER_ITEM
            is DataItem.CategoryList -> CATEGORY_LIST
            else -> throw ClassCastException("Unknown ViewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryViewHolder -> {
                val category = getItem(position) as DataItem.Category
                holder.bind(category,viewModel )
            }
            is SliderViewHolder ->{
                val sliderImages = getItem(position) as DataItem.Slider
                holder.bind(sliderImages)
            }
            is CategoryListViewHolder ->{
                val items = getItem(position) as DataItem.CategoryList
                holder.bind(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CATEGORY_ITEM -> CategoryViewHolder.from(parent)
            SLIDER_ITEM -> SliderViewHolder.from(parent)
            CATEGORY_LIST -> CategoryListViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    class SliderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val imageSlider = view.findViewById<ImageSlider>(R.id.image_slider)

        fun bind(sliderImages : DataItem.Slider) {

            imageSlider.setImageList(sliderImages.sliderImages,ScaleTypes.FIT)

            imageSlider.setItemClickListener(object: ItemClickListener {
                override fun onItemSelected(position: Int) {
                    Timber.i("Banner Position: %d",position)
                }
            })
        }
        companion object {
            fun from(parent: ViewGroup): SliderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.slider_images, parent, false)
                return SliderViewHolder(view)
            }
        }
    }
    class CategoryListViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val categoryList = view.findViewById<RecyclerView>(R.id.category_list)

        fun bind(sliderImages : DataItem.CategoryList) {
            val adapter = CategoryListAdapter(CategoryListener{
                Timber.i("Category Clicker: $it")
            })
            adapter.submitList(sliderImages.categories)
            Timber.i("Category list: ${sliderImages.categories}")
            categoryList.adapter = adapter

        }
        companion object {
            fun from(parent: ViewGroup): CategoryListViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.categories_list, parent, false)
                return CategoryListViewHolder(view)
            }
        }
    }
    class CategoryViewHolder private constructor(view:View) : RecyclerView.ViewHolder(view){
        val products: RecyclerView = view.findViewById(R.id.products)
//        val title: TextView = view.findViewById(R.id.category_title)
        private val adapterScope = CoroutineScope(Dispatchers.IO)

        fun bind(category : DataItem.Category, viewModel : HomeViewModel) {

            adapterScope.launch{
                val productList = viewModel.products(category.item)
                withContext(Dispatchers.Main) {
                    if(productList.isNotEmpty()){
                        val adapter = ProductsAdapter(
                            ProductListener ({ productId ->
                                viewModel.onProductClicked(productId)
                            },{
                                viewModel.updateWishlist(it)
                            }),viewModel.wishList.value)
                        products.adapter = adapter
//                        title.text = category.item
                        adapter.submitList(productList)
                    }
                }
            }
        }
        companion object {
            fun from(parent: ViewGroup): CategoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.products, parent, false)
                return CategoryViewHolder(view)
            }
        }
    }
}


class ViewDiffCallback : DiffUtil.ItemCallback<DataItem>() {

    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

sealed class DataItem{
    abstract var id : Int
    data class Category(val item : String): DataItem(){
         override var id = 0
    }
    data class Slider(val sliderImages : List<SlideModel>) : DataItem(){
        override var id = 1
    }
    data class CategoryList(val categories : List<Banners>) : DataItem(){
        override var id = 2
    }
}