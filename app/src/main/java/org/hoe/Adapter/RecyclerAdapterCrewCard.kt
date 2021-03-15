package org.hoe.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.form_crew_card.view.*
import kotlinx.android.synthetic.main.form_crew_card_search.view.*
import org.hoe.LoginActivity
import org.hoe.R
import org.hoe.Struct.CrewCard

class RecyclerAdapterCrewCard (val context: Context, val boardList: MutableList<CrewCard>, val itemClick:(CrewCard, Boolean) -> Unit): RecyclerView.Adapter<RecyclerAdapterCrewCard.BaseViewHolder<*>>() {

    val url = LoginActivity.url

    override fun getItemViewType(position: Int): Int {
        return if(boardList[position].crew_id == 0) {
            1
        }else {
            0
        }
    }
    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : BaseViewHolder<*> {
        return when(viewType){
            0 -> {
                val view = LayoutInflater.from(context).inflate(R.layout.form_crew_card, parent, false)
                MainViewHolder(view)
            }
            1 -> {
                val view = LayoutInflater.from(context).inflate(R.layout.form_crew_card_search, parent, false)
                SearchViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = boardList.size

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = boardList[position]
        when(holder){
            is MainViewHolder -> holder.bind(element)
            is SearchViewHolder -> holder.bind(element)
            else -> throw IllegalArgumentException()
        }
    }

    inner class MainViewHolder(view:View):BaseViewHolder<CrewCard>(view) {
        val img = itemView.form_crew_card_img
        val title = itemView.form_crew_card_title
        val member = itemView.form_crew_card_memberCount
        val layout = itemView.form_crew_card_layout

        override fun bind(board:CrewCard){
            //Glide.with(context).load(board.img).into(userImg)
            layout.apply {
                measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                clipToOutline = true
            }
            Glide.with(context).load(url+"/yolov5/yolov5/inference/media/"+board.img).into(img)
            title.text = board.title
            member.text = if(board.memberCount == null){
                "멤버 : 0명"
            }else{
                "멤버 : ${board.memberCount}명"
            }
            layout.setOnClickListener {
                itemClick(board, false)
            }
        }
    }

    inner class SearchViewHolder(view:View):BaseViewHolder<CrewCard>(view) {
        val layout = itemView.crew_card_search_btn

        override fun bind(board:CrewCard){
            layout.setOnClickListener {
                itemClick(board, true)
            }
        }
    }


}