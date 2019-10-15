package com.pertamina.absensiapplication.view


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.databinding.FragmentManagePjsBinding
import com.pertamina.absensiapplication.model.User
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManagePJSFragment : Fragment(), TextWatcher {
    private lateinit var binding: FragmentManagePjsBinding
    private val userViewModel: UserViewModel by viewModel()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManagePjsBinding.inflate(inflater, container, false).apply {
            userViewModel.getNetwork().observe(viewLifecycleOwner, Observer {
                profileRefresh.isRefreshing = it == NetworkState.RUNNING
                profileRefresh.setOnRefreshListener {
                    userViewModel.refreshUsers("organic")
                }
                when (it) {
                    NetworkState.SUCCESS -> {
                        profileRefresh.isEnabled = false
                        rootLayout.visibility = View.VISIBLE
                        emptyListImage.visibility = View.GONE
                        emptyListText.visibility = View.GONE
                        emptyListButton.visibility = View.GONE
                    }
                    NetworkState.FAILED -> {
                        com.bumptech.glide.Glide.with(requireContext()).load(R.drawable.ic_healing_black_24dp)
                                .into(emptyListImage)
                        rootLayout.visibility = View.GONE
                        emptyListText.text = getString(R.string.technical_error)
                        emptyListImage.visibility = View.VISIBLE
                        emptyListText.visibility = View.VISIBLE
                        emptyListButton.visibility = View.VISIBLE
                        emptyListButton.setOnClickListener {
                            userViewModel.refreshUser()
                        }
                    }
                    else -> {
                        rootLayout.visibility = View.GONE
                    }
                }
            })
            leaveEmployeeDropdown.addTextChangedListener(this@ManagePJSFragment)
            pjsEmployeeDropdown.addTextChangedListener(this@ManagePJSFragment)
            val positions = resources.getStringArray(R.array.organicPosition)
            var users = mutableListOf<User>()
            userViewModel.getUsers("activeOrganic").observe(viewLifecycleOwner, Observer { listUser ->
                val list = mutableListOf<User>()
                val ranges = 1..6
                for (i in ranges) {
                    list.add(User(""))
                }
                listUser.toMutableList().forEach { user ->
                    when (user.position) {
                        positions[0] -> list[0] = user
                        positions[1] -> list[1] = user
                        positions[2] -> list[2] = user
                        positions[3] -> list[3] = user
                        positions[4] -> list[4] = user
                        positions[5] -> list[5] = user
                        else -> list.add(user)
                    }
                }
                users = list.filter { it.name.isNotEmpty() }.toMutableList()
                val senior = users.filterNot { it.senior.isNotEmpty() && it.operationHead.isNotEmpty() }.toMutableList()
                leaveEmployeeDropdown.setAdapter(configureAdapter(senior.map { it.name.capitalizeWords() }))
                pjsEmployeeDropdown.setAdapter(configureAdapter(users.map { it.name.capitalizeWords() }))
            })

            submitButton.setOnClickListener { view ->
                var leaveEmployeeObj = User()
                var pjsEmployee = User()
                users.forEach {
                    if (leaveEmployeeDropdown.text.toString().equals(it.name, true)) {
                        leaveEmployeeObj = it
                    }
                    if (pjsEmployeeDropdown.text.toString().equals(it.name, true)) {
                        pjsEmployee = it
                    }
                }
                val nameEmployee = leaveEmployeeObj.name
                MaterialAlertDialogBuilder(context)
                        .setTitle("Apakah anda yakin?")
                        .setMessage("Apakah anda yakin ingin MENGUBAH posisi jabatan sementara $nameEmployee?")
                        .setNegativeButton("Tidak", null)
                        .setPositiveButton("Iya") { _, _ ->
                            leaveEmployeeObj.leave = true
                            leaveEmployeeObj.pjs = pjsEmployee.userId
                            pjsEmployee.standIn.add(leaveEmployeeObj.userId)
                            userViewModel.updateUser(leaveEmployeeObj)
                            userViewModel.updateUser(pjsEmployee)
                            Snackbar.make(view, "${pjsEmployee.name.trim()} telah ditunjuk sebagai PJS dari ${leaveEmployeeObj.name.trim()}", Snackbar.LENGTH_SHORT).show()
                            val action = ManagePJSFragmentDirections.actionShowUserList()
                            findNavController().navigate(action)

                        }
                        .show()
            }
        }
        return binding.root
    }

    private fun configureAdapter(stringList: List<String>) =
            ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_item,
                    stringList
            )

    override fun afterTextChanged(s: Editable?) {
        with(binding) {
            pjsEmployeeInputlayout.error = null
            pjsEmployeeDropdown.isEnabled = leaveEmployeeDropdown.text.isNotEmpty()
            if (leaveEmployeeDropdown.text.toString().equals(pjsEmployeeDropdown.text.toString(), true)) {
                pjsEmployeeDropdown.setText("")
                pjsEmployeeInputlayout.error = "Karyawan PJS tidak boleh sama dengan karyawan yang cuti"
            }
            submitButton.isEnabled = !(leaveEmployeeDropdown.text.isEmpty() || pjsEmployeeDropdown.text.isEmpty())
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }
}