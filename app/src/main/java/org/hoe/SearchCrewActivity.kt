package org.hoe

import android.app.Activity
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_search_crew.*
import org.hoe.Adapter.RecyclerAdapterCrewCard
import org.hoe.Json.Crew6New
import org.hoe.Json.ResultString
import org.hoe.LoginActivity.Companion.url
import org.hoe.Struct.CrewCard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchCrewActivity : AppCompatActivity() {
    val server = LoginActivity.server
    val token = MainActivity.token
    var crewList :MutableList<CrewCard> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_crew)

        search_crew_page_recyclerView.layoutManager = GridLayoutManager(applicationContext, 2, GridLayoutManager.VERTICAL, false)

        search_crew_page_search_btn.setOnClickListener {
            val searchString =search_crew_page_editText.text.toString()
            if(searchString.isBlank()){
                getAllCrew()
            }else{
                searchCrew(search_crew_page_editText.text.toString())
            }
        }
    }

    fun searchCrew(searchString:String){
        crewList= mutableListOf()
        server?.getSearchCrewList(searchString)?.enqueue(object:Callback<List<Crew6New>>{
            override fun onFailure(call: Call<List<Crew6New>>, t: Throwable) { Log.e("SEARCH_CREW_ERR", t.toString()) }
            override fun onResponse(call: Call<List<Crew6New>>, response: Response<List<Crew6New>>) {
                val body = response.body()
                if(!body.isNullOrEmpty()){
                    for (element in body){
                        if(element.ID == 1){
                            continue
                        }
                        crewList.add(CrewCard(element.ID!!, element.image!!, element.name!!, element.des!!, element.count, element.gathering_count))
                    }

                    val crewListAdapter = RecyclerAdapterCrewCard(applicationContext, crewList){ it, isSearch ->
                        val builder = AlertDialog.Builder(this@SearchCrewActivity)
                        val dialogView = layoutInflater.inflate(R.layout.dialog_crew_page, null)
                        builder.setView(dialogView)
                        val img = dialogView.findViewById<ImageView>(R.id.dialog_crew_page_img)
                        val title = dialogView.findViewById<TextView>(R.id.dialog_crew_page_title)
                        val content = dialogView.findViewById<TextView>(R.id.dialog_crew_page_content)
                        val memCount = dialogView.findViewById<TextView>(R.id.dialog_crew_page_memberCount)
                        val activityCount = dialogView.findViewById<TextView>(R.id.dialog_crew_page_activityCount)
                        val joinBtn = dialogView.findViewById<Button>(R.id.dialog_crew_page_join_btn)
                        val ad = builder.create()
                        val crewID = it.crew_id

                        Glide.with(applicationContext).load(url+"/yolov5/yolov5/inference/media/"+it.img).into(img)
                        title.text = it.title
                        content.text = it.content
                        memCount.text = it.memberCount.toString()
                        activityCount.text = it.gatheringCount.toString()
                        ad.show()

                        joinBtn.setOnClickListener {
                            server?.joinCrew(token, crewID.toString())?.enqueue(object:Callback<ResultString>{
                                override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("JOIN_CREW_ERR", t.toString()) }
                                override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                    setResult(Activity.RESULT_OK)
                                    ad.dismiss()
                                    finish()
                                }
                            })
                        }

                    }

                    search_crew_page_recyclerView.adapter = crewListAdapter
                }
            }

        })
    }

    fun getAllCrew(){
        crewList= mutableListOf()
        server?.getAllCrewList()?.enqueue(object:Callback<List<Crew6New>>{
            override fun onFailure(call: Call<List<Crew6New>>, t: Throwable) { Log.e("GET_ALL_CREW_ERR", t.toString()) }
            override fun onResponse(call: Call<List<Crew6New>>, response: Response<List<Crew6New>>) {
                val body = response.body()
                if(!body.isNullOrEmpty()){
                    for (element in body){
                        if(element.ID == 1){
                            continue
                        }
                        crewList.add(CrewCard(element.ID!!, element.image, element.name!!, element.des!!, element.count, element.gathering_count))
                    }

                    val crewListAdapter = RecyclerAdapterCrewCard(applicationContext, crewList){ it, isSearch ->
                        val builder = AlertDialog.Builder(this@SearchCrewActivity)
                        val dialogView = layoutInflater.inflate(R.layout.dialog_crew_page, null)
                        builder.setView(dialogView)
                        val img = dialogView.findViewById<ImageView>(R.id.dialog_crew_page_img)
                        val title = dialogView.findViewById<TextView>(R.id.dialog_crew_page_title)
                        val content = dialogView.findViewById<TextView>(R.id.dialog_crew_page_content)
                        val memCount = dialogView.findViewById<TextView>(R.id.dialog_crew_page_memberCount)
                        val activityCount = dialogView.findViewById<TextView>(R.id.dialog_crew_page_activityCount)
                        val joinBtn = dialogView.findViewById<Button>(R.id.dialog_crew_page_join_btn)
                        val ad = builder.create()

                        Glide.with(applicationContext).load(url+"/yolov5/yolov5/inference/media/"+it.img).into(img)
                        title.text = it.title
                        content.text = it.content
                        memCount.text = it.memberCount.toString()
                        activityCount.text = it.gatheringCount.toString()
                        ad.show()

                        joinBtn.setOnClickListener {
                            ad.dismiss()
                        }

                    }

                    search_crew_page_recyclerView.adapter = crewListAdapter
                }
            }

        })
    }
}