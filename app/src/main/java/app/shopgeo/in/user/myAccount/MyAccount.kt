package app.shopgeo.`in`.user.myAccount

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R
import app.shopgeo.`in`.databinding.MyAccountFragmentBinding
import timber.log.Timber

class MyAccount : Fragment() {

    private lateinit var viewModel: MyAccountViewModel
    private lateinit var viewModelFactory : MyAccountViewModelFactory
    private  lateinit var binding: MyAccountFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.my_account_fragment,container,false)
        viewModelFactory = MyAccountViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this,viewModelFactory).get(MyAccountViewModel::class.java)

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.navigationIcon =  resources.getDrawable(R.drawable.ic_baseline_arrow_back_ios_24)

        viewModel.currentSelectedAddress.observe(viewLifecycleOwner,{
            it?.let{
                binding.address.text = viewModel.formattedAddress(it)
            }
        })
        viewModel.user.observe(viewLifecycleOwner,{
            it?.let{
                binding.user = it
            }
        })
        viewModel.isDoneLoading.observe(viewLifecycleOwner,{
            binding.progressBar.visibility = if(it) View.GONE else View.VISIBLE
        })

        viewModel.isNetworkError.observe(viewLifecycleOwner,{
            if(it)
                Toast.makeText(activity,"Network Error", Toast.LENGTH_LONG).show()
        })
        binding.myCart.setOnClickListener {
            this.findNavController().navigate(
                MyAccountDirections.actionMyAccountToMyCart()
            )
        }

        binding.editPhone.setOnClickListener{
            val dialogFragment = AlertDialog(viewModel,"phone_dialog")
            dialogFragment.show(
                requireFragmentManager(),
                "phone_dialog"
            )
        }
        binding.editName.setOnClickListener {
            val dialogFragment = AlertDialog(viewModel,"name_dialog")
            dialogFragment.show(
                requireFragmentManager(),
                "name_dialog"
            )
        }
        binding.editEmail.setOnClickListener {
            val dialogFragment = AlertDialog(viewModel,"email_dialog")
            dialogFragment.show(
                requireFragmentManager(),
                "email_dialog"
            )
        }
        binding.myOrders.setOnClickListener {
            this.findNavController().navigate(
                MyAccountDirections.actionMyAccountToMyOrders()
            )
        }
        binding.logout.setOnClickListener {
            viewModel.logout()
        }
        viewModel.isLoggedOut.observe(viewLifecycleOwner,{
            if(it==true){
                this.findNavController().navigate(MyAccountDirections.actionMyAccountToHome2())
                ActivityCompat.recreate(requireActivity())
            }
        })
        viewModel.openEmailDialog.observe(viewLifecycleOwner,{
            if(it==true){
                openOTPDialog()
            }
        })
        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(true)

        return binding.root
    }

    fun openOTPDialog(){
        val dialogFragment = AlertDialog(viewModel,"otp_dialog")
        dialogFragment.show(
            requireFragmentManager(),
            "otp_dialog"
        )
    }
    override fun onDestroy() {
        Timber.i("Destroyed")
        super.onDestroy()
    }

    override fun onResume() {
        Timber.i("resumed")
        super.onResume()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_bar1,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.myCart -> this.findNavController().navigate(MyAccountDirections.actionMyAccountToMyCart())
        }
        return NavigationUI.onNavDestinationSelected(item,requireView().findNavController())
                ||super.onOptionsItemSelected(item)
    }
}