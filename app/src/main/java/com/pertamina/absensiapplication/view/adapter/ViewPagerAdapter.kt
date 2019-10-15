package com.pertamina.absensiapplication.view.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.view.OrganicListFragment

@Suppress("DEPRECATION")
class ViewPagerAdapter(
        fm: FragmentManager,
        private val context: Context,
        private val parentFragment: String
) :
        FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment =
            when (position) {
                0 -> OrganicListFragment("organik", parentFragment)
                1 -> OrganicListFragment("tkjp", parentFragment)
                else -> OrganicListFragment("organik", parentFragment)
            }

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence? =
            when (position) {
                0 -> context.getString(R.string.organic)
                1 -> context.getString(R.string.tkjp)
                else -> context.getString(R.string.tkjp)
            }
}