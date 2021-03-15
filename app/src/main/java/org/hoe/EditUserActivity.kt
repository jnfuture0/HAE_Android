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
import kotlinx.android.synthetic.main.activity_edit_user.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.hoe.Json.ResultString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Exception

class EditUserActivity : AppCompatActivity() {

    val REQUEST_GET_IMAGE = 200
    val PERMISSIONS_REQUEST_CODE = 105
    var REQUIRED_PERMISSIONS = arrayOf<String>( Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var uri : Uri? = null
    val server = LoginActivity.server
    val token = MainActivity.token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("저장 중입니다.")
        progressDialog.setCancelable(false)

        edit_user_page_img_outLine.apply {
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            clipToOutline = true
        }

        edit_user_page_img.setOnClickListener {
            setPermission()
        }

        edit_user_page_confirm_btn.setOnClickListener {
            val nameString = edit_user_page_userName.text.toString()

            when {
                nameString.isBlank() -> {
                    Toast.makeText(applicationContext, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val name: RequestBody = RequestBody.create(MultipartBody.FORM, nameString)
                    val imgRequestFile = if(uri != null){
                        val file = File(getPath2020(applicationContext, uri!!))
                        val reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                        MultipartBody.Part.createFormData("image", file.name, reqFile)
                    }else{
                        null
                    }
                    edit_user_page_confirm_btn.isEnabled = false
                    progressDialog.show()
                    server?.modifyUser(token, name, imgRequestFile)?.enqueue(object:Callback<ResultString>{
                        override fun onFailure(call: Call<ResultString>, t: Throwable) {
                            Log.e("MODIFY_USER_ERR", t.toString())
                            progressDialog.dismiss()
                            edit_user_page_confirm_btn.isEnabled = true
                        }
                        override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                            Toast.makeText(this@EditUserActivity, "수정되었습니다", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                            edit_user_page_confirm_btn.isEnabled = true
                            finish()
                        }
                    })
                }
            }

        }
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
                        edit_user_page_img.setImageURI(uri)
                    }catch (e: Exception){}
                }
            }
        }
    }
}