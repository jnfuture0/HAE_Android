package org.hoe

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_crew.*
import org.hoe.Adapter.RecyclerAdapterCrewActivityCard
import org.hoe.Adapter.RecyclerAdapterCrewCard
import org.hoe.Adapter.RecyclerAdapterMemberList
import org.hoe.Json.*
import org.hoe.Struct.GatheringCard
import org.hoe.Struct.CrewCard
import org.hoe.Struct.MemberCard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CrewFragment() : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    var isInCrew = MainActivity.hasCrew
    val server = LoginActivity.server
    val token = MainActivity.token
    val userID = MainActivity.userID
    val url = LoginActivity.url
    val REQUEST_SEARCH_CREW = 123
    val REQUEST_ADD_CREW = 200
    val REQUEST_ADD_GATHERING = 300
    var crewCardList :MutableList<CrewCard> = mutableListOf()
    var crewGatheringList:MutableList<GatheringCard> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_crew, container, false)
    }

    private fun checkUserCrew(){
        server?.getUser(token)?.enqueue(object:Callback<User4>{
            override fun onFailure(call: Call<User4>, t: Throwable) { Log.e("GET_USER_CREW_ERR",t.toString()) }
            override fun onResponse(call: Call<User4>, response: Response<User4>) {
                val hasCrew = response.body()?.crew_ID != 1
                if(!hasCrew){
                    showNoCrew()
                    setCrewCardList()
                }else{
                    showInCrew()
                    setCrewGatheringList()
                }
            }
        })
    }

    fun showInCrew(){
        crew_page_no_crew_layout.visibility = View.GONE
        crew_page_yes_crew_refreshLayout.visibility = View.VISIBLE
    }

    fun showNoCrew(){
        crew_page_no_crew_layout.visibility = View.VISIBLE
        crew_page_yes_crew_refreshLayout.visibility = View.GONE
    }

    private fun showCrewCardDetailDialog(it:CrewCard){
        val builder = AlertDialog.Builder(context)
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

        Glide.with(requireContext()).load(LoginActivity.url +"/yolov5/yolov5/inference/media/"+it.img).into(img)
        title.text = it.title
        content.text = it.content
        memCount.text = if(it.memberCount == null){
            "0"
        }else{
            it.memberCount.toString()
        }
        activityCount.text = if(it.gatheringCount == null){
            "0"
        }else{
            it.gatheringCount.toString()
        }
        ad.show()

        joinBtn.setOnClickListener {
            server?.joinCrew(token, crewID.toString())?.enqueue(object:Callback<ResultString>{
                override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("JOIN_CREW_ERR", t.toString()) }
                override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                    ad.dismiss()
                    checkUserCrew()
                }
            })
        }
    }

    private fun setCrewCardList(){
        crewCardList = mutableListOf()
        server?.getAllCrewList()?.enqueue(object: Callback<List<Crew6New>> {
            override fun onFailure(call: Call<List<Crew6New>>, t: Throwable) { Log.e("GET_ALL_CREW_ERR", t.toString()) }
            override fun onResponse(call: Call<List<Crew6New>>, response: Response<List<Crew6New>>) {
                val body = response.body()
                if(!body.isNullOrEmpty()){
                    crewCardList.add(CrewCard(0,"","","",0,0))
                    for (element in body){
                        if(element.ID == 1){
                            continue
                        }
                        crewCardList.add(CrewCard(element.ID!!, element.image, element.name!!, element.des!!, element.count, element.gathering_count))
                    }

                    val crewListAdapter = RecyclerAdapterCrewCard(requireContext(), crewCardList){ it, isSearch ->
                        if(isSearch){
                            val searchIntent = Intent(context, SearchCrewActivity::class.java)
                            startActivityForResult(searchIntent, REQUEST_SEARCH_CREW)
                        }else{
                            showCrewCardDetailDialog(it)
                        }
                    }

                    crew_page_crew_list_recyclerView.adapter = crewListAdapter
                    crew_page_crew_list_recyclerView.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
                }
            }

        })
    }

    private fun setCrewGatheringList(){
        crewGatheringList = mutableListOf()
        server?.getGathering(token)?.enqueue(object:Callback<List<Gathering8>>{
            override fun onFailure(call: Call<List<Gathering8>>, t: Throwable) { Log.e("GET_GATHERING_ERR", t.toString()) }
            override fun onResponse(call: Call<List<Gathering8>>, response: Response<List<Gathering8>>) {
                val body = response.body()
                if(!body.isNullOrEmpty()){

                    server.getUserCrew(token).enqueue(object:Callback<List<Crew6>>{
                        override fun onFailure(call: Call<List<Crew6>>, t: Throwable) { Log.e("GET_USER_CREW_ERR", t.toString()) }
                        override fun onResponse(call: Call<List<Crew6>>, response: Response<List<Crew6>>) {
                            val innerBody = response.body()
                            //information
                            if(!innerBody.isNullOrEmpty()){
                                crewGatheringList.add(GatheringCard(innerBody[0].mem_count!!, 0, innerBody[0].name!!, innerBody[0].des!!, true, body.size, false, innerBody[0].image))
                            }
                            //gathering
                            for(element in body){
                                val isParticipant = element.participate != 0
                                val isMine = element.user_ID_id == userID
                                crewGatheringList.add(GatheringCard(element.ID!!, element.user_ID_id!!, element.name!!, element.date!!, isParticipant, element.count, isMine))
                            }
                            //leave
                            crewGatheringList.add(GatheringCard(0,0,"","",true,0, false))
                            server?.isCrewHeader(token)?.enqueue(object:Callback<ResultString>{
                                override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("IS_CREW_HEADER_ERR", t.toString()) }
                                override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                    val isHeader = response.body()?.result == "success"
                                    val crewListJoinedAdapter = RecyclerAdapterCrewActivityCard(requireContext(), crewGatheringList, isHeader){ itGatheringCard, idx, participation, parNumText, joinBtn, leaveBtn ->
                                        when(idx){
                                            0 -> {
                                                //참여자 보기
                                                showParticipantList(itGatheringCard)
                                            }
                                            1 -> {
                                                //댓글 보기
                                                val gatheringCommentIntent = Intent(requireContext(), GatheringCommentActivity::class.java)
                                                gatheringCommentIntent.putExtra("gathering_id", itGatheringCard.gathering_id)
                                                startActivity(gatheringCommentIntent)
                                            }
                                            2 -> {
                                                //참여하기
                                                server.joinGathering(token, itGatheringCard.gathering_id.toString()).enqueue(object:Callback<ResultString>{
                                                    override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("JOIN_GATHERING_ERR", t.toString()) }
                                                    override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                                        participation?.setBackgroundResource(R.drawable.style_radius_5_primary)
                                                        val num = Integer.parseInt(parNumText?.text.toString())
                                                        parNumText?.text = "${num+1}"
                                                        joinBtn!!.visibility = View.GONE
                                                        leaveBtn!!.visibility = View.VISIBLE
                                                    }
                                                })
                                            }
                                            3 -> {
                                                //참여취소하기
                                                server.leaveGathering(token, itGatheringCard.gathering_id.toString()).enqueue(object:Callback<ResultString>{
                                                    override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("LEAVE_GATHERING_ERR", t.toString()) }
                                                    override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                                        participation?.setBackgroundResource(R.drawable.style_radius_5_lightgray)
                                                        val num = Integer.parseInt(parNumText?.text.toString())
                                                        parNumText?.text = "${num-1}"
                                                        joinBtn!!.visibility = View.VISIBLE
                                                        leaveBtn!!.visibility = View.GONE
                                                    }
                                                })
                                            }
                                            4 -> {
                                                //크루 탈퇴
                                                val builder = AlertDialog.Builder(context)
                                                val dialogView = layoutInflater.inflate(R.layout.dialog_ask_again, null)
                                                builder.setView(dialogView)
                                                val yesBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_yes_btn)
                                                val noBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_no_btn)
                                                val ad = builder.create()
                                                ad.show()

                                                yesBtn.setOnClickListener {
                                                    server.leaveCrew(token).enqueue(object:Callback<ResultString>{
                                                        override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("LEAVE_CREW_ERR", t.toString()) }
                                                        override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                                            Toast.makeText(context, "탈퇴했습니다", Toast.LENGTH_SHORT).show()
                                                            ad.dismiss()
                                                            checkUserCrew()
                                                        }
                                                    })
                                                }

                                                noBtn.setOnClickListener {
                                                    ad.dismiss()
                                                }

                                            }
                                            5 -> {
                                                //크루 삭제
                                                val builder = AlertDialog.Builder(context)
                                                val dialogView = layoutInflater.inflate(R.layout.dialog_ask_again, null)
                                                builder.setView(dialogView)
                                                val yesBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_yes_btn)
                                                val noBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_no_btn)
                                                val ad = builder.create()
                                                ad.show()

                                                yesBtn.setOnClickListener {
                                                    server.deleteCrew(token).enqueue(object:Callback<ResultString>{
                                                        override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("DELETE_CREW_ERR", t.toString()) }
                                                        override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                                            Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                                                            checkUserCrew()
                                                            ad.dismiss()
                                                        }
                                                    })
                                                }

                                                noBtn.setOnClickListener {
                                                    ad.dismiss()
                                                }

                                            }
                                            6 -> {
                                                //모임 삭제
                                                val builder = AlertDialog.Builder(context)
                                                val dialogView = layoutInflater.inflate(R.layout.dialog_ask_again, null)
                                                builder.setView(dialogView)
                                                val yesBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_yes_btn)
                                                val noBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_no_btn)
                                                val text = dialogView.findViewById<TextView>(R.id.dialog_ask_again_text)
                                                val ad = builder.create()
                                                text.text = "해당 모임을 삭제하시겠습니까?"
                                                ad.show()

                                                yesBtn.setOnClickListener {
                                                    server.deleteGathering(token, itGatheringCard.gathering_id.toString()).enqueue(object:Callback<ResultString>{
                                                        override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("DELETE_GATHERING_ERR", t.toString()) }
                                                        override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                                            Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                                                            setCrewGatheringList()
                                                            ad.dismiss()
                                                        }
                                                    })
                                                }
                                                noBtn.setOnClickListener {
                                                    ad.dismiss()
                                                }
                                            }
                                            7 -> {
                                                //모임 추가
                                                val addGatheringIntent = Intent(context, AddGatheringActivity::class.java)
                                                startActivityForResult(addGatheringIntent, REQUEST_ADD_GATHERING)
                                            }
                                            else -> {}
                                        }
                                    }
                                    crew_page_yes_crew_layout_recyclerView.adapter = crewListJoinedAdapter
                                    crew_page_yes_crew_layout_recyclerView.layoutManager = LinearLayoutManager(context)
                                }
                            })

                        }
                    })

                }else{
                    server.getUserCrew(token).enqueue(object:Callback<List<Crew6>> {
                        override fun onFailure(call: Call<List<Crew6>>, t: Throwable) { Log.e("GET_USER_CREW_ERR", t.toString()) }
                        override fun onResponse(call: Call<List<Crew6>>, response: Response<List<Crew6>>) {
                            val innerBody = response.body()
                            //information
                            if (!innerBody.isNullOrEmpty()) {
                                crewGatheringList.add(GatheringCard(innerBody[0].mem_count!!, 0, innerBody[0].name!!, innerBody[0].des!!, true, innerBody[0].gathering_count, false, innerBody[0].image))
                            }
                            //exit
                            crewGatheringList.add(GatheringCard(0,0,"","",true,0,false))
                            server?.isCrewHeader(token)?.enqueue(object:Callback<ResultString>{
                                override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("IS_CREW_HEADER_ERR", t.toString()) }
                                override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                    val isHeader = response.body()?.result == "success"
                                    val crewListJoinedAdapter = RecyclerAdapterCrewActivityCard(requireContext(), crewGatheringList, isHeader){ itGatheringCard, idx, participation, parNumText, joinBtn, leaveBtn ->
                                        when(idx){
                                            0 -> {
                                                //참여자 보기
                                                showParticipantList(itGatheringCard)
                                            }
                                            1 -> {
                                                //댓글 보기
                                                val gatheringCommentIntent = Intent(requireContext(), GatheringCommentActivity::class.java)
                                                gatheringCommentIntent.putExtra("gathering_id", itGatheringCard.gathering_id)
                                                startActivity(gatheringCommentIntent)
                                            }
                                            2 -> {
                                                //참여하기
                                                server.joinGathering(token, itGatheringCard.gathering_id.toString()).enqueue(object:Callback<ResultString>{
                                                    override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("JOIN_GATHERING_ERR", t.toString()) }
                                                    override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                                        participation?.setBackgroundResource(R.drawable.style_radius_5_primary)
                                                        val num = Integer.parseInt(parNumText?.text.toString())
                                                        parNumText?.text = "${num+1}"
                                                        joinBtn!!.visibility = View.GONE
                                                        leaveBtn!!.visibility = View.VISIBLE
                                                    }
                                                })
                                            }
                                            3 -> {
                                                //참여취소하기
                                                server.leaveGathering(token, itGatheringCard.gathering_id.toString()).enqueue(object:Callback<ResultString>{
                                                    override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("LEAVE_GATHERING_ERR", t.toString()) }
                                                    override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                                        participation?.setBackgroundResource(R.drawable.style_radius_5_lightgray)
                                                        val num = Integer.parseInt(parNumText?.text.toString())
                                                        parNumText?.text = "${num-1}"
                                                        joinBtn!!.visibility = View.VISIBLE
                                                        leaveBtn!!.visibility = View.GONE
                                                    }
                                                })
                                            }
                                            4 -> {
                                                //크루 탈퇴
                                                val builder = AlertDialog.Builder(context)
                                                val dialogView = layoutInflater.inflate(R.layout.dialog_ask_again, null)
                                                builder.setView(dialogView)
                                                val yesBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_yes_btn)
                                                val noBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_no_btn)
                                                val ad = builder.create()
                                                ad.show()

                                                yesBtn.setOnClickListener {
                                                    server.leaveCrew(token).enqueue(object:Callback<ResultString>{
                                                        override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("LEAVE_CREW_ERR", t.toString()) }
                                                        override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                                            Toast.makeText(context, "탈퇴했습니다", Toast.LENGTH_SHORT).show()
                                                            ad.dismiss()
                                                            checkUserCrew()
                                                        }
                                                    })
                                                }

                                                noBtn.setOnClickListener {
                                                    ad.dismiss()
                                                }

                                            }
                                            5 -> {
                                                //크루 삭제
                                                val builder = AlertDialog.Builder(context)
                                                val dialogView = layoutInflater.inflate(R.layout.dialog_ask_again, null)
                                                builder.setView(dialogView)
                                                val yesBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_yes_btn)
                                                val noBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_no_btn)
                                                val ad = builder.create()
                                                ad.show()

                                                yesBtn.setOnClickListener {
                                                    server.deleteCrew(token).enqueue(object:Callback<ResultString>{
                                                        override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("DELETE_CREW_ERR", t.toString()) }
                                                        override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                                            Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                                                            checkUserCrew()
                                                            ad.dismiss()
                                                        }
                                                    })
                                                }

                                                noBtn.setOnClickListener {
                                                    ad.dismiss()
                                                }

                                            }
                                            6 -> {
                                                //모임 삭제
                                                val builder = AlertDialog.Builder(context)
                                                val dialogView = layoutInflater.inflate(R.layout.dialog_ask_again, null)
                                                builder.setView(dialogView)
                                                val yesBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_yes_btn)
                                                val noBtn = dialogView.findViewById<Button>(R.id.dialog_ask_again_no_btn)
                                                val text = dialogView.findViewById<TextView>(R.id.dialog_ask_again_text)
                                                val ad = builder.create()
                                                text.text = "해당 모임을 삭제하시겠습니까?"
                                                ad.show()

                                                yesBtn.setOnClickListener {
                                                    server.deleteGathering(token, itGatheringCard.gathering_id.toString()).enqueue(object:Callback<ResultString>{
                                                        override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("DELETE_GATHERING_ERR", t.toString()) }
                                                        override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                                            Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                                                            setCrewGatheringList()
                                                            ad.dismiss()
                                                        }
                                                    })
                                                }
                                                noBtn.setOnClickListener {
                                                    ad.dismiss()
                                                }
                                            }
                                            7 -> {
                                                //모임 추가
                                                val addGatheringIntent = Intent(context, AddGatheringActivity::class.java)
                                                startActivityForResult(addGatheringIntent, REQUEST_ADD_GATHERING)
                                            }
                                            else -> {}
                                        }
                                    }
                                    crew_page_yes_crew_layout_recyclerView.adapter = crewListJoinedAdapter
                                    crew_page_yes_crew_layout_recyclerView.layoutManager = LinearLayoutManager(context)
                                }
                            })
                        }
                    })
                }
            }

        })
    }

    fun showParticipantList(it : GatheringCard){
        server?.getGatheringParticipant(it.gathering_id.toString())?.enqueue(object:Callback<List<User4>>{
            override fun onFailure(call: Call<List<User4>>, t: Throwable) { Log.e("GET_PARTICIPANT_ERR", t.toString()) }
            override fun onResponse(call: Call<List<User4>>, response: Response<List<User4>>) {
                val innerBody = response.body()
                val partiList:MutableList<MemberCard> = mutableListOf()
                if(!innerBody.isNullOrEmpty()){
                    for(element in innerBody){
                        partiList.add(MemberCard(element.image, element.name!!))
                    }
                }
                val partiAdapter = RecyclerAdapterMemberList(requireContext(),partiList){}
                val builder = AlertDialog.Builder(context)
                val dialogView = layoutInflater.inflate(R.layout.dialog_recycler_view, null)
                builder.setView(dialogView)
                val recycler = dialogView.findViewById<RecyclerView>(R.id.dialog_recyclerView)
                val ad = builder.create()

                recycler.adapter = partiAdapter
                recycler.layoutManager = LinearLayoutManager(context)
                ad.show()
            }
        })
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        crew_page_yes_crew_refreshLayout.setOnRefreshListener(this)

        //checkUserCrew()
        if(isInCrew){
            showInCrew()
            setCrewGatheringList()
        }else{
            showNoCrew()
            setCrewCardList()
        }

        crew_page_add_crew_btn.setOnClickListener {
            val addCrewIntent = Intent(context, AddCrewActivity::class.java)
            startActivityForResult(addCrewIntent, REQUEST_ADD_CREW)
        }


    }

    override fun onRefresh() {
        setCrewGatheringList()
        crew_page_yes_crew_refreshLayout.isRefreshing = false
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                REQUEST_SEARCH_CREW -> {
                    checkUserCrew()
                }
                REQUEST_ADD_CREW -> {
                    checkUserCrew()
                }
                REQUEST_ADD_GATHERING -> {
                    setCrewGatheringList()
                }
            }
        }
    }
}
