package app.shopgeo.`in`.user.category

import android.annotation.SuppressLint
import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.ColumnInfo
import app.shopgeo.`in`.user.database.Banners
import app.shopgeo.`in`.databinding.CategoryItemBinding
import app.shopgeo.`in`.user.network.NetworkApi
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.executeWithRetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber

@InternalCoroutinesApi
class ExploreCategoryViewModel(application: Application) : AndroidViewModel(application) {

    val categories = MutableLiveData<List<Banners>?>()

    val isDoneLoading = MutableLiveData<Boolean?>()
    var isNetworkError = MutableLiveData<Boolean?>()

    init{
        getAllCategories()
    }
    private fun getAllCategories(){
        isDoneLoading.value = false
        viewModelScope.launch{
            with(Dispatchers.IO){
                val res = executeWithRetry(times = 5) {
                    NetworkApi.RETROFIT_SERVICES.getCategories()
                }
                with(Dispatchers.Main){
                    when(res){
                        is NetworkResponse.Success<List<Banners>> -> {
                            Timber.i("Success: ${res.body}")
                            categories.value = res.body
                        }
                        is NetworkResponse.Error -> {
                            isNetworkError.value = false
                        }
                    }
                    isDoneLoading.value  = true
                }

            }
        }
    }
}

data class CategoryItem(
    @ColumnInfo(name="Category")
    val category: String,
    @ColumnInfo(name="Images")
    val image : List<String>
)

class CategoryListAdapter (private val listener: CategoryListener) : ListAdapter<Banners, CategoryListAdapter.CategoryViewHolder>(
    CategoryDiffCallback()) {

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item,listener)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder.from(parent)
    }

    class CategoryViewHolder private constructor(private val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Banners, clickListener: CategoryListener) {
            binding.category = item
            binding.listener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): CategoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CategoryItemBinding.inflate(layoutInflater, parent, false)
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
class CategoryListener(val clickListener: (productId: String?) -> Unit) {
    fun onClick(product: Banners) = clickListener(product.extra)
}

@InternalCoroutinesApi
@Suppress("UNCHECKED_CAST")
class ExploreCategoryViewModelFactory(
    private val application: Application
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExploreCategoryViewModel::class.java)) {
            return ExploreCategoryViewModel(application ) as T        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}