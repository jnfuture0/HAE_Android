package org.hoe

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.hoe.Json.RetrofitService
import org.hoe.Json.Token
import org.hoe.Json.User4
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    companion object{
        val url = "http://4be0a7aaef12.ngrok.io"
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val server: RetrofitService? = retrofit.create(RetrofitService::class.java)
    }

    fun setBtn(){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("로그인 중입니다.")
        progressDialog.setCancelable(false)

        login_page_login_btn.setOnClickListener {
            val id = login_page_id.text.toString()
            val pw = login_page_pw.text.toString()
            //TODO : 마지막에 아디비번 세팅 바꾸기
            progressDialog.show()

            server?.login(id, pw)?.enqueue(object :Callback<Token>{
                override fun onFailure(call: Call<Token>, t: Throwable) {
                    Toast.makeText(applicationContext, "Login_Error : $t", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
                override fun onResponse(call: Call<Token>, response: Response<Token>) {
                    val resTok = response.body()?.token
                    if(resTok!=null){
                        //Toast.makeText(applicationContext, resTok.toString(), Toast.LENGTH_SHORT).show()
                        server.getUser(resTok).enqueue(object:Callback<User4>{
                            override fun onFailure(call: Call<User4>, t: Throwable) { Log.e("GET_UESR_ERR", t.toString()) }
                            override fun onResponse(call: Call<User4>, response: Response<User4>) {
                                val userID = response.body()?.ID!!
                                val hasCrew = response.body()?.crew_ID != 1
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                intent.putExtra("token", resTok.toString())
                                intent.putExtra("userID", userID)
                                intent.putExtra("hasCrew", hasCrew)
                                finish()
                                startActivity(intent)
                            }
                        })
                    }else{
                        Toast.makeText(applicationContext, "ID, PW를 확인해주세요", Toast.LENGTH_SHORT).show()
                    }
                    progressDialog.dismiss()
                }

            })


        }

        login_page_sign_up_btn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        /*login_page_pw.setOnKeyListener(object:View.OnKeyListener{
            override fun onKey(p0: View?, p1: Int, p2: KeyEvent?): Boolean {
                if((p2?.action == KeyEvent.ACTION_DOWN) && p1 == KeyEvent.KEYCODE_ENTER){
                    login_page_login_btn.performClick()
                    return true
                }
                return false
            }
        })*/

        setBtn()

        /*val ssb :SpannableStringBuilder = SpannableStringBuilder(resources.getText(R.string.hae_horizontal))
        ssb.setSpan(ForegroundColorSpan(resources.getColor(R.color.colorPrimary)),0,1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(resources.getColor(R.color.colorPrimary)),4,5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(resources.getColor(R.color.colorPrimary)),10,11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        login_page_hae_textView.text = ssb*/


    }
}