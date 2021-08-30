package app.shopgeo.`in`.user.my_cart

import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R
import app.shopgeo.`in`.databinding.MyCartFragmentBinding
import app.shopgeo.`in`.user.toInr
import com.google.android.material.bottomsheet.BottomSheetBehavior
import timber.log.Timber

class MyCart : Fragment() {

    private lateinit var viewModel: MyCartViewModel
    private lateinit var viewModelFactory: MyCartViewModelFactory
    private  lateinit var binding: MyCartFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<MyCartFragmentBinding>(inflater,
            R.layout.my_cart_fragment,container,false)

        viewModelFactory = MyCartViewModelFactory(this.requireActivity().application)

        viewModel = ViewModelProvider(this, viewModelFactory).get(MyCartViewModel::class.java)

        val cartAdapter = CartAdapter(Listener ({
            this.findNavController().navigate(
               MyCartDirections.actionMyCartToProductFragment().setProductId(it.product_id))
        },{
            viewModel.deleteFromCart(it.product_id)
        }))

        viewModel.isDoneLoading.observe(viewLifecycleOwner,{
            binding.progressBar3.visibility = if(it) View.GONE else View.VISIBLE
        })
        viewModel.isNetworkError.observe(viewLifecycleOwner,{
            if(it)
                Toast.makeText(activity,"Network Error", Toast.LENGTH_LONG).show()
        })

        if(!viewModel.loginRepository.isLoggedIn){
            binding.info.visibility = View.VISIBLE
            binding.cart.visibility = View.GONE
            binding.infoMsg.text = getString(R.string.check_login)
        }

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.navigationIcon =  resources.getDrawable(R.drawable.ic_baseline_arrow_back_ios_24)

        binding.cartList.adapter = cartAdapter

        viewModel.cartItems.observe(viewLifecycleOwner,{
           if(it.isNullOrEmpty()){
               binding.info.visibility = View.VISIBLE
               binding.cart.visibility = View.GONE
               binding.infoMsg.text = getString(R.string.cart_empty)
           }else{
               binding.cart.visibility = View.VISIBLE
               binding.info.visibility = View.GONE
               cartAdapter.submitList(it)
           }
        })
        binding.login.setOnClickListener {
            this.findNavController().navigate(MyCartDirections.actionMyCartToLoginNow())
        }
        viewModel.cartTotalCost.observe(viewLifecycleOwner,{
            if(it!=0){
                binding.cartTotalCost.text = getString(R.string.cart_total_price,it.toInr())
                binding.checkout.isEnabled =  true
            }
            else binding.checkout.isEnabled =  false
        })

        viewModel.currentSelectedAddress.observe(viewLifecycleOwner,{
            it?.let{
                binding.deliveryAddress.text = getString(R.string.delivery_address,viewModel.formatedAddress(it))
            }
        })
        val bottomSheet = binding.addressBottomSheet
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        val radioGroup = binding.userAddress

        viewModel.userAddress.observe(viewLifecycleOwner,{ list ->
            list?.forEach {
                val radio = RadioButton(this.context)
                radio.text = viewModel.formatedAddress(it)
                if(it.is_default=='1')
                    radio.isChecked = true
                radioGroup.orientation = RadioGroup.VERTICAL
                val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,200)
                radio.layoutParams = lp
                radio.id = it.address_id
                radioGroup.addView(radio)
            }
        })

        
        binding.change.setOnClickListener {
            if(bottomSheetBehavior.state==BottomSheetBehavior.STATE_HIDDEN)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        binding.cart.setOnClickListener {
            if(bottomSheetBehavior.state==BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState==BottomSheetBehavior.STATE_HIDDEN){
                    binding.cart.alpha = 1.0F
                    binding.cart.isClickable = true
                }else {
                    binding.cart.isClickable = false
                    binding.cart.alpha = 0.3F
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
               if (-slideOffset > 0.3F)
                binding.cart.alpha = -slideOffset
                Timber.i("Offset: $slideOffset")
            }
        })
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            Timber.i("Group : $group, checkedId: $checkedId")
            viewModel.onAddressChanged(checkedId)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_bar1,menu)
        menu.findItem(R.id.myCart).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.myCart -> this.findNavController().navigate(MyCartDirections.actionMyCartToSearchFragment())
        }
        return NavigationUI.onNavDestinationSelected(item,requireView().findNavController())
                ||super.onOptionsItemSelected(item)
    }
}