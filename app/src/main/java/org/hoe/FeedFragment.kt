package org.hoe

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_feed_inner.*
import org.hoe.Adapter.TabAdapterFeed

class FeedFragment() : Fragment() {

    private val adapter by lazy{ TabAdapterFeed(childFragmentManager) }

    val mContext = context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    fun setTabLayout(){
        vpFeed.adapter = adapter
        vpFeed.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int,positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                when(position){
                    0 -> { }
                    1 -> { }
                }
            }
        })


        tabLayout_feed.setupWithViewPager(vpFeed)
        tabLayout_feed.getTabAt(0)?.text = "크루"
        tabLayout_feed.getTabAt(1)?.text = "내 피드"
        tabLayout_feed.setTabTextColors(Color.parseColor("#bfbfbf"), Color.parseColor("#001023"))
    }

    fun setBtn(){
        feed_add_btn.setOnClickListener {
            val intent = Intent(requireContext(), AddFeedActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        setTabLayout()

        setBtn()





    }

}
