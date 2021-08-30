package app.shopgeo.`in`.user.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.shopgeo.`in`.user.database.DatabaseProduct
import app.shopgeo.`in`.databinding.SearchResultBinding
import app.shopgeo.`in`.user.network.NetworkApi
import app.shopgeo.`in`.user.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel : ViewModel() {
    val products = MutableLiveData<List<DatabaseProduct>>()

    val networkError = MutableLiveData<Boolean?>()

    val isDoneLoading = MutableLiveData<Boolean?>()
    init{
        isDoneLoading.value = true
    }
    fun get_search(keywords : String){
        isDoneLoading.value = false
        try{
            viewModelScope.launch {
                withContext(Dispatchers.IO){
                    val searchResult = NetworkApi.RETROFIT_SERVICES.search(keywords)
                    withContext(Dispatchers.Main){
                        products.value = searchResult.asDatabaseModel()
                        isDoneLoading.value = true
                        networkError.value = false
                    }
                }
            }
        }catch(e : Throwable){
            networkError.value = true
            isDoneLoading.value = true
        }

    }
}

class SearchAdapter (private val clickListener: Listener) : ListAdapter<DatabaseProduct, SearchAdapter.SearchViewHolder>(
    ProductDiffCallback()) {

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val cart = getItem(position)
        holder.bind(cart, clickListener)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder.from(parent)
    }

    class SearchViewHolder private constructor(private val binding: SearchResultBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: DatabaseProduct, clickListener: Listener) {
            binding.listener = clickListener
            binding.product = item
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): SearchViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SearchResultBinding.inflate(layoutInflater, parent, false)
                return SearchViewHolder(binding)
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
class Listener(val clickListener: (item: DatabaseProduct) -> Unit) {
    fun onClick(item: DatabaseProduct) = clickListener(item)
}
