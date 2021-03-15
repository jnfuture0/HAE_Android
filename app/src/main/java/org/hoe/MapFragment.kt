package org.hoe

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_map.*
import net.daum.mf.map.api.MapCircle
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.hoe.Json.Equipment6
import org.hoe.Json.ResultString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
//import net.daum.mf.map.api.MapPOIItem
//import net.daum.mf.map.api.MapPoint
import kotlin.math.floor

class MapFragment : Fragment(), MapView.MapViewEventListener, MapView.POIItemEventListener {



    val GPS_ENABLE_REQUEST_CODE = 2001
    val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf<String>( Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    var latilongiMap : MutableMap<String,Array<Double>> = mutableMapOf()

    val server = LoginActivity.server
    val token = MainActivity.token

    var isInfoLayoutOpen = false
    var isSettingLayoutOpen = false
    lateinit var comeUpAnim : Animation
    lateinit var goDownAnim : Animation
    lateinit var goUpAnim : Animation
    lateinit var comeDownAnim : Animation
    lateinit var nowPOIItem : MapPOIItem
    var selectedEquipmentID = 0
    var selectedDistance = 5
    var selectedFilter = 0
    var mapCenterLatitude = 0.0
    var mapCenterLongitude = 0.0
    var markerList : MutableList<MapPOIItem> = mutableListOf()
    val url = LoginActivity.url
    val userID = MainActivity.userID


    var objectMap:MutableMap<String, Any> = mutableMapOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }


    fun setPermission(){
        var permissionCheck1 = ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
        var permissionCheck2= ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION)

