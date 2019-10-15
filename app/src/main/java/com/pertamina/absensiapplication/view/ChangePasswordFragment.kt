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
import com.google.firebase.auth.FirebaseAuth
import com.pertamina.absensiapplication.databinding.FragmentChangePasswordBinding
import org.koin.android.ext.android.inject

class ChangePasswordFragment : Fragment() {

    private val auth by inject<FirebaseAuth>()
    private lateinit var binding: FragmentChangePasswordBinding
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false).apply {
            passwordInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (passwordInput.text.toString().trim().length < 6) {
                        passwordInputlayout.error = "Kata sandi minimal terdiri dari 6 karakter"
                    } else {
                        passwordInputlayout.error = null
                    }
                }
            })
            passwordConfirmInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (passwordConfirmInput.text.toString().trim() != passwordInput.text.toString().trim()) {
                        passwordConfirmInputlayout.error = "Kata sandi tidak sama"
                        submitButton.isEnabled = false
                    } else {
                        passwordConfirmInputlayout.error = null
                        submitButton.isEnabled = true
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            submitButton.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                        .setTitle("Apakah anda yakin?")
                        .setMessage("Apakah anda yakin ingin MENGGANTI password dengan yang baru?")
                        .setNegativeButton("Tidak", null)
                        .setPositiveButton("Iya") { _, _ ->
                            auth.currentUser?.updatePassword(passwordConfirmInput.text.toString())
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("Testing", "User password updated.")
                                        }
                                    }
                            val action = ChangePasswordFragmentDirections.actionShowProfile()
                            findNavController().navigate(action)
                        }
                        .show()
            }
        }
        return binding.root
    }
}