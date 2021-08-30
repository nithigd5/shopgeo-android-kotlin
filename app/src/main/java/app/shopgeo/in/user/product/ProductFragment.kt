package app.shopgeo.`in`.user.product

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R
import app.shopgeo.`in`.user.database.ProductDatabase
import app.shopgeo.`in`.databinding.FragmentProductBinding
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.InternalCoroutinesApi
import timber.log.Timber

class ProductFragment : Fragment() {

    private lateinit var viewModel: ProductViewModel
    private lateinit var viewModelFactory: ProductViewModelFactory
    private lateinit var binding : FragmentProductBinding

    @InternalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val application = requireNotNull(this.activity).application
        val database = ProductDatabase.getInstance(application)

        val productId = ProductFragmentArgs.fromBundle(requireArguments()).productId

        viewModelFactory = ProductViewModelFactory(productId,database,application)
        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(ProductViewModel::class.java)

        binding = DataBindingUtil.inflate<FragmentProductBinding>(inflater,
                R.layout.fragment_product,container,false)


        viewModel.images.observe(viewLifecycleOwner,{
            if (it != null) {
                binding.productImages.setImageList(it,ScaleTypes.CENTER_INSIDE)
                binding.productImages.setItemClickListener(object : ItemClickListener{
                    override fun onItemSelected(position: Int) {
                        Timber.i("Position $position")
                        findNavController().navigate(ProductFragmentDirections.actionProductFragmentToImageViewer2(
                            ViewerImages(
                                images = viewModel.product.value?.Images,
                                curIndex = position
                            )
                        ))
                    }

                })
            }
        })

        (activity as MainActivity).setSupportActionBar(binding.toolbar as MaterialToolbar)
        (activity as MainActivity).title=""
        binding.toolbar.navigationIcon =  resources.getDrawable(R.drawable.ic_baseline_arrow_back_ios_24)

//        viewModel.regionList.observe(viewLifecycleOwner, object: Observer<List<String>> {
//            override fun onChanged(data: List<String>?) {
//                data ?: return
//
//                val chipGroup = binding.regionList
//                val inflator = LayoutInflater.from(chipGroup.context)
//
//                val children = data.map { regionName ->
//                    val chip = inflator.inflate(R.layout.region, chipGroup, false) as Chip
//                    chip.text = regionName
//                    chip.tag = regionName
//                    chip.setOnCheckedChangeListener { button, isChecked ->
//                        viewModel.onFilterChanged(button.tag as String, isChecked)
//                    }
//                    chip
//                }
//                chipGroup.removeAllViews()
//
//                for (chip in children) {
//                    chipGroup.addView(chip)
//                }
//            }
//        })
//
        binding.viewModel = viewModel
        viewModel.isDoneLoading.observe(viewLifecycleOwner,{
            binding.progressBar.visibility = if(it) View.GONE else View.VISIBLE
        })

        viewModel.setWishListUpdate.observe(viewLifecycleOwner,{
            Toast.makeText(activity,"WishList Updated ",Toast.LENGTH_SHORT).show()
        })

        viewModel.isNetworkError.observe(viewLifecycleOwner,{
            if(it)
            Toast.makeText(activity,"Network Error Retrying : ${viewModel.fetchAttempt}",Toast.LENGTH_SHORT).show()
        })

        viewModel.isProductAdded.observe(viewLifecycleOwner,{
            if(it == true) {
                Toast.makeText(activity, "Product added to Cart", Toast.LENGTH_SHORT).show()
                viewModel.isProductAdded.value = false
            }
        })
        binding.addToCart.setOnClickListener {
            if(viewModel.isloggedIn)
                viewModel.addToCart()
            else
                Toast.makeText(activity,"Please login to add product to cart.",Toast.LENGTH_LONG).show()
        }
        binding.favourite.setOnClickListener {
            viewModel.updateWishlist(productId)
        }

        viewModel.wishList.observe(viewLifecycleOwner,{
            binding.favourite.isChecked = it
        })
        viewModel.product.observe(viewLifecycleOwner,{
            addProductDetails()
        })

        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun addProductDetails(){
        val details = viewModel.product.value?.Details
        if((details == null)||details.isEmpty()){
            binding.productDetails.visibility = View.GONE
        }else{
            binding.productDetails.visibility = View.VISIBLE
        }
        Timber.i("Product Details: $details")
        details?.forEach { detail ->
            Timber.i("Key : ${detail.name}")
            Timber.i("Value : ${detail.value}")

            val row = TableRow(this.requireContext())
            row.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            row.setPadding(8)

            val key = TextView(this.requireContext())
            key.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
            key.gravity = Gravity.START
            key.text = detail.name

            row.addView(key)

            val value = TextView(this.requireContext(),null,0,R.style.Base_TextAppearance_MaterialComponents_Subtitle2)
            value.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)

            value.gravity = Gravity.START
            value.text = detail.value


            row.addView(value)

            binding.productDetails.addView(row)
        }
        fun setSellerDetails(){
            viewModel.product.value?.Manufacturer
        }
    }

    private fun getShareIntent() : Intent {
        val shareIntent  = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT,
                "https://shopgeo.in/mp/${viewModel.product.value?.Title}/${viewModel.product.value?.id}")
        return shareIntent
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_bar,menu)
        if(this.getShareIntent().resolveActivity(requireActivity().packageManager)==null){
            menu.findItem(R.id.share).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.share -> startActivity(getShareIntent())
            R.id.myCart -> this.findNavController().navigate(ProductFragmentDirections.actionProductFragmentToMyCart())
        }
        return NavigationUI.onNavDestinationSelected(item,requireView().findNavController())
                ||super.onOptionsItemSelected(item)
    }
}