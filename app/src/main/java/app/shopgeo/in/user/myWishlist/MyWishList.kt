package app.shopgeo.`in`.user.myWishlist

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R
import app.shopgeo.`in`.databinding.MyWishListFragmentBinding
import timber.log.Timber

class MyWishList : Fragment() {

    private lateinit var viewModel: MyWishListViewModel
    private  lateinit var binding: MyWishListFragmentBinding
    private lateinit var viewModelFactory: MyWishlistViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.my_wish_list_fragment,container,false)

        val application = requireNotNull(this.activity).application

        viewModelFactory = MyWishlistViewModelFactory(application)

        viewModel = ViewModelProvider(this,viewModelFactory).get(MyWishListViewModel::class.java)

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.navigationIcon =  resources.getDrawable(R.drawable.ic_baseline_arrow_back_ios_24)

        val adapter = MyWishAdapter(Listener ({
            findNavController().navigate(MyWishListDirections.actionMyWishListToProductFragment()
                .setProductId(it.product_id))
        },{
            viewModel.updateWishlist(it.product_id)
        },{
            viewModel.addToCart(it.product_id)
        }))
        binding.wishList.adapter = adapter

        viewModel.wishList.observe(viewLifecycleOwner,{
            it?.let{
                adapter.submitList(it)
                Timber.i("Wish List: $it")
            }
        })

        viewModel.isDoneLoading.observe(viewLifecycleOwner,{
            binding.progressBar3.visibility = if(it) View.GONE else View.VISIBLE
        })

        viewModel.isCartUpdated.observe(viewLifecycleOwner,{
            Toast.makeText(activity,"Added to Cart", Toast.LENGTH_LONG).show()
        })

        viewModel.isNetworkError.observe(viewLifecycleOwner,{
            if(it)
                Toast.makeText(activity,"Network Error", Toast.LENGTH_LONG).show()
        })

        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(true)

        return binding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_bar1,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.myCart -> this.findNavController().navigate(MyWishListDirections.actionMyWishListToMyCart())
        }
        return NavigationUI.onNavDestinationSelected(item,requireView().findNavController())
                ||super.onOptionsItemSelected(item)
    }

}
