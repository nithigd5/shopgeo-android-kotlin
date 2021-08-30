package app.shopgeo.`in`.user.myOrders

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R
import app.shopgeo.`in`.databinding.MyOrdersFragmentBinding

class MyOrders : Fragment() {

    private lateinit var viewModel: MyOrdersViewModel
    private lateinit var viewModelFactory: MyOrdersViewModelFactory
    private  lateinit var binding: MyOrdersFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.my_orders_fragment,container,false)

        viewModelFactory = MyOrdersViewModelFactory(requireNotNull(this.activity).application)

        viewModel = ViewModelProvider(this,viewModelFactory).get(MyOrdersViewModel::class.java)

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.navigationIcon =  resources.getDrawable(R.drawable.ic_baseline_arrow_back_ios_24)

        val cartAdapter = MyOrdersAdapter(Listener {
            this.findNavController().navigate(MyOrdersDirections.actionMyOrdersToOrderDetail(it.orderId))
        })
        binding.ordersList.adapter = cartAdapter

        viewModel.orders.observe(viewLifecycleOwner,{
            it?.let{
                cartAdapter.submitList(it)
            }
        })

        viewModel.isDoneLoading.observe(viewLifecycleOwner,{
            binding.progressBar3.visibility = if(it) View.GONE else View.VISIBLE
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
//        when(item.itemId){
//            R.id.share -> startActivity(getShareIntent())
//        }
        return NavigationUI.onNavDestinationSelected(item,requireView().findNavController())
                ||super.onOptionsItemSelected(item)
    }

}