package org.hoe

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_gathering.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.hoe.Json.ResultString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AddGatheringActivity : AppCompatActivity() {

    val server = LoginActivity.server
    val token = MainActivity.token
    val simpleDateFormat : DateFormat = SimpleDateFormat("MM-dd")

    var stHour = -1
    var stMin = -1
    var setDate : Date = Date()
    var isDateSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_gathering)

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("저장 중입니다.")
        progressDialog.setCancelable(false)

        add_gathering_date_set_btn.setOnClickListener {
            showDatePicker()
        }

        add_gathering_st_time_set_btn.setOnClickListener {
            showTimePicker()
        }

        add_gathering_btn.setOnClickListener {
            if(!checkFinishable()){
                Toast.makeText(this, "정보가 부족합니다", Toast.LENGTH_SHORT).show()
            }else{
                progressDialog.show()
                val desString = add_gathering_title_editText.text.toString()
                val des: RequestBody = RequestBody.create(MultipartBody.FORM, desString)
                val st = "${setDate.year}-${simpleDateFormat.format(setDate)}T$stHour:$stMin:00+09:00"
                val startTime: RequestBody = RequestBody.create(MultipartBody.FORM, st)

                server?.createGathering(token, des, startTime)?.enqueue(object: Callback<ResultString> {
                    override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("CREATE_GATHERING_ERR", t.toString())
                        progressDialog.dismiss()}
                    override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                        Toast.makeText(applicationContext, "추가되었습니다.", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                })
            }
        }
    }

    private fun checkFinishable():Boolean{
        return !(stHour == -1 || !isDateSet || add_gathering_title_editText.text.isBlank())
    }

    fun showDatePicker(){
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_date_picker, null)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.dialog_datePicker)
        val dialogSaveBtn = dialogView.findViewById<TextView>(R.id.dialog_datePicker_save_btn)
        val dialogCancelBtn = dialogView.findViewById<TextView>(R.id.dialog_datePicker_cancel_btn)

        builder.setView(dialogView)
        val ad : AlertDialog = builder.create()
        ad.show()

        dialogSaveBtn.setOnClickListener {
            setDate.year = datePicker.year
            setDate.month = datePicker.month //유저가 한거보다 1 적게 나옴
            setDate.date = datePicker.dayOfMonth
            isDateSet = true
            add_gathering_date_text.text = "${datePicker.year}-${simpleDateFormat.format(setDate)}"
            ad.dismiss()
        }

        dialogCancelBtn.setOnClickListener {
            ad.dismiss()
        }
    }

    fun showTimePicker(){
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.dialog_timePicker)
        val dialogSaveBtn = dialogView.findViewById<TextView>(R.id.dialog_timePicker_save_btn)
        val dialogCancelBtn = dialogView.findViewById<TextView>(R.id.dialog_timePicker_cancel_btn)


        builder.setView(dialogView)
        val ad : AlertDialog = builder.create()
        ad.show()

        dialogSaveBtn.setOnClickListener {
            stHour = timePicker.hour
            stMin = timePicker.minute
            add_gathering_st_time_text.text = "${stHour}시 ${stMin}분"
            ad.dismiss()
        }

        dialogCancelBtn.setOnClickListener {
            ad.dismiss()
        }
    }
}