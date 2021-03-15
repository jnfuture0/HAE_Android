package org.hoe.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.form_member_card.view.*
import org.hoe.LoginActivity
import org.hoe.R
import org.hoe.Struct.MemberCard

class RecyclerAdapterMemberList (val context: Context, val boardList: MutableList<MemberCard>, val itemClick:(MemberCard) -> Unit): RecyclerView.Adapter<RecyclerAdapterMemberList.BaseViewHolder<*>>() {

    val url = LoginActivity.url

    override fun getItemViewType(position: Int): Int {
        return when(position){
            0 -> 0
            else -> 0
        }
    }
    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : BaseViewHolder<*>{
        return when(viewType){
            0 -> {
                val view = LayoutInflater.from(context).inflate(R.layout.form_member_card, parent, false)
                MainViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = boardList.size

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = boardList[position]
        when(holder){
            is MainViewHolder -> holder.bind(element)
            else -> throw IllegalArgumentException()
        }
    }

    inner class MainViewHolder(view:View):BaseViewHolder<MemberCard>(view) {
        val img = itemView.form_member_card_img
        val name = itemView.form_member_card_name
        val imgOutLine = itemView.form_member_card_img_outline

        override fun bind(item: MemberCard){

            imgOutLine.apply {
                measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                clipToOutline = true
            }
            if(!item.img.isNullOrBlank()){
                Glide.with(context).load(url + item.img).into(img)
            }


            name.text = item.name
        }
    }


    /*inner class InfoViewHolder(view:View):BaseViewHolder<MemberCard>(view){
        override fun bind(item: MemberCard) {

        }
    }*/



}