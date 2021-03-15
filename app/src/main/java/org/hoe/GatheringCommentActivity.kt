package org.hoe

import android.app.Activity
import android.app.AlertDialog
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
import org.hoe.Json.GatheringComment7
import org.hoe.Json.ResultString
import org.hoe.Struct.Comment8
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GatheringCommentActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    val server = LoginActivity.server
    val token = MainActivity.token
    var gatheringID = 0
    val userID = MainActivity.userID

    val REQUEST_CODE_EDIT = 100

    fun setCommentList(gatheringID:Int){
        server?.getGatheringComment(gatheringID.toString())?.enqueue(object:Callback<List<GatheringComment7>>{
            override fun onFailure(call: Call<List<GatheringComment7>>, t: Throwable) { Log.e("GET_COMMENT_ERR", t.toString()) }
            override fun onResponse(call: Call<List<GatheringComment7>>, response: Response<List<GatheringComment7>>) {
                val commentList:MutableList<Comment8> = mutableListOf()
                val body = response.body()!!
                for(element in body){
                    val isMine = element.user_ID == userID

                    commentList.add(
                        Comment8(element.ID!!, element.comment!!, element.created_at!!, element.gathering_ID!!, element.user_ID!!, element.user_ID__name!!, element.user_ID__image, isMine)
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

    fun showEditDialog(gatheringCommentID:Int, comment:String){
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
            intent.putExtra("comment_id", gatheringCommentID)
            intent.putExtra("isFeed", false)
            startActivityForResult(intent, REQUEST_CODE_EDIT)
            ad.dismiss()
        }

        dialogDeleteBtn.setOnClickListener {
            server?.deleteGatheringComment(token, gatheringCommentID.toString())?.enqueue(object:Callback<ResultString>{
                override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("DELETE_FEED_COMMENT_ERR", t.toString()) }
                override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                    Toast.makeText(applicationContext, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                    setCommentList(gatheringID)
                }
            })
            ad.dismiss()
        }
    }

    fun setBtn(gatheringID: Int){
        comment_send_btn.setOnClickListener {
            val comment = comment_write_editText.text.toString()
            val commentR:RequestBody = RequestBody.create(MultipartBody.FORM, comment)

            server?.createGatheringComment(token, gatheringID.toString(), commentR)?.enqueue(object:Callback<ResultString>{
                override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("CREATE_FEED_COMMENT_ERR", t.toString()) }
                override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                    Log.e("CHECK_FEED_INFO", "${response.body()}")
                    Toast.makeText(applicationContext, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                    comment_write_editText.text.clear()
                    comment_write_editText.clearFocus()
                    setCommentList(gatheringID)
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        gatheringID = intent.getIntExtra("gathering_id", 0)



        comment_recyclerView.layoutManager = LinearLayoutManager(applicationContext)

        setCommentList(gatheringID)
        setBtn(gatheringID)

        comment_swipeRefreshLayout.setOnRefreshListener(this)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_EDIT -> {
                    setCommentList(gatheringID)
                }
            }
        }
    }

    override fun onRefresh() {
        setCommentList(gatheringID)
        comment_swipeRefreshLayout.isRefreshing = false
    }
}