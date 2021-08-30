package app.shopgeo.`in`.user.category

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R
import app.shopgeo.`in`.databinding.ExploreCategoryFragmentBinding
import kotlinx.coroutines.InternalCoroutinesApi
import timber.log.Timber

@InternalCoroutinesApi
class ExploreCategory : Fragment() {

    private lateinit var viewModel: ExploreCategoryViewModel
    private lateinit var viewModelFactory: ExploreCategoryViewModelFactory
    private lateinit var binding: ExploreCategoryFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val application = requireNotNull(this.activity).application
        viewModelFactory = ExploreCategoryViewModelFactory(application)

        viewModel = ViewModelProvider(this,viewModelFactory)
            .get(ExploreCategoryViewModel::class.java)

        binding = DataBindingUtil.inflate(inflater,
            R.layout.explore_category_fragment,container,false)

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.navigationIcon =  resources.getDrawable(R.drawable.ic_baseline_arrow_back_ios_24)
        val adapter = CategoryListAdapter(CategoryListener{
            Timber.i("Category Clicked: $it")
        })
        binding.categoryList.adapter = adapter
        viewModel.categories.observe(viewLifecycleOwner,{
            it?.let{
                adapter.submitList(it)
            }
        })

        viewModel.isNetworkError.observe(viewLifecycleOwner,{
            if(it == true)
                Toast.makeText(activity,"Network Error", Toast.LENGTH_SHORT).show()
        })

        viewModel.isDoneLoading.observe(viewLifecycleOwner,{
            binding.progressBar.visibility = if(it == true) View.GONE else View.VISIBLE
        })
        setHasOptionsMenu(true)
        return binding.root
    }


}
