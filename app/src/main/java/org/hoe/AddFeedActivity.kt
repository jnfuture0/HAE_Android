package org.hoe

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_add_feed.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.hoe.Json.ResultString
import java.io.File
import java.lang.Exception
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AddFeedActivity : AppCompatActivity() {

    val REQUEST_GET_IMAGE = 200
    val PERMISSIONS_REQUEST_CODE = 105
    var REQUIRED_PERMISSIONS = arrayOf<String>( Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    lateinit var uri :Uri
    val server = LoginActivity.server
    val token = MainActivity.token
    val simpleDateFormat : DateFormat = SimpleDateFormat("MM-dd")

    var stHour = -1
    var stMin = -1
    var enHour = -1
    var enMin = -1

    var setDate : Date = Date()
    var isDateSet = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_feed)

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("저장 중입니다.")
        progressDialog.setCancelable(false)


        if(intent.getBooleanExtra("isEdit", false)){
            val oldDes = intent.getStringExtra("des")
            val feedID = intent.getIntExtra("feed_id", 0)
            add_feed_editText.setText(oldDes)
            add_feed_title.text = "피드 수정"
            add_feed_btn.text = "수정"

            add_feed_btn.setOnClickListener {
                if(!checkFinishable()){
                    Toast.makeText(this, "시간 설정을 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
                else{
                    if(!checkStEnTime()){
                        Toast.makeText(this, "시작 시간은 종료 시간보다 빨라야 합니다.", Toast.LENGTH_SHORT).show()
                    }else{
                        add_feed_btn.isEnabled = false
                        progressDialog.show()
                        val desString = add_feed_editText.text.toString()
                        val des: RequestBody = RequestBody.create(MultipartBody.FORM, desString)
                        val st = "${setDate.year}-${simpleDateFormat.format(setDate)} $stHour:$stMin"
                        val en = "${setDate.year}-${simpleDateFormat.format(setDate)} $enHour:$enMin"
                        val startTime: RequestBody = RequestBody.create(MultipartBody.FORM, st)
                        val endTime: RequestBody = RequestBody.create(MultipartBody.FORM, en)


                        val imgRequestFile = if(add_feed_img.visibility == View.VISIBLE){
                            val file = File(getPath2020(applicationContext, uri))
                            val reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                            MultipartBody.Part.createFormData("image", file.name, reqFile)
                        }else{
                            null
                        }

                        server?.modifyFeed(token, feedID ,des, startTime, endTime, imgRequestFile)?.enqueue(object:Callback<ResultString>{
                            override fun onFailure(call: Call<ResultString>, t: Throwable) {
                                add_feed_btn.isEnabled = true
                                progressDialog.dismiss()
                                Toast.makeText(applicationContext, "ERR", Toast.LENGTH_SHORT).show()
                            }
                            override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                Log.e("CHECK_CREATE_FEED", response.body().toString())
                                Toast.makeText(applicationContext, "수정되었습니다.", Toast.LENGTH_SHORT).show()
                                add_feed_btn.isEnabled = true
                                progressDialog.dismiss()
                                finish()
                            }
                        })
                    }
                }
            }
        }else{
            add_feed_btn.setOnClickListener {
                if(!checkFinishable()){
                    Toast.makeText(this, "시간 설정을 확인해주세요.", Toast.LENGTH_SHORT).show()
                }else{
                    if(!checkStEnTime()){
                        Toast.makeText(this, "시작 시간은 종료 시간보다 빨라야 합니다.", Toast.LENGTH_SHORT).show()
                    }else{
                        add_feed_btn.isEnabled = false
                        progressDialog.show()
                        val desString = add_feed_editText.text.toString()
                        val des: RequestBody = RequestBody.create(MultipartBody.FORM, desString)
                        val st = "${setDate.year}-${simpleDateFormat.format(setDate)}T$stHour:$stMin:00+09:00"
                        val en = "${setDate.year}-${simpleDateFormat.format(setDate)}T$enHour:$enMin:00+09:00"
                        val startTime: RequestBody = RequestBody.create(MultipartBody.FORM, st)
                        val endTime: RequestBody = RequestBody.create(MultipartBody.FORM, en)

                        val imgRequestFile = if(add_feed_img.visibility == View.VISIBLE){
                            val file = File(getPath2020(applicationContext, uri))
                            val reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                            MultipartBody.Part.createFormData("image", file.name, reqFile)
                        }else{
                            null
                        }

                        server?.createFeed(token, des, startTime, endTime, imgRequestFile)?.enqueue(object:Callback<ResultString>{
                            override fun onFailure(call: Call<ResultString>, t: Throwable) {
                                add_feed_btn.isEnabled = true
                                progressDialog.dismiss()
                                Toast.makeText(applicationContext, "ERR", Toast.LENGTH_SHORT).show()
                            }
                            override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                Toast.makeText(applicationContext, "추가되었습니다.", Toast.LENGTH_SHORT).show()
                                add_feed_btn.isEnabled = true
                                progressDialog.dismiss()
                                finish()
                            }
                        })
                    }
                }

            }
        }

        add_feed_date_set_btn.setOnClickListener {
            showDatePicker()
        }

        add_feed_st_time_set_btn.setOnClickListener {
            showTimePicker(true)
        }

        add_feed_en_time_set_btn.setOnClickListener {
            showTimePicker(false)
        }

        add_feed_add_img_btn.setOnClickListener {
            setPermission()
        }


    }

    fun checkFinishable():Boolean{
        return !(stHour == -1 || enHour == -1 || !isDateSet)
    }

    fun checkStEnTime():Boolean{
        return when {
            stHour < enHour -> { true }
            stHour == enHour -> { stMin <= enMin }
            else -> { false }
        }
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
            add_feed_date_text.text = "${datePicker.year}-${simpleDateFormat.format(setDate)}"
            Log.e("CHECK_DATE", "${datePicker.year}, ${datePicker.month}, ${datePicker.dayOfMonth}")
            Log.e("CHECK_DATE", "${setDate.year}, ${setDate.month}, ${setDate.date}")
            ad.dismiss()
        }

        dialogCancelBtn.setOnClickListener {
            ad.dismiss()
        }
    }

    fun showTimePicker(isStart:Boolean){
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.dialog_timePicker)
        val dialogSaveBtn = dialogView.findViewById<TextView>(R.id.dialog_timePicker_save_btn)
        val dialogCancelBtn = dialogView.findViewById<TextView>(R.id.dialog_timePicker_cancel_btn)


        builder.setView(dialogView)
        val ad : AlertDialog = builder.create()
        ad.show()

        dialogSaveBtn.setOnClickListener {
            if(isStart) {
                stHour = timePicker.hour
                stMin = timePicker.minute
                add_feed_st_time_text.text = "${stHour}시 ${stMin}분"
            }else{
                enHour = timePicker.hour
                enMin = timePicker.minute
                add_feed_en_time_text.text = "${enHour}시 ${enMin}분"
            }
            ad.dismiss()
        }

        dialogCancelBtn.setOnClickListener {
            ad.dismiss()
        }
    }

    fun getPath2020(context:Context, uri:Uri):String{
        var fileName = "unknown"
        var filePathUri:Uri = uri
        if(uri.scheme.toString().compareTo("content") == 0){
            val cursor:Cursor = context.contentResolver.query(uri, null, null, null, null)!!
            if(cursor.moveToFirst()) {
                var columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePathUri = Uri.parse(cursor.getString(columnIndex))
                fileName = filePathUri.lastPathSegment.toString()
            }
            cursor.close()
        }else if(uri.scheme?.compareTo("file") == 0){
            fileName = filePathUri.lastPathSegment.toString()
        }else{
            fileName = fileName+"_"+filePathUri.lastPathSegment
        }
        //return fileName
        return filePathUri.path.toString()
    }

    fun setPermission(){
        val permissionReadStore = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
        val permissionWriteStore = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)


        if(permissionReadStore != PackageManager.PERMISSION_GRANTED || permissionWriteStore != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            }else{
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE )
            }
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            }else{
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE )
            }
        }else{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GET_IMAGE)
        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                REQUEST_GET_IMAGE -> {
                    try{
                        uri = data?.data!!
                        add_feed_img.setImageURI(uri)
                    }catch (e:Exception){}
                    if(add_feed_img.visibility == View.GONE) {
                        add_feed_img.visibility = View.VISIBLE
                        add_feed_add_img_btn.text = "이미지 변경"
                    }
                }
            }
        }
    }
}