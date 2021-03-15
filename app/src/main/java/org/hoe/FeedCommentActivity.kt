package org.hoe

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_comment.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.hoe.Adapter.RecyclerAdapterComment
import org.hoe.Json.FeedComment
import org.hoe.Json.ResultString
import org.hoe.Struct.Comment8
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedCommentActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    val server = LoginActivity.server
    val token = MainActivity.token
    var feedId = 0
    val userID = MainActivity.userID


    val REQUEST_CODE_EDIT = 100

    fun setCommentList(feedId:Int){
        server?.getFeedComment(feedId)?.enqueue(object:Callback<List<FeedComment>>{
            override fun onFailure(call: Call<List<FeedComment>>, t: Throwable) { Log.e("GET_COMMENT_ERR", t.toString()) }
            override fun onResponse(call: Call<List<FeedComment>>, response: Response<List<FeedComment>>) {
                val commentList:MutableList<Comment8> = mutableListOf()
                val body = response.body()!!
                for(element in body){
                    val isMine = element.user_ID == userID

                    commentList.add(
                        Comment8(element.ID!!, element.comment!!, element.created_at!!, element.feed_ID!!, element.user_ID!!, element.user_ID__name!!, element.user_ID__image, isMine)
                    )
                }

                commentList.reverse()

                val comAdapter = RecyclerAdapterComment(applicationContext, commentList){
                    showEditDialog(it.comment_id, it.comment)
                }
                comment_recyclerView.adapter = comAdapter
                comment_loading_text.visibility = View.GONE
            }

        })

    }

    fun showEditDialog(comment_id:Int, comment:String){
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_ask_edit_delete, null)
        val dialogEditBtn = dialogView.findViewById<Button>(R.id.dialog_edit_btn)
        val dialogDeleteBtn = dialogView.findViewById<Button>(R.id.dialog_delete_btn)

        builder.setView(dialogView)
        val ad : AlertDialog = builder.create()
        ad.show()

        dialogEditBtn.setOnClickListener {
            val intent = Intent(applicationContext, CommentEditActivity::class.java)
            intent.putExtra("comment", comment)
            intent.putExtra("comment_id", comment_id)
            intent.putExtra("isFeed", true)
            startActivityForResult(intent, REQUEST_CODE_EDIT)
            ad.dismiss()
        }

        dialogDeleteBtn.setOnClickListener {
            server?.deleteFeedComment(token, comment_id)?.enqueue(object:Callback<ResultString>{
                override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("DELETE_FEED_COMMENT_ERR", t.toString()) }
                override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                    Toast.makeText(applicationContext, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                    setCommentList(feedId)
                }
            })
            ad.dismiss()
        }
    }

    fun setBtn(feedId: Int, progressDialog:ProgressDialog){
        comment_send_btn.setOnClickListener {
            progressDialog.show()
            val comment = comment_write_editText.text.toString()
            val commentR:RequestBody = RequestBody.create(MultipartBody.FORM, comment)

            server?.createFeedComment(token, feedId, commentR)?.enqueue(object:Callback<ResultString>{
                override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("CREATE_FEED_COMMENT_ERR", t.toString()) }
                override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                    Log.e("CHECK_FEED_INFO", "${response.body()}")
                    Toast.makeText(applicationContext, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                    comment_write_editText.text.clear()
                    comment_write_editText.clearFocus()
                    progressDialog.dismiss()
                    setCommentList(feedId)
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        feedId = intent.getIntExtra("feed_id", 0)

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("저장 중입니다.")
        progressDialog.setCancelable(false)


        comment_recyclerView.layoutManager = LinearLayoutManager(applicationContext)

        setCommentList(feedId)
        setBtn(feedId, progressDialog)

        comment_swipeRefreshLayout.setOnRefreshListener(this)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_EDIT -> {
                    setCommentList(feedId)
                }
            }
        }
    }

    override fun onRefresh() {
        setCommentList(feedId)
        comment_swipeRefreshLayout.isRefreshing = false
    }
}