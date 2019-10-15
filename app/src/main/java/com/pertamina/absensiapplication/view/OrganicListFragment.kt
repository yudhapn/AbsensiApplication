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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.databinding.FragmentOrganicListBinding
import com.pertamina.absensiapplication.model.User
import com.pertamina.absensiapplication.util.ItemClickSupport
import com.pertamina.absensiapplication.view.adapter.PagedListUserAdapter
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class OrganicListFragment(
        private val type: String,
        private val parentFragment: String
) : Fragment(), PagedListUserAdapter.OnClickListener {
    private val userViewModel: UserViewModel by viewModel()
    private lateinit var binding: FragmentOrganicListBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrganicListBinding.inflate(inflater, container, false).apply {
            val adapter = PagedListUserAdapter("", this@OrganicListFragment)
            userList.layoutManager = GridLayoutManager(requireContext(), 2)
            userList.adapter = adapter
            if (type == "organik") {
                userViewModel.getOrganicLeave().observe(viewLifecycleOwner, Observer {
                    amountLeave = it.toString()
                })
                userViewModel.networkStateManageOrganic.observe(viewLifecycleOwner, Observer {
                    adapter.updateNetworkState(it)
                    userRefresh.isRefreshing = it == NetworkState.RUNNING
                    userRefresh.setOnRefreshListener {
                        userViewModel.refreshUsersOrganic()
                        userViewModel.refreshOrganicLeave()
                    }
                })
            } else {
                userViewModel.getTkjpLeave().observe(viewLifecycleOwner, Observer {
                    amountLeave = it.toString()
                })
                userViewModel.networkStateManageTkjp.observe(viewLifecycleOwner, Observer {
                    adapter.updateNetworkState(it)
                    userRefresh.isRefreshing = it == NetworkState.RUNNING
                    userRefresh.setOnRefreshListener {
                        userViewModel.refreshUsersTkjp()
                        userViewModel.refreshTkjpLeave()
                    }
                })
            }
            userViewModel.getUsersPagedList(type, "")?.observe(viewLifecycleOwner, Observer {
                adapter.submitList(it)
                if (parentFragment == "manageFragment") {
                    adapter.setParentFragment(parentFragment)
                    ItemClickSupport.addTo(userList)
                            .setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                                override fun onItemClicked(recyclerView: RecyclerView, position: Int, view: View) {
                                    val action =
                                            it[position]?.let { it1 -> ManageFragmentDirections.actionShowUserDetail(it1) }
                                    action?.let { it1 -> findNavController().navigate(it1) }
                                }
                            })
                }
            })

            actionClose.setOnClickListener {
                amountStatus.visibility = View.GONE
            }
        }
        return binding.root
    }

    override fun whenListIsUpdated(size: Int, networkState: NetworkState?) {}
}