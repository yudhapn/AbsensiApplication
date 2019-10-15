package com.pertamina.absensiapplication.view


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.databinding.FragmentAddUserBinding
import com.pertamina.absensiapplication.model.User
import com.pertamina.absensiapplication.util.InputFilterMinMax
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddUserFragment : Fragment(), TextWatcher {
    private lateinit var binding: FragmentAddUserBinding
    private val userViewModel: UserViewModel by viewModel()
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddUserBinding.inflate(inflater, container, false).apply {
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
            emailInput.addTextChangedListener(this@AddUserFragment)
            passwordInput.addTextChangedListener(this@AddUserFragment)
            passwordConfirmInput.addTextChangedListener(this@AddUserFragment)
            typeDropdown.addTextChangedListener(this@AddUserFragment)
            nameInput.addTextChangedListener(this@AddUserFragment)
            employeeNumberInput.addTextChangedListener(this@AddUserFragment)
            positionDropdown.addTextChangedListener(this@AddUserFragment)
            seniorDropdown.addTextChangedListener(this@AddUserFragment)
            operatoinheadDropdown.addTextChangedListener(this@AddUserFragment)
            val listType = resources.getStringArray(R.array.type).toList()
            balanceInput.filters = arrayOf<InputFilter>(InputFilterMinMax("1", "19"))
            typeDropdown.setAdapter(configureAdapter(listType))
            typeDropdown.setOnItemClickListener { _, _, position, _ ->
                positionDropdown.isEnabled = true
                if (listType[position] == "Organik") {
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
                operatoinheadDropdown.setAdapter(configureAdapter(listOH.map { it.name }))
            })

            userViewModel.getUsers("senior").observe(viewLifecycleOwner, Observer { list ->
                listSenior = list.filterNot { it.operationHead.isEmpty() }.toMutableList()
                listSenior.add(0, User(name = "-"))
                seniorDropdown.setAdapter(configureAdapter(listSenior.map { it.name }))
            })

            submitButton.setOnClickListener { view ->
                val email = emailInput.text.toString()
                val password = passwordInput.text.toString()
                val name = "${nameInput.text} ".toLowerCase()
                val number = employeeNumberInput.text.toString()
                val position = positionDropdown.text.toString()
                var senior = ""
                val type = typeDropdown.text.toString()
                val balance = balanceInput.text.toString()
                val organic = type.equals("Organik", true)
                var operationHead = ""
                listSenior.forEach {
                    if (seniorDropdown.text.toString().equals(it.name, false) && it.name != "-") {
                        senior = it.userId
                    }
                }
                listOH.forEach {
                    if (operatoinheadDropdown.text.toString().equals(it.name, false) && it.name != "-") {
                        operationHead = it.userId
                    }
                }
                val user = User(
                        userId = "",
                        employeeNumber = number,
                        name = name,
                        position = position,
                        leaveBalance = balance.toInt(),
                        senior = senior,
                        operationHead = operationHead,
                        organic = organic
                )
                MaterialAlertDialogBuilder(context)
                        .setTitle("Apakah anda yakin?")
                        .setMessage("Apakah anda yakin ingin MENAMBAHKAN pengguna baru ini?")
                        .setNegativeButton("Tidak", null)
                        .setPositiveButton("Iya") { _, _ ->
                            userViewModel.createUser(email, password, user)
                            Snackbar.make(view, "${name.trim()} telah berhasil ditambahkan menajadi pengguna baru", Snackbar.LENGTH_SHORT).show()
                            val action = AddUserFragmentDirections.actionShowUserList()
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
            var notSamePassword = true
            if (passwordInput.text.toString().trim().length < 6) {
                passwordInputlayout.error = "Kata sandi minimal terdiri dari 6 karakter"
                passwordConfirmInputlayout.isEnabled = false
            } else {
                passwordConfirmInputlayout.isEnabled = true
                if (passwordConfirmInput.text.toString() != passwordInput.text.toString()) {
                    passwordConfirmInputlayout.error = "Kata sandi tidak sama"
                    notSamePassword = true
                } else {
                    passwordConfirmInputlayout.error = null
                    notSamePassword = false
                }
                passwordInputlayout.error = null
            }
            submitButton.isEnabled = !(emailInput.text.isEmpty() || typeDropdown.text.isEmpty() || nameInput.text.isEmpty()
                    || employeeNumberInput.text.isEmpty() || positionDropdown.text.isEmpty() || balanceInput.text.toString().isEmpty() || notSamePassword)
            Log.d("testing", "balanceinput: ${balanceInput.text.toString().isEmpty()}, buttonisenabled: ${submitButton.isEnabled}")


        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    private fun hideKeyboard(view: View) {
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}