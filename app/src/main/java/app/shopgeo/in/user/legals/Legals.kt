package app.shopgeo.`in`.user.legals

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.shopgeo.`in`.R

class Legals : Fragment() {

    companion object {
        fun newInstance() = Legals()
    }

    private lateinit var viewModel: LegalsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.legals_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LegalsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}