package org.hoe.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.form_feed.view.*
import org.hoe.LoginActivity
import org.hoe.R
import org.hoe.Struct.FeedStruct
import java.text.DateFormat
import java.text.SimpleDateFormat

class RecyclerAdapterFeed (val context: Context, val boardList: MutableList<FeedStruct>, val itemClick:(FeedStruct, Int) -> Unit): RecyclerView.Adapter<RecyclerAdapterFeed.MainViewHolder>() {

    val iso8601Format : DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
    val dateFormat : DateFormat = SimpleDateFormat("yyyy년 MM월 dd일")
    val timeFormat : DateFormat = SimpleDateFormat("HH:mm")
    val url = LoginActivity.url


    override fun getItemViewType(position: Int): Int {
        return when(position){
            0 -> 0
            else -> 0
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : MainViewHolder{
        return when(viewType){
            0 -> MainViewHolder(parent, itemClick, R.layout.form_feed)
            else -> MainViewHolder(parent, itemClick, R.layout.form_feed)
        }
    }

    override fun getItemCount(): Int = boardList.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder?.bind(boardList[position], context)
    }

    inner class MainViewHolder(parent: ViewGroup, itemClick:(FeedStruct, Int) -> Unit, layout : Int) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(layout, parent, false)
    ) {
        val des = itemView.feed_des
        val time = itemView.feed_time
        val date = itemView.feed_date
        val feedImg = itemView.feed_img
        val userImg = itemView.feed_userImg
        val userImgOutLine = itemView.feed_userImg_outLine
        val userName = itemView.feed_userName
        val commentBtn = itemView.feed_comment_button
        val threeDots = itemView.feed_three_dots

        fun bind(board:FeedStruct, context: Context){
            //피드이미지
            if(board.feed_img.isNullOrBlank()){
                feedImg.visibility = View.GONE
            }else{
                Glide.with(context).load(url+ "/yolov5/yolov5/inference/media/" + board.feed_img).into(feedImg)
            }
            des.text = board.des
            userImgOutLine.apply {
                measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                clipToOutline = true
            }
            if(!board.user_img.isNullOrBlank()){
                Glide.with(context).load(url+ "/yolov5/yolov5/inference/media/" +board.user_img).into(userImg)
            }

            userName.text = board.user_name
            val st = iso8601Format.parse(board.start_time)
            val en = iso8601Format.parse(board.end_time)
            date.text = dateFormat.format(st)
            time.text = "${timeFormat.format(st)} ~ ${timeFormat.format(en)}"

            if(board.isMine){
                threeDots.visibility = View.VISIBLE
                threeDots.setOnClickListener { itemClick(board, 0) }
            }

            commentBtn.setOnClickListener {
                itemClick(board, 1)
            }
        }
    }


}