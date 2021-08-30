package app.shopgeo.`in`.user.search

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R
import app.shopgeo.`in`.databinding.SearchFragmentBinding


class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel
    private  lateinit var binding: SearchFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.search_fragment,container,false)

        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        val adapter = SearchAdapter(Listener {
            this.findNavController().navigate(
                SearchFragmentDirections.actionSearchFragmentToProductFragment()
                    .setProductId(it.id)
            )
        })

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.navigationIcon =  resources.getDrawable(R.drawable.ic_baseline_arrow_back_ios_24)
        binding.toolbar.title = ""
        binding.searchResult.adapter = adapter
        viewModel.products.observe(viewLifecycleOwner,{
            it?.let{
                if(it.isNotEmpty()){

                    binding.searchError.visibility = View.GONE
                }
                adapter.submitList(it)
            }
        })


        viewModel.isDoneLoading.observe(viewLifecycleOwner,{
            if(it==true) binding.progressBar.visibility = View.GONE else binding.progressBar.visibility = View.VISIBLE
        })
        viewModel.networkError.observe(viewLifecycleOwner, { isNetworkError ->
            if (isNetworkError == true) onNetworkError()
        })


//        binding.searchInputLayout.setEndIconOnClickListener {
//            binding.searchInputLayout.visibility = View.GONE
////            binding.searchBtn.visibility = View.VISIBLE
//            binding.toolbar.menu.findItem(R.id.searchFragment).isVisible = true
//
//            binding.toolbar.menu.findItem(R.id.searchFragment).isVisible = true
//        }

        binding.searchInput.showSoftInputOnFocus = true

        fun hideKeyboard(context : Context, view : View) {
            val imm : InputMethodManager =
                context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager;
               imm.hideSoftInputFromWindow(view.windowToken, 0);
        }

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    binding.searchInputLayout.visibility= View.GONE
//                    binding.searchBtn.visibility = View.VISIBLE
                    binding.toolbar.menu.findItem(R.id.searchFragment).isVisible = true

                    hideKeyboard(requireContext(),requireView())

                    val s = binding.searchInput.text.toString()

                    if(s.length>=3){
                        binding.toolbar.title = s.toString()
                    }else{
                        Toast.makeText(activity,"Please enter atleast 3 letters",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            false
        }
        binding.searchInput.setOnFocusChangeListener { view, b ->
            if(b){
                binding.searchInputLayout.hint = ""
            }
            else binding.searchInputLayout.hint = getString(R.string.search_title)
        }

        binding.searchInput.doOnTextChanged { text, start, before, count ->

            text?.let{
                if(text.toString().length>=3){
                    viewModel.get_search(text.toString())
                }
            }
        }

//        binding.searchBtn.setOnClickListener{
//            binding.searchInputLayout.visibility = View.VISIBLE
//            it.visibility = View.GONE
//        }


        binding.toolbar.setOnClickListener{
            binding.searchInputLayout.visibility= View.VISIBLE
            binding.toolbar.menu.findItem(R.id.searchFragment).isVisible = false
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(binding.searchInput.hasFocus()){

                    binding.searchInputLayout.visibility= View.GONE
                    binding.toolbar.menu.findItem(R.id.searchFragment).isVisible = true

                    hideKeyboard(requireContext(),requireView())

                    val s = binding.searchInput.text.toString()

                    if(s.length>=3){
                        binding.toolbar.title = s.toString()
                    }else{
                        Toast.makeText(activity,"Please enter atleast 3 letters",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })
        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun onNetworkError() {
        Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_bar1,menu)
        menu.findItem(R.id.searchFragment).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.myCart ->{
                findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToMyCart())
            }
            R.id.searchFragment ->{
                binding.toolbar.menu.findItem(R.id.searchFragment).isVisible = false
                binding.searchInputLayout.visibility = View.VISIBLE
            }
        }
        return super.onOptionsItemSelected(item)
    }

}