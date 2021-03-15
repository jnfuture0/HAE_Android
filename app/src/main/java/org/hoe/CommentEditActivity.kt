package org.hoe

import android.app.Activity
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_comment_edit.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.hoe.Json.ResultString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentEditActivity : AppCompatActivity() {

    val server = LoginActivity.server
    val token = MainActivity.token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_edit)

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("저장 중입니다.")
        progressDialog.setCancelable(false)

        val comment = intent.getStringExtra("comment")
        val commentId = intent.getIntExtra("comment_id",0)
        val isFeedComment = intent.getBooleanExtra("isFeed", true)

        comment_edit_write_editText.setText(comment)

        comment_edit_send_btn.setOnClickListener {
            progressDialog.show()
            val newComment = comment_edit_write_editText.text.toString()
            val commentR: RequestBody = RequestBody.create(MultipartBody.FORM, newComment)
            if(isFeedComment){
                server?.modifyFeedComment(token, commentId, commentR)?.enqueue(object: Callback<ResultString>{
                    override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("MODIFY_FEED_COMMENT_ERR", t.toString())
                        progressDialog.dismiss() }
                    override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                        Log.e("CHECK_DATA", "$commentId, ${response.body()}")
                        Toast.makeText(applicationContext, "수정되었습니다", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                })
            }else{
                server?.modifyGatheringComment(token, commentId.toString(), commentR)?.enqueue(object:Callback<ResultString>{
                    override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("MODIFY_G_COMMENT_ERR", t.toString())
                        progressDialog.dismiss() }
                    override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                        Log.e("CHECK_DATA", "$commentId, ${response.body()}")
                        Toast.makeText(applicationContext, "수정되었습니다", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                })
            }
        }

    }


}