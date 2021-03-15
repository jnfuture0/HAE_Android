package org.hoe.Adapter

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.hoe.CrewFragment
import org.hoe.FeedFragment
import org.hoe.MapFragment
import org.hoe.ProfileFragment

class TabAdapterMain (fm: FragmentManager, context:Context): FragmentStatePagerAdapter(fm) {

    val context = context

    override fun getItem(p0: Int): Fragment {
        return when(p0){
            0 -> FeedFragment()

            1 -> MapFragment()

            2 -> CrewFragment()

            3 -> ProfileFragment()

            else -> FeedFragment()
        }
    }

    override fun getCount() = 4

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return null
    }

}