        if(permissionCheck1 != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this.requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
            }else{
                ActivityCompat.requestPermissions(this.requireActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE )
            }
        }

        if(permissionCheck2 != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this.requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)){
            }else{
                ActivityCompat.requestPermissions(this.requireActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE )
            }
        }
    }




    lateinit var mapView :MapView



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mapView = MapView(activity)
        val mapViewContainer = map_view
        mapViewContainer.addView(mapView)

        //setPermission()

        circle500_radio_btn.isChecked = true

        //삭제 버튼
        map_info_delete_btn.setOnClickListener {
            val uObjectMap:MutableMap<String,Any> = nowPOIItem.userObject as MutableMap<String, Any>
            val equipID = uObjectMap["equipment_id"] as Int
            server?.deleteEquipment(token, equipID.toString())?.enqueue(object:Callback<ResultString>{
                override fun onFailure(call: Call<ResultString>, t: Throwable) { Log.e("DELETE_EQUIPMENT_ERR", t.toString()) }
                override fun onResponse(call: Call<ResultString>, response: Response<ResultString>) {
                    if(response.body()?.result.toString() == "success"){
                        Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        hideInfoLayout()
                        hideSettingLayout()
                    }
                }
            })
        }
        //길찾기 버튼
        map_info_find_route_btn.setOnClickListener {
            val permissionCheck1 = ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
            if(permissionCheck1 == PackageManager.PERMISSION_GRANTED ) {
                val lm: LocationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                try {
                    val userNowLocation: Location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    val uLatitude = userNowLocation.latitude
                    val uLongitude = userNowLocation.longitude

                    val mapLati = nowPOIItem.mapPoint.mapPointGeoCoord.latitude
                    val mapLong = nowPOIItem.mapPoint.mapPointGeoCoord.longitude

                    val url = "kakaomap://route?sp=${uLatitude},${uLongitude}&ep=${mapLati},${mapLong}&by=FOOT"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)

                }catch(e: java.lang.NullPointerException){
                    Log.e("LOCATION_ERROR", e.toString())
                    activity?.let{
                        ActivityCompat.finishAffinity(it)
                    }
                    activity?.let{
                        val intent = Intent(it, MainActivity::class.java)
                        it.startActivity(intent)
                    }
                    System.exit(0)
                }
            }else{
                Toast.makeText(context, "위치 권한이 없습니다.\n[설정 - 어플리케이션]에서 위치 권한을 켜주세요.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this.requireActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE )
            }
        }
        //세팅 버튼
        map_page_setting_btn.setOnClickListener {
            showSettingLayout()
        }
        //위치 버튼
        map_page_location_btn.setOnClickListener {
            var permissionCheck1 = ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
            var permissionCheck2= ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION)
            if(permissionCheck1 == PackageManager.PERMISSION_GRANTED && permissionCheck2 == PackageManager.PERMISSION_GRANTED) {
                var lm: LocationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                try {
                    if (mapView.currentLocationTrackingMode == MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving) {
                        mapView.currentLocationTrackingMode =
                            MapView.CurrentLocationTrackingMode.TrackingModeOff
                        mapView.setShowCurrentLocationMarker(false)
                        map_page_location_img.setImageResource(R.drawable.icon_my_location)
                    } else {
                        mapView.currentLocationTrackingMode =
                            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
                        mapView.setShowCurrentLocationMarker(true)
                        map_page_location_img.setImageResource(R.drawable.icon_my_location_primary)
                        var userNowLocation: Location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        var uLatitude = userNowLocation.latitude
                        var uLongitude = userNowLocation.longitude
                        val uNowPosition = MapPoint.mapPointWithGeoCoord(uLatitude, uLongitude)
                        mapView.setMapCenterPoint(uNowPosition, true)
                    }
                }catch(e:NullPointerException){
                    Log.e("LOCATION_ERROR", e.toString())
                    activity?.let{
                        val intent = Intent(it, MainActivity::class.java)
                        it.startActivity(intent)
                    }
                    System.exit(0)
                }
            }else{
                Toast.makeText(context, "위치 권한이 없습니다.\n[설정 - 어플리케이션 - HAE]에서 위치 권한을 켜주세요.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this.requireActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE )
            }
        }
        //마커 클릭 시
        mapView.setPOIItemEventListener(this)
        //맵 터치 시
        mapView.setMapViewEventListener(this)
        //마커 추가
        map_add_location_btn.setOnClickListener {

            val intent = Intent(context, AddMarkerActivity::class.java)
            intent.putExtra("latitude", mapView.mapCenterPoint.mapPointGeoCoord.latitude)
            intent.putExtra("longitude", mapView.mapCenterPoint.mapPointGeoCoord.longitude)
            startActivity(intent)

        }




        comeUpAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.come_up)
        goDownAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.go_down)
        goUpAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.go_up)
        comeDownAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.come_down)

        /*필터 스피너 세팅*/
        setSpinner(map_ex_type_spinner, R.array.ex_type_filter_array)



    }


    fun setEquipment(user_x:Double, user_y:Double){
        markerList= mutableListOf()
        objectMap= mutableMapOf()
        mapView.removeAllPOIItems()
        if(selectedFilter == 0){
            server?.getEquipment(user_x.toString(), user_y.toString(), selectedDistance.toString())?.enqueue(object:Callback<List<Equipment6>>{
                override fun onFailure(call: Call<List<Equipment6>>, t: Throwable) { Log.e("GET_EQUIP_ERR",t.toString()) }
                override fun onResponse(call: Call<List<Equipment6>>, response: Response<List<Equipment6>>) {
                    val body = response.body()
                    if(!body.isNullOrEmpty()){
                        for(element in body){
                            val marker = MapPOIItem()
                            val map = MapPoint.mapPointWithGeoCoord(element.location_X!!.toDouble(), element.location_Y!!.toDouble())
                            val category = when(element.category){
                                0 -> {
                                    "가슴운동"
                                }
                                1 -> {
                                    "평행봉"
                                }
                                2 -> {
                                    "철봉"
                                }
                                3 -> {
                                    "허리운동"
                                }
                                else -> {
                                    "기타"
                                }
                            }
                            marker.mapPoint = map
                            marker.itemName = category
                            objectMap["equipment_id"] = element.ID!!
                            objectMap["user_id"] = element.user_ID!!
                            objectMap["img"] = element.image.toString()
                            marker.userObject = objectMap
                            markerList.add(marker)
                            objectMap= mutableMapOf()
                        }
                        mapView.addPOIItems(markerList.toTypedArray())
                    }
                }
            })
        }else{
            server?.getEquipmentWithCategory(user_x.toString(), user_y.toString(), selectedDistance.toString(), (selectedFilter-1).toString())?.enqueue(object:Callback<List<Equipment6>>{
                override fun onFailure(call: Call<List<Equipment6>>, t: Throwable) { Log.e("GET_EQUIP_ERR",t.toString()) }
                override fun onResponse(call: Call<List<Equipment6>>, response: Response<List<Equipment6>>) {
                    val body = response.body()
                    if(!body.isNullOrEmpty()){
                        for(element in body){
                            val marker = MapPOIItem()
                            val map = MapPoint.mapPointWithGeoCoord(element.location_X!!.toDouble(), element.location_Y!!.toDouble())
                            val category = when(element.category){
                                0 -> {
                                    "가슴운동"
                                }
                                1 -> {
                                    "평행봉"
                                }
                                2 -> {
                                    "철봉"
                                }
                                3 -> {
                                    "허리운동"
                                }
                                else -> {
                                    "기타"
                                }
                            }
                            marker.mapPoint = map
                            marker.itemName = category
                            objectMap["equipment_id"] = element.ID!!
                            objectMap["user_id"] = element.user_ID!!
                            objectMap["img"] = element.image.toString()
                            marker.userObject = objectMap
                            markerList.add(marker)
                            objectMap= mutableMapOf()
                        }
                        mapView.addPOIItems(markerList.toTypedArray())
                    }
                }
            })
        }
    }




    fun setSpinner(spinner : Spinner, spinnerArray : Int){
        val items = resources.getStringArray(spinnerArray)
        val locationSpinnerAdapter = object: ArrayAdapter<String>(requireContext(), R.layout.form_spinner_text){
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
        locationSpinnerAdapter.add("전체")
        spinner.adapter = locationSpinnerAdapter
        spinner.setSelection(locationSpinnerAdapter.count)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if( position != items.size) {
                    selectedFilter = position

                    when {
                        circle200_radio_btn.isChecked -> {
                            selectedDistance = 2
                        }
                        circle500_radio_btn.isChecked -> {
                            selectedDistance = 5
                        }
                        circle1000_radio_btn.isChecked -> {
                            selectedDistance = 10
                        }
                    }
                    mapView.removeAllCircles()
                    val circle = MapCircle(mapView.mapCenterPoint, selectedDistance*100, Color.TRANSPARENT, Color.parseColor("#202196F3"))
                    mapView.addCircle(circle)

                    mapCenterLatitude = mapView.mapCenterPoint.mapPointGeoCoord?.latitude!!
                    mapCenterLongitude = mapView.mapCenterPoint.mapPointGeoCoord?.longitude!!
                    Log.e("CHECK_LATILONG", "$mapCenterLatitude, $mapCenterLongitude")
                    setEquipment(mapCenterLatitude, mapCenterLongitude)
                }
            }
        }
    }


    fun changeLayoutVisibility(layout :LinearLayout){
        if(layout.visibility == View.VISIBLE) layout.visibility = View.GONE
        else layout.visibility = View.VISIBLE
    }

    /*setMapViewEventListener*/
    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {}
    override fun onMapViewInitialized(p0: MapView?) {
        val circle = MapCircle(p0?.mapCenterPoint, 500, Color.TRANSPARENT, Color.parseColor("#202196F3"))
        p0?.addCircle(circle)
    }
    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {}
    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
        when {
            circle200_radio_btn.isChecked -> {
                selectedDistance = 2
            }
            circle500_radio_btn.isChecked -> {
                selectedDistance = 5
            }
            circle1000_radio_btn.isChecked -> {
                selectedDistance = 10
            }
        }
        p0?.removeAllCircles()
        val circle = MapCircle(p0?.mapCenterPoint, selectedDistance*100, Color.TRANSPARENT, Color.parseColor("#202196F3"))
        p0?.addCircle(circle)

        mapCenterLatitude = p1?.mapPointGeoCoord?.latitude!!
        mapCenterLongitude = p1?.mapPointGeoCoord?.longitude!!
        Log.e("CHECK_LATILONG", "$mapCenterLatitude, $mapCenterLongitude")
        setEquipment(mapCenterLatitude, mapCenterLongitude)
    }
    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {}
    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
    }
    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
        hideInfoLayout()
        hideSettingLayout()
    }
    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {}
    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {}

    /*setPOIItemEventListener*/
    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {}
    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?, p2: MapPOIItem.CalloutBalloonButtonType?) {}
    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {}
    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        map_info_title.text = p1?.itemName.toString()
        val uObjectMap:MutableMap<String,Any> = p1?.userObject as MutableMap<String, Any>

        selectedEquipmentID = uObjectMap["equipment_id"] as Int
        val isMine = userID == uObjectMap["user_id"]

        showInfoLayout(isMine)
        map_info_img.setBackgroundResource(R.drawable.img_ready)
        Glide.with(requireContext()).load(url+ uObjectMap["img"].toString()).into(map_info_img)

        val lat = floor(p1.mapPoint.mapPointGeoCoord.latitude * 1000000) / 1000000
        val long = floor(p1.mapPoint.mapPointGeoCoord.longitude * 1000000) / 1000000
        map_info_latitude.text = "위도 : $lat"
        map_info_longitude.text = "경도 : $long"

        nowPOIItem = p1
        nowPOIItem.userObject = p1.userObject ////????
    }

    private fun showInfoLayout(isMine:Boolean){
        if(isMine){
            map_info_delete_btn.visibility = View.VISIBLE
        }else{
            map_info_delete_btn.visibility = View.GONE
        }

        if(!isInfoLayoutOpen) {
            map_info_layout.visibility = View.VISIBLE
            map_info_layout.startAnimation(comeUpAnim)
            isInfoLayoutOpen = true
        }
    }

    private fun hideInfoLayout(){
        if(isInfoLayoutOpen){
            map_info_layout.visibility = View.GONE
            map_info_layout.startAnimation(goDownAnim)
            isInfoLayoutOpen = false
        }
    }

    private fun showSettingLayout(){
        if(!isSettingLayoutOpen){
            map_page_setting_layout.visibility = View.VISIBLE
            map_page_setting_layout.startAnimation(comeDownAnim)
            isSettingLayoutOpen = true
        }
    }

    private fun hideSettingLayout(){
        if(isSettingLayoutOpen){
            map_page_setting_layout.visibility = View.GONE
            map_page_setting_layout.startAnimation(goUpAnim)
            isSettingLayoutOpen = false
        }
    }

}

