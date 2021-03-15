package org.hoe.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.form_activity_list.view.*
import kotlinx.android.synthetic.main.form_activity_list_crew_info.view.*
import kotlinx.android.synthetic.main.form_activity_list_crew_quit.view.*
import org.hoe.LoginActivity
import org.hoe.R
import org.hoe.Struct.GatheringCard
import java.text.DateFormat
import java.text.SimpleDateFormat

class RecyclerAdapterCrewActivityCard (val context: Context, val boardList: MutableList<GatheringCard>, val isHead:Boolean, val itemClick:(item:GatheringCard, type:Int, ImageView?, TextView?, LinearLayout?, LinearLayout?) -> Unit): RecyclerView.Adapter<RecyclerAdapterCrewActivityCard.BaseViewHolder<*>>() {

    val url = LoginActivity.url
    val dateFormat : DateFormat = SimpleDateFormat("yyyy-MM-dd  HH시 mm분")
    val iso8601Format : DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")



    override fun getItemViewType(position: Int): Int {
        return when(position){
            0 -> 0
            boardList.size-1 -> 2
            else -> 1
        }
    }
    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : BaseViewHolder<*>{
        return when(viewType){
            0 -> {
                val view = LayoutInflater.from(context).inflate(R.layout.form_activity_list_crew_info, parent, false)
                InfoViewHolder(view)
            }
            1 -> {
                val view = LayoutInflater.from(context).inflate(R.layout.form_activity_list, parent, false)
                MainViewHolder(view)
            }
            2 -> {
                val view = LayoutInflater.from(context).inflate(R.layout.form_activity_list_crew_quit, parent, false)
                QuitViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = boardList.size

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = boardList[position]
        when(holder){
            is MainViewHolder -> holder.bind(element)
            is InfoViewHolder -> holder.bind(element)
            is QuitViewHolder -> holder.bind(element)
            else -> throw IllegalArgumentException()
        }
    }

    inner class MainViewHolder(view:View):BaseViewHolder<GatheringCard>(view) {
        val participation = itemView.form_activity_participation
        val joinBtn = itemView.form_activity_join_btn
        val leaveBtn = itemView.form_activity_leave_btn
        val showParticipants = itemView.form_activity_show_participants_btn
        val showComments = itemView.form_activity_comment_btn
        val participantsNum = itemView.form_activity_participants_num
        val title = itemView.form_activity_title
        val time = itemView.form_activity_time
        val deleteBtn = itemView.form_activity_delete_btn

        override fun bind(board: GatheringCard){
            //Glide.with(context).load(board.img).into(userImg)
            participation.clipToOutline = true
            if(board.isParticipation) {
                participation.setBackgroundResource(R.drawable.style_radius_5_primary)
                joinBtn.visibility = View.GONE
                leaveBtn.visibility = View.VISIBLE
            }else{
                participation.setBackgroundResource(R.drawable.style_radius_5_lightgray)
                joinBtn.visibility = View.VISIBLE
                leaveBtn.visibility = View.GONE
            }
            if(board.isMine){
                deleteBtn.visibility = View.VISIBLE
                deleteBtn.setOnClickListener {
                    itemClick(board, 6, null, null, null, null)
                }
            }else{
                deleteBtn.visibility = View.GONE
            }
            title.text = board.title
            val date = iso8601Format.parse(board.date)
            time.text = dateFormat.format(date)
            showParticipants.setOnClickListener {
                itemClick(board, 0, participation, participantsNum, null, null)
            }
            showComments.setOnClickListener {
                itemClick(board, 1, participation, participantsNum, null, null)
            }
            joinBtn.setOnClickListener {
                itemClick(board, 2, participation, participantsNum, joinBtn, leaveBtn)
            }
            leaveBtn.setOnClickListener {
                itemClick(board,3, participation, participantsNum, joinBtn, leaveBtn)
            }
            participantsNum.text = if(board.memberCount == null){
                "0"
            }else{
                board.memberCount.toString()
            }
        }
    }


    inner class InfoViewHolder(view:View):BaseViewHolder<GatheringCard>(view){
        val crewImg = itemView.crew_page_info_card_img
        val title = itemView.crew_page_info_card_title
        val crewDes = itemView.crew_page_info_card_content
        val memCount = itemView.crew_page_info_card_memberCount
        val gatheringCount = itemView.crew_page_info_card_activityCount
        val addGatheringBtn = itemView.crew_page_info_card_add_gathering_btn

        override fun bind(item: GatheringCard) {
            //don't care data class name
            Glide.with(context).load(url+"/yolov5/yolov5/inference/media/"+item.temp).into(crewImg)
            memCount.text = item.gathering_id.toString()
            gatheringCount.text = if(item.memberCount == null){
                "0"
            }else{
                item.memberCount.toString()
            }

            addGatheringBtn.setOnClickListener {
                Log.e("ADD_GATHERING","in")
                itemClick(item, 7, null, null, null, null)
            }
            title.text = item.title
            crewDes.text = item.date
        }
    }


    inner class QuitViewHolder(view:View):BaseViewHolder<GatheringCard>(view){
        val quitBtn = itemView.form_activity_crew_quit_btn
        override fun bind(item: GatheringCard) {
            if(isHead){
                quitBtn.text = "크루 삭제하기"
                quitBtn.setOnClickListener {
                    itemClick(item, 5, null, null, null, null)
                }
            }else{
                quitBtn.setOnClickListener {
                    itemClick(item, 4, null, null, null, null)
                }
            }

        }
    }

}