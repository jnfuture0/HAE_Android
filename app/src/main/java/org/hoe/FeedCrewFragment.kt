package org.hoe

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_feed_inner.*
import org.hoe.Adapter.RecyclerAdapterFeed
import org.hoe.Json.Feed9
import org.hoe.Json.ResultString
import org.hoe.Json.User4
import org.hoe.Struct.FeedStruct
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedCrewFragment() : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    val token = MainActivity.token
    val server = LoginActivity.server
    val userID = MainActivity.userID
    val REQUEST_CODE_EDIT = 100
    //val mContext = context


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feed_inner, container, false)
    }

    fun newInstance():FeedCrewFragment{
        return FeedCrewFragment()
    }

    var feedList:MutableList<FeedStruct> = mutableListOf()


    fun setFeedList(){
        server?.getCrewFeedList(token)?.enqueue(object:Callback<List<Feed9>>{
            override fun onFailure(call: Call<List<Feed9>>, t: Throwable) {
                feed_list_loading_text.visibility = View.GONE
                Log.e("GET_FEED_LIST_ERR", t.toString())
            }
            override fun onResponse(call: Call<List<Feed9>>, response: Response<List<Feed9>>) {
                val body = response.body()!!

                feedList= mutableListOf()

                for(element in body){
                    val isMine = element.user_ID == userID

                    feedList.add(
                        FeedStruct(element.ID!!, element.des!!, element.start_time!!, element.end_time!!, element.image!!, element.user_ID!!, element.user_ID__name!!, element.user_ID__image, isMine)
                    )
                }

                val recycAdapter = RecyclerAdapterFeed(requireContext(), feedList){ board, type ->
                    when(type){
                        0 -> {
                            showEditDialog(board.feed_id, board.des)
                        }
                        1 -> {
                            val feedEditIntent = Intent(requireContext(), FeedCommentActivity::class.java)
                            feedEditIntent.putExtra("feed_id", board.feed_id)
                            startActivity(feedEditIntent)
                        }
                    }
                }

                feed_fragment_recyclerView.adapter = recycAdapter
                feed_fragment_recyclerView.layoutManager = LinearLayoutManager(context)

                feed_list_loading_text.visibility = View.GONE
            }

        })
    }


    fun showEditDialog(feedId:Int, des:String){
        val builder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_ask_edit_delete, null)
        val dialogEditBtn = dialogView.findViewById<Button>(R.id.dialog_edit_btn)
        val dialogDeleteBtn = dialogView.findViewById<Button>(R.id.dialog_delete_btn)

        builder.setView(dialogView)
        val ad : AlertDialog = builder.create()
        ad.show()

        dialogEditBtn.setOnClickListener {
            val intent = Intent(context, AddFeedActivity::class.java)
            intent.putExtra("isEdit", true)
            intent.putExtra("des", des)
            intent.putExtra("feed_id", feedId)
            startActivityForResult(intent, REQUEST_CODE_EDIT)
            ad.dismiss()
        }

        dialogDeleteBtn.setOnClickListener {
            server?.deleteFeed(token, feedId)?.enqueue(object:Callback<ResultString>{
                override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("DELETE_FEED_ERR", t.toString()) }
                override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                    Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                    setFeedList()
                }
            })
            ad.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_EDIT -> {
                    setFeedList()
                }
            }
        }
    }

    fun setBtnAnim(){
        val downAnim = AnimationUtils.loadAnimation(context, R.anim.go_down)
        val upAnim = AnimationUtils.loadAnimation(context, R.anim.come_up)


        val p : Fragment = this.parentFragment as Fragment
        if(p.activity != null){
            val adBtn = p.activity!!.findViewById<ImageButton>(R.id.feed_add_btn)
            feed_fragment_recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if(dy > 50){
                        if(adBtn.visibility == View.VISIBLE) {
                            adBtn.startAnimation(downAnim)
                            adBtn.visibility = View.INVISIBLE
                        }
                    }else if (dy < -50){
                        if(adBtn.visibility == View.INVISIBLE) {
                            adBtn.visibility = View.VISIBLE
                            adBtn.startAnimation(upAnim)
                        }
                    }
                }
            } )
        }
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setBtnAnim()
        setFeedList()
        feed_fragment_swipeRefreshLayout.setOnRefreshListener(this)

    }

    override fun onResume() {
        super.onResume()
        setFeedList()
    }

    override fun onRefresh() {
        setFeedList()
        feed_fragment_swipeRefreshLayout.isRefreshing = false
    }
}
