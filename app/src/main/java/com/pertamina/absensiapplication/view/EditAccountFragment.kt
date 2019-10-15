package com.pertamina.absensiapplication.view


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.pertamina.absensiapplication.databinding.FragmentEditAccountBinding
import com.pertamina.absensiapplication.model.User
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class EditAccountFragment : Fragment(), TextWatcher {
    private lateinit var binding: FragmentEditAccountBinding
    private val userViewModel: UserViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditAccountBinding.inflate(inflater, container, false).apply {
            var user = User()
            arguments?.let {
                user = EditAccountFragmentArgs.fromBundle(it).user
            }
            emailInput.addTextChangedListener(this@EditAccountFragment)
            passwordInput.addTextChangedListener(this@EditAccountFragment)
            submitButton.setOnClickListener { view ->
                val email = emailInput.text.toString()
                val password = passwordInput.text.toString()
                Log.d("testing", "email: $email, password: $password, uid: ${user.userId}")
                MaterialAlertDialogBuilder(context)
                    .setTitle("Apakah anda yakin?")
                    .setMessage("Apakah anda yakin ingin MENGUBAH akun pengguna ini?")
                    .setNegativeButton("Tidak", null)
                    .setPositiveButton("Iya") { _, _ ->
                        userViewModel.updateAccount(user.userId, email, password)
                        Snackbar.make(view, "Akun ${user.name.trim()} telah berhasil diubah", Snackbar.LENGTH_SHORT)
                            .show()
                        val action = EditAccountFragmentDirections.actionShowUserDetail(user)
                        findNavController().navigate(action)
                    }
                    .show()
            }
        }
        return binding.root
    }

    override fun afterTextChanged(s: Editable?) {
        with(binding) {
            submitButton.isEnabled = !(emailInput.text.isEmpty() || passwordInput.text.isEmpty() || passwordInput.text.toString().trim().length < 6)
            if (passwordInput.text.toString().trim().length < 6) {
                passwordInputlayout.error = "Kata sandi minimal terdiri dari 6 karakter"
            } else {
                passwordInputlayout.error = null
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}