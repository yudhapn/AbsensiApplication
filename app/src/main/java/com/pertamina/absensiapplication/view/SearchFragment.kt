package com.pertamina.absensiapplication.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.databinding.FragmentSearchBinding
import com.pertamina.absensiapplication.util.ItemClickSupport
import com.pertamina.absensiapplication.view.adapter.PagedListUserAdapter
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchFragment : Fragment(), PagedListUserAdapter.OnClickListener {
    private val userViewModel: UserViewModel by sharedViewModel()
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false).apply {
            val adapter = PagedListUserAdapter("manageFragment", this@SearchFragment)
            rvSearch.layoutManager = GridLayoutManager(requireContext(), 2)
            rvSearch.adapter = adapter
            userViewModel.getQuerySearch().observe(viewLifecycleOwner, Observer { query ->
                userViewModel.getSearchResult(query).observe(viewLifecycleOwner, Observer {
                    adapter.submitList(it)
                    ItemClickSupport.addTo(rvSearch)
                        .setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                            override fun onItemClicked(recyclerView: RecyclerView, position: Int, view: View) {
                                val action =
                                    it[position]?.let { it1 -> SearchFragmentDirections.actionShowUserDetail(it1) }
                                action?.let { it1 -> findNavController().navigate(it1) }
                            }
                        })
                })
            })
        }
        return binding.root
    }

    override fun whenListIsUpdated(size: Int, networkState: NetworkState?) {}
}
