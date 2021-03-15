package org.hoe

import android.Manifest
import android.app.Activity
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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_add_crew.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.hoe.Json.ResultString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Exception

class AddCrewActivity : AppCompatActivity() {

    val REQUEST_GET_IMAGE = 200
    val PERMISSIONS_REQUEST_CODE = 105
    var REQUIRED_PERMISSIONS = arrayOf<String>( Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    lateinit var uri : Uri
    val server = LoginActivity.server
    val token = MainActivity.token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_crew)

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("저장 중입니다.")
        progressDialog.setCancelable(false)

        add_crew_add_img_btn.setOnClickListener {
            setPermission()
        }

        add_crew_upload_btn.setOnClickListener {
            if(checkFinishable()){
                progressDialog.show()
                val nameString = add_crew_title_editText.text.toString()
                val name:RequestBody = RequestBody.create(MultipartBody.FORM, nameString)

                val desString = add_crew_des_editText.text.toString()
                val des: RequestBody = RequestBody.create(MultipartBody.FORM, desString)

                val imgRequestFile = if(add_crew_img.visibility == View.VISIBLE){
                    val file = File(getPath2020(applicationContext, uri))
                    val reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    MultipartBody.Part.createFormData("image", file.name, reqFile)
                }else{
                    null
                }

                server?.createCrew(token, name, des, imgRequestFile)?.enqueue(object:Callback<ResultString>{
                    override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("CREATE_CREW_ERR", t.toString())
                        progressDialog.dismiss() }
                    override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                        Toast.makeText(applicationContext, "생성되었습니다", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                })

            }else{
                Toast.makeText(applicationContext, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkFinishable():Boolean{
        return !(add_crew_title_editText.text.isBlank() || add_crew_des_editText.text.isBlank())
    }

    fun getPath2020(context: Context, uri:Uri):String{
        var fileName = "unknown"
        var filePathUri:Uri = uri
        if(uri.scheme.toString().compareTo("content") == 0){
            val cursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
            if(cursor.moveToFirst()) {
                var column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePathUri = Uri.parse(cursor.getString(column_index))
                fileName = filePathUri.lastPathSegment.toString()
            }
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
                        add_crew_img.setImageURI(uri)
                    }catch (e: Exception){}
                    if(add_crew_img.visibility == View.GONE) {
                        add_crew_img.visibility = View.VISIBLE
                        add_crew_add_img_btn.text = "이미지 변경"
                    }
                }
            }
        }
    }
}