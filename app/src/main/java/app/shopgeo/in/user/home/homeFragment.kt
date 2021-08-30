package app.shopgeo.`in`.user.home

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R
import app.shopgeo.`in`.user.database.ProductDatabase
import app.shopgeo.`in`.databinding.FragmentHomeBinding
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber

class Home : Fragment() {

    private lateinit var viewModel: HomeViewModel
    lateinit var binding : FragmentHomeBinding
    private  lateinit var toolbar : MaterialToolbar

    @InternalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_home,container,false)

        val application = requireNotNull(this.activity).application
        val database = ProductDatabase.getInstance(application)
        val viewModelFactory = HomeViewModelFactory(database,application)
        viewModel = ViewModelProvider(this,viewModelFactory).get(HomeViewModel::class.java)

        viewModel.adapter = HomeAdapter(viewModel)

        viewModel.navToProduct.observe(viewLifecycleOwner, { id ->
            id?.let {
                this.findNavController().navigate(
                        HomeDirections.actionHome2ToProductFragment().setProductId(id))
                viewModel.navigatedToProduct()
            }
        })
        toolbar = binding.toolbar

        (activity as MainActivity).setSupportActionBar(toolbar)

        binding.productitem.adapter = viewModel.adapter

        binding.searchBar.setOnClickListener{
            Timber.i("Search bar clicked")
            findNavController().navigate(HomeDirections.actionHome2ToSearchFragment())
        }
        binding.searchBarInput.setOnClickListener{
            Timber.i("Search bar clicked")
            findNavController().navigate(HomeDirections.actionHome2ToSearchFragment())
        }
        viewModel.categories.observe(viewLifecycleOwner,{
            lifecycleScope.launch {
                it?.let{ list ->
                    viewModel.sliderImages.observe(viewLifecycleOwner,{ list1 ->
                        list1.let{  sliderImages->
                            viewModel.categoryList.observe(viewLifecycleOwner,{ list3->
                                list3?.let{ Banners->
                                    val recyclerList = listOf(DataItem.CategoryList(Banners)) + listOf(
                                        DataItem.Slider(sliderImages)) + list.map{
                                        DataItem.Category(it)
                                    }
                                    Timber.i("Category List: $list3")
                                    Timber.i("Home Recycler View List: $recyclerList")
                                    viewModel.wishList.observe(viewLifecycleOwner,{
                                        if(recyclerList.isNotEmpty()){
                                            viewModel.adapter.submitList(recyclerList)
                                        }
                                    })
                                }
                            })
                        }
                    })
                }
            }
        })

        viewModel.setWishListUpdate.observe(viewLifecycleOwner,{
            Toast.makeText(activity,"WishList Updated ",Toast.LENGTH_SHORT).show()
        })

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel


        viewModel.eventNetworkError.observe(viewLifecycleOwner, { isNetworkError ->
            if (isNetworkError == true) onNetworkError()
        })
        viewModel.isDoneLoading.observe(viewLifecycleOwner,{
            binding.progressBar2.visibility = if(it) View.GONE else View.VISIBLE
        })
        var pressedTime : Long = 0

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (pressedTime + 2000 > System.currentTimeMillis()) {

                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "Press back again to exit", Toast.LENGTH_SHORT).show()
//                    isEnabled = false
//                    activity?.onBackPressed()
                }
                pressedTime = System.currentTimeMillis()
            }
        })
        setHasOptionsMenu(true)
        return binding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.cart,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,requireView().findNavController())
                ||super.onOptionsItemSelected(item)
    }
    private fun onNetworkError() {
        if(!viewModel.isNetworkErrorShown.value!!) {
            Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
            viewModel.onNetworkErrorShown()
        }
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        Timber.i( "onAttach called")
//    }
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Timber.i( "onCreate called")
//    }
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        Timber.i( "onViewCreated called")
//    }
//    override fun onStart() {
//        super.onStart()
//        Timber.i( "onStart called")
//    }
//    override fun onResume() {
//        super.onResume()
//        Timber.i( "onResume called")
//    }
//    override fun onPause() {
//        super.onPause()
//        Timber.i( "onPause called")
//    }
//    override fun onStop() {
//        super.onStop()
//        Timber.i( "onStop called")
//    }
//    override fun onDestroyView() {
//        super.onDestroyView()
//        Timber.i( "onDestroyView called")
//    }
//    override fun onDetach() {
//        super.onDetach()
//        Timber.i( "onDetach called")
//    }

}