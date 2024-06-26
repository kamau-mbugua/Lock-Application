package com.kelvin.demoapplication

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kelvin.demoapplication.fragments.FragmentMainFragment
import com.kelvin.demoapplication.fragments.SetUpFragment

class MainViewPagerDefaultAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return FragmentMainFragment()
            1 -> return SetUpFragment()
        }
        return FragmentMainFragment()
    }
}