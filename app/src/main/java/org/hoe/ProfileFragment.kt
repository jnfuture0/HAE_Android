package org.hoe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_profile.*
import org.hoe.Json.Crew6
import org.hoe.Json.User4
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    val server = LoginActivity.server
    val token = MainActivity.token
    val url = LoginActivity.url


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    fun setBtn(){
        profile_frag_edit_btn.setOnClickListener {
            val editIntent = Intent(context, EditUserActivity::class.java)
            startActivity(editIntent)
        }
    }

    fun setInfo(context:Context){
        server?.getUser(token)?.enqueue(object:Callback<User4>{
            override fun onFailure(call: Call<User4>, t: Throwable) { Log.e("GET_USER_ERR",t.toString()) }
            override fun onResponse(call: Call<User4>, response: Response<User4>) {
                val body = response.body()
                if(!body?.image.isNullOrBlank()){
                    Glide.with(context).load(url+body?.image).into(profile_frag_user_img)
                }
                profile_frag_userName.text = body?.name
            }
        })

        server?.getUserCrew(token)?.enqueue(object:Callback<List<Crew6>>{
            override fun onFailure(call: Call<List<Crew6>>, t: Throwable) { Log.e("GET_USER_CREW_ERR", t.toString()) }
            override fun onResponse(call: Call<List<Crew6>>, response: Response<List<Crew6>>) {
                val body = response.body()
                if(body.isNullOrEmpty()){
                    profile_frag_userCrew.visibility = View.GONE
                    profile_frag_noUserCrew.visibility = View.VISIBLE
                }else{
                    profile_frag_userCrew.visibility = View.VISIBLE
                    profile_frag_noUserCrew.visibility = View.GONE

                    profile_frag_userCrew.setOnClickListener {
                        val mainViewPager = (activity as MainActivity).findViewById<NonSwipeViewPager>(R.id.vpMainActivity)
                        mainViewPager.setCurrentItem(2, true)
                    }

                    Glide.with(context).load(url+"/yolov5/yolov5/inference/media/"+body[0].image).into(profile_frag_crew_img)

                    profile_frag_crew_name.text = body[0].name
                    profile_frag_crew_memberCount.text = "멤버 수 : ${body[0].mem_count}명"
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        profile_frag_user_img_outLine.apply {
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            clipToOutline = true
        }

        setBtn()
        setInfo(requireContext())



    }
}
