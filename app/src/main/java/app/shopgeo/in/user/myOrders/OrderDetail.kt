package app.shopgeo.`in`.user.myOrders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R
import app.shopgeo.`in`.databinding.OrderDetailFragmentBinding

class OrderDetail : Fragment() {

    private lateinit var viewModel: OrderDetailViewModel
    private lateinit var viewModelFactory: OrderDetailViewModelFactory
    private  lateinit var binding: OrderDetailFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.order_detail_fragment,container,false)

        viewModelFactory = OrderDetailViewModelFactory(OrderDetailArgs.fromBundle(requireArguments()).orderId,requireNotNull(this.activity).application)

        viewModel = ViewModelProvider(this,viewModelFactory).get(OrderDetailViewModel::class.java)

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.navigationIcon =  resources.getDrawable(R.drawable.ic_baseline_arrow_back_ios_24)


        viewModel.isDoneLoading.observe(viewLifecycleOwner,{
            binding.progressBar3.visibility = if(it) View.GONE else View.VISIBLE
        })

        viewModel.isNetworkError.observe(viewLifecycleOwner,{
            if(it)
                Toast.makeText(activity,"Network Error", Toast.LENGTH_LONG).show()
        })


        viewModel.order.observe(viewLifecycleOwner,{
            it?.let {
                binding.order = it
                if(viewModel.order.value?.orderStatus=="OnDelivery"){
                    Order.setDeliveryStatus(binding, it.deliveryStatus)
                    binding.trackYourOrder.visibility = View.VISIBLE
                }else{
                    binding.trackYourOrder.visibility = View.GONE
                }
            }
        })
        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(true)

        return binding.root
    }


}