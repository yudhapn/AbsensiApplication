package com.pertamina.absensiapplication.view


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.firebase.iid.FirebaseInstanceId
import com.pertamina.absensiapplication.databinding.FragmentStatusBinding
import com.pertamina.absensiapplication.view.adapter.ViewPagerAdapter
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*


class StatusFragment : Fragment() {
    private lateinit var binding: FragmentStatusBinding
    private val userViewModel: UserViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatusBinding.inflate(inflater, container, false).apply {
            val userShared = activity?.getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
            val myFormat = "EEE, dd MMM yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
            val currentDate = Calendar.getInstance()
            tvDate.text = sdf.format(currentDate.time)
            userViewModel.getUser().observe(viewLifecycleOwner, Observer { user ->
                val edit = userShared?.edit()
                edit?.putString("position", user.position)
                edit?.apply()
                FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val token = it.result?.token
                        //save tokenl
                        user.token = token.toString()
                        userViewModel.updateUser(user)
                    } else {
                        Log.d("testing", it.exception?.message)
                    }
                }
            })
            val adapter = ViewPagerAdapter(childFragmentManager, requireContext(), "")
            viewPager.adapter = adapter
            tabLayout.setupWithViewPager(viewPager)
            userViewModel.getCounterGlobal().observe(viewLifecycleOwner, Observer {
                tabLayout.getTabAt(0)?.text = "Organik (${it.counterOrganic})"
                tabLayout.getTabAt(1)?.text = "TKJP (${it.counterTkjp})"
            })
        }
        return binding.root
    }
}