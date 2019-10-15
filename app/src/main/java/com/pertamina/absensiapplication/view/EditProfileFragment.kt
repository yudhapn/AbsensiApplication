package com.pertamina.absensiapplication.view


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.databinding.FragmentEditProfileBinding
import com.pertamina.absensiapplication.model.User
import com.pertamina.absensiapplication.util.InputFilterMinMax
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class EditProfileFragment : Fragment(), TextWatcher {
    private lateinit var binding: FragmentEditProfileBinding
    private val userViewModel: UserViewModel by viewModel()
    private var cookiePosition: String? = ""

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false).apply {
            val userShared = activity?.getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
            cookiePosition = userShared?.getString("position", "missing")
            adminPosition = cookiePosition
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
            var user = User()
            var senior = "-"
            var operationHead = "-"
            arguments?.let {
                user = EditProfileFragmentArgs.fromBundle(it).user
                senior = EditProfileFragmentArgs.fromBundle(it).senior
                operationHead = EditProfileFragmentArgs.fromBundle(it).operationHead
            }
            balanceInput.filters = arrayOf<InputFilter>(InputFilterMinMax("1", "19"))
            userArg = user
            typeDropdown.setText(if (user.organic) "Organik" else "TKJP")
            nameInput.setText(user.name.trim().capitalizeWords())
            seniorDropdown.setText(senior.trim().capitalizeWords())
            operatoinheadDropdown.setText(operationHead.trim().capitalizeWords())
            typeDropdown.addTextChangedListener(this@EditProfileFragment)
            nameInput.addTextChangedListener(this@EditProfileFragment)
            employeeNumberInput.addTextChangedListener(this@EditProfileFragment)
            positionDropdown.addTextChangedListener(this@EditProfileFragment)
            val listType = resources.getStringArray(R.array.type).toList()
            typeDropdown.setAdapter(configureAdapter(listType))
            typeDropdown.setOnItemClickListener { _, _, position, _ ->
                positionDropdown.isEnabled = true
                if (listType[position].equals("Organik", true)) {
                    positionDropdown.setAdapter(configureAdapter(resources.getStringArray(R.array.organicPosition).toList()))
                    positionDropdown.setText("")
                    positionDropdown.clearFocus()
                } else {
                    positionDropdown.setAdapter(configureAdapter(resources.getStringArray(R.array.tkjpPosition).toList()))
                    positionDropdown.setText("")
                    positionDropdown.clearFocus()
                }
            }
            positionDropdown.setOnItemClickListener { _, _, _, _ ->
                hideKeyboard(positionDropdown)
                positionDropdown.clearFocus()
            }
            var listOH = mutableListOf<User>()
            var listSenior = mutableListOf<User>()
            userViewModel.getUsers("operationHead").observe(viewLifecycleOwner, Observer { list ->
                listOH = list.filter { it.senior.isEmpty() && it.operationHead.isEmpty() }.toMutableList()
                listOH.add(0, User(name = "-"))
                operatoinheadDropdown.setAdapter(configureAdapter(listOH.map { user -> user.name.trim().capitalizeWords() }))
            })

            userViewModel.getUsers("senior").observe(viewLifecycleOwner, Observer { list ->
                listSenior =
                        list.filterNot { it.senior.equals(user.userId, true) || it.operationHead.isEmpty() }.toMutableList()
                listSenior.add(0, User(name = "-"))
                seniorDropdown.setAdapter(configureAdapter(listSenior.map { user -> user.name.trim().capitalizeWords() }))
            })

            submitButton.setOnClickListener { view ->
                user.name = "${nameInput.text} ".toLowerCase()
                user.leaveBalance = balanceInput.text.toString().toInt()
                user.employeeNumber = employeeNumberInput.text.toString()
                user.position = positionDropdown.text.toString()
                val type = typeDropdown.text.toString()
                user.organic = type.equals("Organik", true)
                listSenior.forEach {
                    if (seniorDropdown.text.toString().equals(it.name.trim(), true)) {
                        user.senior = it.userId
                    }
                }
                listOH.forEach {
                    if (operatoinheadDropdown.text.toString().equals(it.name.trim(), true)) {
                        user.operationHead = it.userId
                    }
                }

                MaterialAlertDialogBuilder(context)
                        .setTitle("Apakah anda yakin?")
                        .setMessage("Apakah anda yakin ingin MENGUBAH profil pengguna ini?")
                        .setNegativeButton("Tidak", null)
                        .setPositiveButton("Iya") { _, _ ->
                            userViewModel.updateUser(user)
                            Snackbar.make(view, "Profil ${user.name.trim()} telah berhasil diubah", Snackbar.LENGTH_SHORT)
                                    .show()
                            val action = EditProfileFragmentDirections.actionShowUserDetail(user)
                            findNavController().navigate(action)
                        }
                        .show()
            }
        }
        return binding.root
    }

    private fun configureAdapter(stringArray: List<String>) =
            ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_item,
                    stringArray
            )

    override fun afterTextChanged(s: Editable?) {
        with(binding) {
            submitButton.isEnabled = !(typeDropdown.text.isEmpty() || nameInput.text.isEmpty()
                    || employeeNumberInput.text.isEmpty())
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    private fun hideKeyboard(view: View) {
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }
}