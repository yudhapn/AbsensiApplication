package com.pertamina.absensiapplication.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.databinding.FragmentManageBinding
import com.pertamina.absensiapplication.view.adapter.ViewPagerAdapter
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*


class ManageFragment : Fragment() {
    private lateinit var binding: FragmentManageBinding
    private val userViewModel: UserViewModel by viewModel()
    private var cookiePosition: String? = ""

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageBinding.inflate(inflater, container, false).apply {
            val myFormat = "EEE, dd MMM yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
            val currentDate = Calendar.getInstance()
            tvDate.text = sdf.format(currentDate.time)
            val userShared = activity?.getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
            cookiePosition = userShared?.getString("position", "missing")
            val adapter = ViewPagerAdapter(childFragmentManager, requireContext(), "manageFragment")
            viewPager.adapter = adapter
            tabLayout.setupWithViewPager(viewPager)

            userViewModel.getCounterGlobal().observe(viewLifecycleOwner, Observer {
                tabLayout.getTabAt(0)?.text = "Organik (${it.counterOrganic})"
                tabLayout.getTabAt(1)?.text = "TKJP (${it.counterTkjp})"
            })

            if (cookiePosition != null && (cookiePosition.equals("Spv Sales Services & General Affair")
                            || cookiePosition.equals(""))) {
                speedDial.addActionItem(
                        SpeedDialActionItem.Builder(R.id.fab_add_user, R.drawable.follow)
                                .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorGreen, requireContext().theme))
                                .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.colorWhite, requireContext().theme))
                                .setLabel(getString(R.string.tambah_pengguna))
                                .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorWhite, requireContext().theme))
                                .create()
                )
                speedDial.addActionItem(
                        SpeedDialActionItem.Builder(R.id.fab_change_pjs, R.drawable.exchange)
                                .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorBlue, requireContext().theme))
                                .setLabel(getString(R.string.atur_pjs))
                                .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorWhite, requireContext().theme))
                                .create()
                )

            }


            speedDial.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_create_permit, R.drawable.add_permit_24)
                            .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorRed, requireContext().theme))
                            .setLabel(getString(R.string.buat_sij_backdate))
                            .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorWhite, requireContext().theme))
                            .create()
            )

            speedDial.setOnActionSelectedListener { speedDialActionItem ->
                when (speedDialActionItem.id) {
                    R.id.fab_add_user -> {
                        val action = ManageFragmentDirections.actionAddUser()
                        findNavController().navigate(action)
                        false // true to keep the Speed Dial open
                    }
                    R.id.fab_change_pjs -> {
                        val action = ManageFragmentDirections.actionManagePjs()
                        findNavController().navigate(action)
                        false // true to keep the Speed Dial open
                    }
                    R.id.fab_create_permit -> {
                        val action = ManageFragmentDirections.actionCreatePermit()
                        findNavController().navigate(action)
                        false // true to keep the Speed Dial open
                    }
                    else -> false
                }
            }
        }
        return binding.root
    }
}