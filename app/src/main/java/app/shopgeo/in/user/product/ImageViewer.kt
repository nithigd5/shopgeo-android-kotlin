package app.shopgeo.`in`.user.product

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R
import app.shopgeo.`in`.user.TouchImageView
import app.shopgeo.`in`.databinding.ImageViewerFragmentBinding
import app.shopgeo.`in`.user.setImageUrl
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

private lateinit var viewModel: ImageViewerViewModel

class ImageViewer : Fragment() {

    private lateinit var binding : ImageViewerFragmentBinding

    private lateinit var demoCollectionAdapter: DemoCollectionAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<ImageViewerFragmentBinding>(
            inflater,
            R.layout.image_viewer_fragment, container, false
        )
        (activity as MainActivity).setSupportActionBar(binding.toolbar as MaterialToolbar)
        (activity as MainActivity).title=""
        binding.toolbar.navigationIcon =
            resources.getDrawable(R.drawable.ic_baseline_cancel_24)

        viewModel = ViewModelProvider(this).get(ImageViewerViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val images = ImageViewerArgs.fromBundle(requireArguments()).images
        Timber.i("Images: $images")
        images.images?.let{
            demoCollectionAdapter = DemoCollectionAdapter(this,images.images)
            viewPager = binding.imageViewer
            viewPager.adapter = demoCollectionAdapter
            viewPager.currentItem = images.curIndex

            viewModel.isOnZoom.observe(viewLifecycleOwner,{
                it?.let{
                    Timber.i("userInputEnabled : ${viewPager.isUserInputEnabled}")
                    viewPager.isUserInputEnabled = true
                }
            })

            val tabLayout = binding.tabImages

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.setCustomView(R.layout.image_view)

                tab.customView?.findViewById<ImageView>(R.id.tab)?.setImageUrl(it[position])
            }.attach()
        }
    }
}
data class ViewerImages(
    val images: List<String>?,
    val curIndex : Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(images)
        parcel.writeInt(curIndex)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ViewerImages> {
        override fun createFromParcel(parcel: Parcel): ViewerImages {
            return ViewerImages(parcel)
        }

        override fun newArray(size: Int): Array<ViewerImages?> {
            return arrayOfNulls(size)
        }
    }

}

class DemoCollectionAdapter(fragment: Fragment, private val images : List<String>) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = images.size

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment = DemoObjectFragment()
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putString(ARG_OBJECT, images[position])
        }
        return fragment
    }
}
private const val ARG_OBJECT = "object"

// Instances of this class are fragments representing a single
// object in our collection.
class DemoObjectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.zoom_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            val image: TouchImageView = view.findViewById(R.id.images_zoom)
            image.setImageUrl(getString(ARG_OBJECT))
            image.isOnZoom.observe(viewLifecycleOwner,{
                it?.let{
                    Timber.i("isOnZoom: $it")
                    viewModel.isOnZoom.value = it
                }
            })
        }
    }

}