package org.hoe.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.form_comment.view.*
import org.hoe.LoginActivity
import org.hoe.R
import org.hoe.Struct.Comment8
import java.text.DateFormat
import java.text.SimpleDateFormat

class RecyclerAdapterComment (val context: Context, val boardList: MutableList<Comment8>, val itemClick:(Comment8) -> Unit): RecyclerView.Adapter<RecyclerAdapterComment.MainViewHolder>() {

    val iso8601Format : DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
    val dateFormat : DateFormat = SimpleDateFormat("yyyy-MM-dd  HH시 mm분")
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
            0 -> MainViewHolder(parent, itemClick, R.layout.form_comment)
            else -> MainViewHolder(parent, itemClick, R.layout.form_comment)
        }
    }

    override fun getItemCount(): Int = boardList.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder?.bind(boardList[position], context)
    }

    inner class MainViewHolder(parent: ViewGroup, itemClick:(Comment8) -> Unit, layout : Int) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(layout, parent, false)
    ) {
        val userImg = itemView.comment_userImg
        val userImgOutLine = itemView.comment_userImg_outLine
        val userName = itemView.comment_userName
        val content = itemView.comment_text
        val time = itemView.comment_time
        val threeDots = itemView.comment_three_dots

        fun bind(board:Comment8, context: Context){
            userImgOutLine.apply {
                measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                clipToOutline = true
            }
            if(board.isMine){
                threeDots.visibility = View.VISIBLE
                threeDots.setOnClickListener { itemClick(board) }
            }
            if(!board.user_img.isNullOrBlank()) {
                Glide.with(context).load(url+"/yolov5/yolov5/inference/media/"+board.user_img).into(userImg)
            }

            userName.text = board.user_name
            content.text = board.comment
            val created = board.created_at.substring(0,19)+"Z"
            val createdTime = iso8601Format.parse(created)
            time.text = dateFormat.format(createdTime)
        }
    }
}