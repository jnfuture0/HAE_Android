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
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_add_marker.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.hoe.Json.ResultInt
import org.hoe.Json.ResultString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddMarkerActivity : AppCompatActivity() {

    val REQUEST_GET_IMAGE = 105
    val PERMISSIONS_REQUEST_CODE = 100
    val REQUIRED_PERMISSIONS = arrayOf<String>( Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var EquipmentTypeArray = arrayOf<String>()
    lateinit var uri :Uri
    val server = LoginActivity.server
    val token = MainActivity.token

    //처음 100이였다가 유저가 이미지 선택하면 변경
    var userSelectedImgType = 100
    //실패할때마다 false되고 선택시 true
    var isFailedAndSelect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_marker)

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("저장 중입니다.")
        progressDialog.setCancelable(false)

        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        val items = resources.getStringArray(R.array.ex_type_filter_array_except_all)
        EquipmentTypeArray = items

        add_marker_page_Image_add_btn.setOnClickListener {
            requestPermission()
        }

        add_marker_page_Image.setOnClickListener {
            requestPermission()
        }

        add_marker_page_confirm_btn.setOnClickListener {
            if(checkFinishable()){
                if(checkIsWaitTextIsGONE()){
                    if(!checkIsFailedAndDontSelect()){
                        progressDialog.show()
                        val file = File(getPath2020(applicationContext, uri))
                        val reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                        val imgRequestFile = MultipartBody.Part.createFormData("image", file.name, reqFile)
                        server?.createEquipment(token, latitude.toString(), longitude.toString(), userSelectedImgType.toString(), imgRequestFile)?.enqueue(object:Callback<ResultString>{
                            override fun onFailure(call: Call<ResultString>, t: Throwable) {
                                progressDialog.dismiss()
                                finish()
                                Log.e("CREATE_EQUIPMENT_ERR", t.toString()) }
                            override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                                Toast.makeText(applicationContext, "추가되었습니다", Toast.LENGTH_SHORT).show()
                                progressDialog.dismiss()
                                finish()
                            }
                        })
                    }else{
                        Toast.makeText(applicationContext, "운동기구 종류를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(applicationContext, "잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(applicationContext, "사진을 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
            
        }

    }

    private fun checkFinishable():Boolean{
        return add_marker_page_Image.visibility == View.VISIBLE
    }

    private fun checkIsFailedAndDontSelect():Boolean{
        //true 리턴하려면 failed text가 보이고, ifFailed가 false -> 운동기구 선택 toast 메시지
        return add_marker_page_fail_text.visibility == View.VISIBLE && !isFailedAndSelect
    }

    private fun checkIsWaitTextIsGONE():Boolean{
        return add_marker_page_wait_text.visibility == View.GONE
    }

    private fun requestPermission(){
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

    fun setSpinner(spinner : Spinner, spinnerArray : Int){
        val items = resources.getStringArray(spinnerArray)
        val locationSpinnerAdapter = object: ArrayAdapter<String>(applicationContext, R.layout.form_spinner_text){
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                if(position == count) {
                    (v.findViewById<View>(R.id.tvItemSpinner) as TextView).text = ""
                    (v.findViewById<View>(R.id.tvItemSpinner) as TextView).hint = getItem(count)
                }
                return v
            }

            override fun getCount(): Int {
                return super.getCount()-1
            }
        }
        locationSpinnerAdapter.addAll(items.toMutableList())
        locationSpinnerAdapter.add("선택해주세요")
        spinner.adapter = locationSpinnerAdapter
        spinner.setSelection(locationSpinnerAdapter.count)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if( position != items.size) {
                    userSelectedImgType = position
                    isFailedAndSelect = true
                    /*when(position){
                        0 -> { userSelectedImgType = position}
                        else -> {}
                    }*/
                }
            }
        }
    }

    private fun showWait(){
        add_marker_page_wait_text.visibility = View.VISIBLE
        add_marker_page_fail_text.visibility = View.GONE
        add_marker_page_type_notice_layout.visibility = View.GONE
        add_marker_page_spinner.visibility = View.GONE
    }

    private fun showSuccess(){
        add_marker_page_wait_text.visibility = View.GONE
        add_marker_page_fail_text.visibility = View.GONE
        add_marker_page_type_notice_layout.visibility = View.VISIBLE
        add_marker_page_spinner.visibility = View.VISIBLE
        setSpinner(add_marker_page_spinner, R.array.ex_type_filter_array_except_all)
    }

    private fun showFailed(){
        add_marker_page_wait_text.visibility = View.GONE
        add_marker_page_fail_text.visibility = View.VISIBLE
        isFailedAndSelect = false
        add_marker_page_type_notice_layout.visibility = View.GONE
        add_marker_page_spinner.visibility = View.VISIBLE
        setSpinner(add_marker_page_spinner, R.array.ex_type_filter_array_except_all)
    }

    fun getPath2020(context: Context, uri: Uri):String{
        var fileName = "unknown"
        var filePathUri: Uri = uri
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                REQUEST_GET_IMAGE -> {
                    try{
                        uri = data?.data!!
                        add_marker_page_Image.setImageURI(uri)
                        if(add_marker_page_Image.visibility == View.GONE) {
                            add_marker_page_Image_add_btn.visibility = View.INVISIBLE
                            add_marker_page_Image.visibility = View.VISIBLE
                        }

                        showWait()

                        val file = File(getPath2020(applicationContext, uri))
                        val reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                        val imgRequestFile = MultipartBody.Part.createFormData("image", file.name, reqFile)
                        server?.judgeEquipment(imgRequestFile)?.enqueue(object:Callback<ResultInt>{
                            override fun onFailure(call: Call<ResultInt>, t: Throwable) {
                                showFailed()
                                Log.e("JUDGE_EQUIPMENT_ERR",t.toString())
                            }
                            override fun onResponse(call: Call<ResultInt>, response: Response<ResultInt>) {
                                if(response.isSuccessful){
                                    val type = response.body()?.result!!
                                    if(type == 1 || type == 2 || type == 3 || type == 0){
                                        showSuccess()
                                        userSelectedImgType = type
                                        add_marker_page_type_textView.text = EquipmentTypeArray[type]
                                        add_marker_page_type_textView2.text = EquipmentTypeArray[type]
                                    }else{
                                        showFailed()
                                    }
                                    
                                }
                            }

                        })
                    }catch (e:Exception){}
                }
            }
        }
    }
}