package com.pertamina.absensiapplication.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.databinding.FragmentDashboardBinding
import com.pertamina.absensiapplication.model.Counter
import com.pertamina.absensiapplication.model.User
import com.pertamina.absensiapplication.util.ItemClickSupport
import com.pertamina.absensiapplication.view.adapter.PagedListPermitAdapter
import com.pertamina.absensiapplication.viewmodel.PermitViewModel
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment(), PagedListPermitAdapter.OnClickListener {
    private val permitViewModel: PermitViewModel by viewModel()
    private val userViewModel: UserViewModel by viewModel()
    private lateinit var binding: FragmentDashboardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false).apply {
            val myFormat = "MMMM dd, yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
            val currentDate = Calendar.getInstance()
            valueDateText.text = sdf.format(currentDate.time)
            user = User()
            userViewModel.getUser().observe(viewLifecycleOwner, Observer { updateUI(it)})
        }
        return binding.root
    }

    private fun updateUI(userObj: User) {
        with(binding) {
            user = userObj
            initList("request", processList)
            initList("complete", finishList)
            initList("confirm", confirmList)
            Firebase.firestore.document("branch/f303/users/${userObj.userId}").addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject(User::class.java).apply {
                        leaveBalance = this?.leaveBalance
                    }
                }
            }
            val counterRef = Firebase.firestore.collection("branch/f303/counter")
            counterRef.document(userObj.userId).addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val counterObj: Counter? = snapshot.toObject()
                    counter = counterObj
                }
            }
            processCard.setOnClickListener { setVisibility(getString(R.string.belum_disetujui), processList) }
            finishCard.setOnClickListener { setVisibility(getString(R.string.sudah_selesai), finishList) }
            confirmCard.setOnClickListener { setVisibility(getString(R.string.perlu_disetujui), confirmList) }
            floatingActionButton.setOnClickListener {
                val action = DashboardFragmentDirections.actionCreatePermit(userObj)
                findNavController().navigate(action)
            }
        }
    }

    private fun setVisibility(message: String, rv: RecyclerView) {
        with(binding) {
            headLabel.text = message
            processList.visibility = View.GONE
            finishList.visibility = View.GONE
            confirmList.visibility = View.GONE
            rv.visibility = View.VISIBLE
        }
    }

    private fun initList(
        status: String,
        rv: RecyclerView
    ) {
        val adapter = PagedListPermitAdapter(this@DashboardFragment)
        rv.adapter = adapter
        permitViewModel.networkRequestPagedList.observe(viewLifecycleOwner, Observer {
            adapter.updateNetworkState(it)
            binding.dashboardRefresh.isRefreshing = it ?: NetworkState.RUNNING == NetworkState.RUNNING
        })
        permitViewModel.getPermits(status)?.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            ItemClickSupport.addTo(rv).setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                override fun onItemClicked(recyclerView: RecyclerView, position: Int, view: View) {
                    val action = it[position]?.let { it1 -> DashboardFragmentDirections.actionShowDetailPermit(it1) }
                    action?.let { it1 -> findNavController().navigate(it1) }
                }
            })
        })
    }

    override fun whenListIsUpdated(size: Int, networkState: NetworkState?) {
        updateUI(networkState)
    }

    private fun updateUI(networkState: NetworkState?) {
        with(binding) {
            when (networkState) {
                NetworkState.SUCCESS -> {
                    rootLayout.visibility = View.VISIBLE
                    dashboardRefresh.isEnabled = false
                    emptyImage.visibility = View.GONE
                    emptyText.visibility = View.GONE
                    emptyButton.visibility = View.GONE
                }
                NetworkState.FAILED -> {
                    Glide.with(requireContext()).load(R.drawable.ic_healing_black_24dp)
                        .into(emptyImage)
                    rootLayout.visibility = View.GONE
                    emptyText.text = getString(R.string.technical_error)
                    emptyImage.visibility = View.VISIBLE
                    emptyText.visibility = View.VISIBLE
                    emptyButton.visibility = View.VISIBLE
                    dashboardRefresh.isEnabled = true
                    emptyButton.setOnClickListener {
                        permitViewModel.refreshRequestFailed()
                    }
                }
                else -> rootLayout.visibility = View.GONE
            }
        }
    }
    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }
}