package app.shopgeo.`in`.user.category

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R
import app.shopgeo.`in`.databinding.SubCategoryFragmentBinding

class SubCategory : Fragment() {

    private lateinit var viewModel: SubCategoryViewModel
    private lateinit var binding: SubCategoryFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel = ViewModelProvider(this).get(SubCategoryViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater,
            R.layout.sub_category_fragment,container,false)

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.navigationIcon =  resources.getDrawable(R.drawable.ic_baseline_arrow_back_ios_24)

        return binding.root
    }
}