package com.demo.miniflicks.dramaseries.shortstory.ui.home
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class MainPagerAdapterOscar(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CinemixFragmentOscar()
            1 -> PlaylistFragmentOscar()
            2 -> SettingFragmentOscar()
            else -> CinemixFragmentOscar()
        }
    }
}