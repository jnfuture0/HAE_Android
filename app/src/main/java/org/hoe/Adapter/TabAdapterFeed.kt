package org.hoe.Adapter

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.hoe.FeedCrewFragment
import org.hoe.FeedMyFragment

class TabAdapterFeed(fm:FragmentManager):FragmentStatePagerAdapter(fm){
    //val context = context
    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> FeedCrewFragment().newInstance()

            1 -> FeedMyFragment().newInstance()

            else -> FeedCrewFragment().newInstance()
        }
    }

    override fun getCount() = 2

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        //return fragmentTitleList[position]    //맨위 ABCD 글자로 넣을 때
        return null //이미지만 보이고 텍스트 지우고 싶을 때
    }
}