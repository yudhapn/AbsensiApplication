package com.pertamina.absensiapplication.view


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.pertamina.absensiapplication.databinding.FragmentLoginBinding
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment(), TextWatcher {
    private val auth by inject<FirebaseAuth>()
    private lateinit var binding: FragmentLoginBinding
    private val userViewModel: UserViewModel by viewModel()
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false).apply {
            emailInput.addTextChangedListener(this@LoginFragment)
            passwordInput.addTextChangedListener(this@LoginFragment)
            if (auth.currentUser != null) {
                val action = LoginFragmentDirections.actionShowDashboard()
                findNavController().navigate(action)
            } else {
                loginButton.setOnClickListener { view ->
                    progressbar.visibility = View.VISIBLE
                    val email = emailInput.text.toString()
                    val password = passwordInput.text.toString()

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val action = LoginFragmentDirections.actionShowDashboard()
                                findNavController().navigate(action)
                            } else {
                                Snackbar.make(view, "Kata sandi / email tidak valid", Snackbar.LENGTH_SHORT).show()
                            }
                            progressbar.visibility = View.GONE
                        }
                }
            }
        }
        return binding.root
    }

    override fun afterTextChanged(s: Editable?) {
        with(binding) {
            loginButton.isEnabled = !(emailInput.text.isEmpty() || passwordInput.text.isEmpty())
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

}