package org.hoe

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.android.material.tabs.TabLayout
import devlight.io.library.ntb.NavigationTabBar
import kotlinx.android.synthetic.main.activity_main.*
import org.hoe.Adapter.TabAdapterMain
import org.hoe.Json.RetrofitService
import org.hoe.Json.User4
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {

    private lateinit var backPressedForFinish: BackPressedForFinish

    companion object{
        lateinit var token :String
        var userID = 0
        var hasCrew = false
    }

    private val adapter by lazy{ TabAdapterMain(supportFragmentManager, applicationContext) }
    val server = LoginActivity.server

    fun setTabLayout(){
        vpMainActivity.adapter = adapter
        tabLayout.setupWithViewPager(vpMainActivity)
        tabLayout.getTabAt(0)?.setCustomView(R.layout.tablayout_icon_feed)
        tabLayout.getTabAt(1)?.setCustomView(R.layout.tablayout_icon_map)
        tabLayout.getTabAt(2)?.setCustomView(R.layout.tablayout_icon_crew)
        tabLayout.getTabAt(3)?.setCustomView(R.layout.tablayout_icon_profile)
    }

    /*fun setUserID(token:String){
        server?.getUser(token)?.enqueue(object:Callback<User4>{
            override fun onFailure(call: Call<User4>, t: Throwable) { Log.e("GET_UESR_ERR", t.toString()) }
            override fun onResponse(call: Call<User4>, response: Response<User4>) {
                userID = response.body()?.ID!!
            }

        })
    }*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        token = intent.getStringExtra("token").toString()
        userID = intent.getIntExtra("userID",0)
        hasCrew = intent.getBooleanExtra("hasCrew", false)
        setTabLayout()

        backPressedForFinish = BackPressedForFinish(this)


    }

    override fun onBackPressed() {
        backPressedForFinish.onBackPressed()
    }

    fun getHashKey(){
        var packageInfo : PackageInfo = PackageInfo()
        try {
            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        } catch (e: PackageManager.NameNotFoundException){
            e.printStackTrace()
        }

        for (signature: Signature in packageInfo.signatures){
            try{
                var md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KEY_HASH", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            } catch(e: NoSuchAlgorithmException){
                Log.e("KEY_HASH", "Unable to get MessageDigest. signature = " + signature, e)
            }
        }
    }
}