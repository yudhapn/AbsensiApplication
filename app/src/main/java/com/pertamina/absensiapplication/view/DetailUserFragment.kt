package com.pertamina.absensiapplication.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.databinding.FragmentDetailUserBinding
import com.pertamina.absensiapplication.model.User
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailUserFragment : Fragment() {
    val userViewModel: UserViewModel by viewModel()
    private lateinit var binding: FragmentDetailUserBinding
    private var cookiePosition: String? = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailUserBinding.inflate(inflater, container, false).apply {
            val userShared = activity?.getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
            cookiePosition = userShared?.getString("position", "missing")
            adminPosition = cookiePosition
            var user = User()
            arguments?.let {
                user = DetailUserFragmentArgs.fromBundle(it).user
            }
            userArg = user
            userName.text = user.name.trim().capitalizeWords()
            userViewModel.getListNetwork().observe(viewLifecycleOwner, Observer {
                profileRefresh.isRefreshing = it == NetworkState.RUNNING
                profileRefresh.setOnRefreshListener {
                    userViewModel.refreshListUser(user.userId, user.senior, user.operationHead)
                }
                when (it) {
                    NetworkState.SUCCESS -> {
                        rootLayout.visibility = View.VISIBLE
                        emptyImage.visibility = View.GONE
                        emptyText.visibility = View.GONE
                        emptyButton.visibility = View.GONE
                    }
                    NetworkState.FAILED -> {
                        com.bumptech.glide.Glide.with(requireContext()).load(R.drawable.ic_healing_black_24dp)
                            .into(emptyImage)
                        rootLayout.visibility = View.GONE
                        emptyText.text = getString(R.string.technical_error)
                        emptyImage.visibility = View.VISIBLE
                        emptyText.visibility = View.VISIBLE
                    }
                    else -> {
                        rootLayout.visibility = View.GONE
                    }
                }
            })

            userViewModel.getUser(user.userId, user.senior, user.operationHead)
                .observe(viewLifecycleOwner, Observer {
                    valueLeaveBalance.text = it[0].leaveBalance.toString()
                    valueSenior.text = if (it[1].name.isEmpty()) "-" else it[1].name.trim().capitalizeWords()
                    valueOperationHead.text = if (it[2].name.isEmpty()) "-" else it[2].name.trim().capitalizeWords()
                })

            var userPJS = User()
            if (user.leave && user.senior.isEmpty()) {
                // if user is senior or oh, continue execute
                userViewModel.getUser(user.pjs).observe(viewLifecycleOwner, Observer { userPJS = it })
            }
            activeEmployeeButton.setOnClickListener { view ->
                MaterialAlertDialogBuilder(context)
                    .setTitle("Apakah anda yakin?")
                    .setMessage("Apakah anda yakin ingin MENGAKTIFKAN pengguna ini?")
                    .setNegativeButton("Tidak", null)
                    .setPositiveButton("Iya") { _, _ ->
                        if (user.senior.isNotEmpty() && user.operationHead.isNotEmpty()) {
                            user.leave = false
                            userViewModel.updateUser(user)
                        } else {
                            user.leave = false
                            userPJS.standIn.remove(user.userId)
                            user.pjs = ""
                            userViewModel.updateUser(userPJS)
                            userViewModel.updateUser(user)
                            Snackbar.make(
                                view,
                                "${user.name.capitalizeWords().trim()} telah kembali aktif",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        val action = DetailUserFragmentDirections.actionShowUserList()
                        findNavController().navigate(action)
                    }
                    .show()
            }

            deactiveEmployeeButton.setOnClickListener { view ->
                MaterialAlertDialogBuilder(context)
                    .setTitle("Apakah anda yakin?")
                    .setMessage("Apakah anda yakin ingin MENANDAI pengguna ini sedang cuti?")
                    .setNegativeButton("Tidak", null)
                    .setPositiveButton("Iya") { _, _ ->
                        user.leave = true
                        userViewModel.updateUser(user)
                        Snackbar.make(
                            view,
                            "${user.name.capitalizeWords().trim()} telah ditandai sedang cuti",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        val action = DetailUserFragmentDirections.actionShowUserList()
                        findNavController().navigate(action)
                    }
                    .show()
            }

            setAccountButton.setOnClickListener {
                val action = DetailUserFragmentDirections.actionEditAccount(user)
                findNavController().navigate(action)
            }

            setProfileButton.setOnClickListener {
                val senior = valueSenior.text.toString()
                val operationHead = valueOperationHead.text.toString()
                val action = DetailUserFragmentDirections.actionEditProfile(user, senior, operationHead)
                findNavController().navigate(action)
            }

            viewPermitButton.setOnClickListener {
                val action = DetailUserFragmentDirections.actionShowHistory(user.userId)
                findNavController().navigate(action)
            }
        }
        return binding.root
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }
}