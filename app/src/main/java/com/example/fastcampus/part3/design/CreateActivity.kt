package com.example.fastcampus.part3.design

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fastcampus.part3.design.model.route.PublicTransitRoute
import com.example.fastcampus.part3.design.model.route.SubPath
import com.example.fastcampus.part3.design.databinding.ActivityCreateBinding
import com.example.fastcampus.part3.design.model.location.Location
import com.example.fastcampus.part3.design.model.location.LocationAdapter
import com.example.fastcampus.part3.design.model.car.CarRouteProvider
import com.example.fastcampus.part3.design.model.car.CarRouteRequest
import com.example.fastcampus.part3.design.model.car.DepartureInfo
import com.example.fastcampus.part3.design.model.car.DestinationInfo
import com.example.fastcampus.part3.design.model.car.RoutesInfo
import com.example.fastcampus.part3.design.model.route.ResultInfo
import com.example.fastcampus.part3.design.model.route.RouteProvider
import com.example.fastcampus.part3.design.model.route.bus.realLocation.BusRealTimeLocationProvider
import com.example.fastcampus.part3.design.model.route.bus.realtime.BusRealTimeProvider
import com.example.fastcampus.part3.design.model.route.subway.SubwayTimeTableProvider
import com.example.fastcampus.part3.design.model.walk.Dto
import com.example.fastcampus.part3.design.model.walk.RouteData
import com.example.fastcampus.part3.design.model.walk.WalkingRouteProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.CountDownLatch

class CreateActivity : AppCompatActivity(), OnMapReadyCallback, WalkingRouteProvider.Callback,
    CarRouteProvider.Callback, RouteProvider.Callback {

    private lateinit var binding: ActivityCreateBinding

    private lateinit var dateBottomBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mapBottomBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var searchBottomBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    private lateinit var locationAdapter: LocationAdapter

    private val picker = MaterialTimePicker.Builder()
        .setTimeFormat(TimeFormat.CLOCK_12H)
        .setHour(12)
        .setMinute(59)
        .setTitleText("약속시간을 정하세요.")
        .build()

    private val startMarker = Marker()
    private val arrivalMarker = Marker()
    private var amChecked = false
    private val handler = Handler(Looper.getMainLooper())
    private val walkProvider = WalkingRouteProvider(this)
    private val carProvider = CarRouteProvider(this)
    private val routeProvider = RouteProvider(this)
    private val subwayTimeTableProvider = SubwayTimeTableProvider()
    private val busRealTimeLocationProvider = BusRealTimeLocationProvider()
    private val busRealTimeProvider = BusRealTimeProvider()
    private var isStart = true
    private var startPlace = ""
    private var arrivalPlace = ""
    private var startX = 0.0
    private var startY = 0.0
    private var endX = 0.0
    private var endY = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationSource = FusedLocationSource(this, MapFragment.LOCATION_PERMISSION_REQUEST_CODE)
        mapView = binding.mapBottomSheetLayout.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        initLocationAdapter(binding)
        initView()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {//권한 거부됨
                checkPermission()
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.locationSource = locationSource
        binding.mapBottomSheetLayout.locationButton.map = map
    }

    private fun checkPermission() {
        when {//permission이 되었을 때
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 허용됨
            }
            // permission을 거부가 되었을 때 다이얼로그로 한번더 확인
            shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showPermissionDialog()
            }

            else -> {
                requestLocationTrack()
            }
        }

    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("위치 정보를 가져오기 위해서는 위치추적 권한이 필요합니다")
            setNegativeButton("취소", null)
            setPositiveButton("동의") { _, _ ->
                requestLocationTrack()
            }
        }.show()
    }

    private fun requestLocationTrack() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            MapFragment.LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun initLocationAdapter(binding: ActivityCreateBinding) {
        //출발지 도착지 이름을 정해주고 마커를 찍어주자
        locationAdapter = LocationAdapter {
            val name = it.name
            val lat = it.frontLat.toDouble()
            val lng = it.frontLon.toDouble()
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
            cameraUpdate.animate(CameraAnimation.Fly, 500)
            naverMap.moveCamera(cameraUpdate)
            naverMap.minZoom = 5.0
            naverMap.maxZoom = 18.0
            binding.mapBottomSheetLayout.apply {
                if (isStart) {
                    startEditText.setText(name)
                    startMarker.apply {
                        position = LatLng(lat, lng)
                        captionText = "출발지"
                        iconTintColor = Color.MAGENTA
                        map = naverMap
                    }
                    startPlace = name
                    startX = lng
                    startY = lat
                } else {
                    arrivalEditText.setText(name)
                    arrivalMarker.apply {
                        position = LatLng(lat, lng)
                        captionText = "도착지"
                        iconTintColor = Color.BLUE
                        map = naverMap
                    }
                    arrivalPlace = name
                    endX = lng
                    endY = lat
                }
            }
            searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mapBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(
                binding.searchBottomSheetLayout.searchEditText.windowToken,
                0
            )
            binding.searchBottomSheetLayout.searchEditText.setText("")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        //키보드 통제
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        dateBottomBehavior = BottomSheetBehavior.from(binding.dateBottomSheetLayout.root)
        mapBottomBehavior = BottomSheetBehavior.from(binding.mapBottomSheetLayout.root)
        searchBottomBehavior = BottomSheetBehavior.from(binding.searchBottomSheetLayout.root)

        binding.dateTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        dateBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.layout.setOnTouchListener { _, _ ->
            imm.hideSoftInputFromWindow(binding.memoEditText.windowToken, 0)
            dateBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            false
        }

        if (startPlace.isEmpty() || arrivalPlace.isEmpty()) {
            binding.locationChip.text = "추억의 장소를 지정해주세요."
        }



        binding.dateTextView.setOnClickListener {
            //완전 펴져있으면은 접어버림 아니면은 닫아버림
            imm.hideSoftInputFromWindow(binding.memoEditText.windowToken, 0)
            mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            if (dateBottomBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                dateBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                dateBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
        binding.locationChip.setOnClickListener {
            dateBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            imm.hideSoftInputFromWindow(binding.memoEditText.windowToken, 0)
            if (mapBottomBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                mapBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }


        binding.memoEditText.apply {
            setOnClickListener {
                dateBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(text: Editable) {
                    if (text.isNotEmpty()) {
                        binding.createButton.background = AppCompatResources.getDrawable(
                            this@CreateActivity,
                            R.drawable.baseline_check_24
                        )
                    } else binding.createButton.background = AppCompatResources.getDrawable(
                        this@CreateActivity,
                        R.drawable.baseline_check_gray_24
                    )
                }

            })
            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    dateBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                } else {
                    imm.hideSoftInputFromWindow(binding.memoEditText.windowToken, 0)
                }
            }
        }

        binding.dateBottomSheetLayout.apply {
            hourText.isFocusable = false
            minuteText.isClickable = false
            timeLayout.setOnClickListener {
                picker.show(supportFragmentManager, "tag")
                picker.addOnPositiveButtonClickListener {
                    hourText.text = String.format("%02d", picker.hour)
                    minuteText.text = String.format("%02d", picker.minute)
                }
            }
        }

        binding.mapBottomSheetLayout.startEditText.isClickable = false
        binding.mapBottomSheetLayout.startEditText.isFocusable = false
        binding.mapBottomSheetLayout.arrivalEditText.isClickable = false
        binding.mapBottomSheetLayout.arrivalEditText.isFocusable = false

        binding.mapBottomSheetLayout.apply {
            startEditText.setOnClickListener {
                isStart = true
                mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                searchBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            arrivalEditText.setOnClickListener {
                isStart = false
                mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                searchBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            okButton.setOnClickListener {
                if (startPlace.isNotEmpty() && arrivalPlace.isNotEmpty()) {
                    binding.locationChip.text =
                        getString(R.string.start_to_arrive, startPlace, arrivalPlace)
                }
                mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            walkButton.setOnClickListener {
                if (checkPlace()) return@setOnClickListener
                Toast.makeText(this@CreateActivity, "이동수단 : 걷기", Toast.LENGTH_SHORT).show()
                val body = RouteData(
                    startX = startX,
                    startY = startY,
                    endX = endX,
                    endY = endY,
                    startName = "%EC%B6%9C%EB%B0%9C%EC%A7%80",
                    endName = "%EB%8F%84%EC%B0%A9%EC%A7%80",
                    searchOption = 4
                )
                walkProvider.getWalkingRoot(body)
            }
            carButton.setOnClickListener {
                if (checkPlace()) return@setOnClickListener
                Toast.makeText(this@CreateActivity, "이동수단 : 자동차", Toast.LENGTH_SHORT).show()
                val startTime = binding.dateTextView.text.toString()
                val isoDateTime = convertToISODateTime(startTime)
                val body = CarRouteRequest(
                    routesInfo = RoutesInfo(
                        departure = DepartureInfo(
                            name = "출발지",
                            lon = startX.toString(),
                            lat = startY.toString()
                        ),
                        destination = DestinationInfo(
                            name = "도착지",
                            lon = endX.toString(),
                            lat = endY.toString()
                        ),
                        predictionType = "departure",
                        predictionTime = "$isoDateTime+0900"
                    )
                )
                carProvider.getCarRoot(body)
            }
            publicTransportationButton.setOnClickListener {
                if (checkPlace()) return@setOnClickListener
                Toast.makeText(this@CreateActivity, "이동수단 : 대중교통", Toast.LENGTH_SHORT).show()
                routeProvider.getRoute(startX, startY, endX, endY)
            }
        }

        binding.searchBottomSheetLayout.apply {
            searchRecyclerView.adapter = locationAdapter
            searchEditText.addTextChangedListener {
                val runnable = Runnable {
                    searchLocation(it.toString())
                }
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 300)
            }
        }
    }

    private fun convertToISODateTime(date: String): String {
        val inputFormat =
            SimpleDateFormat("yyyy년 M월 d일(EEE), HH:mm", Locale.getDefault()) // 입력 형식 지정
        val outputFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) // 출력 형식 지정

        try {
            val date = inputFormat.parse(date) // 문자열을 Date 객체로 변환
            return outputFormat.format(date) // ISO 8601 형식으로 변환한 문자열 반환
        } catch (e: Exception) {
            return "날짜 형식이 올바르지 않습니다."
        }
    }

    private fun checkPlace(): Boolean {
        if (startPlace.isEmpty() || arrivalPlace.isEmpty()) {
            Toast.makeText(
                this@CreateActivity,
                "출발장소와 도착장소를 둘다 지정해주세요.",
                Toast.LENGTH_SHORT
            ).show()
            return true
        }
        return false
    }

    private fun searchLocation(searchKeyWord: String) {
        LocationRetrofitManager.searchLocationService.getLocation(
            "1",
            searchKeyWord,
            "WGS84GEO",
            "WGS84GEO",
            200
        )
            .enqueue(object : Callback<Location> {
                override fun onResponse(call: Call<Location>, response: Response<Location>) {
                    val name = response.body()?.searchPoiInfo?.pois?.poi?.map { it.name }
                    val address =
                        response.body()?.searchPoiInfo?.pois?.poi?.map { it.newAddressList.newAddress.map { it.fullAddressRoad } }
                    locationAdapter.submitList(response.body()?.searchPoiInfo?.pois?.poi)
                }

                override fun onFailure(call: Call<Location>, t: Throwable) {
                    t.printStackTrace()
                }

            })
    }

    private fun formatTotalTime(totalTime: Int): String {
        val hours = (totalTime / 60) / 60
        val minutes = (totalTime / 60) % 60
        return if (hours != 0) "약 $hours 시간 $minutes 분" else "약 $minutes 분"
    }

    private fun parseDateTime(dateTime: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
        val zonedDateTime = ZonedDateTime.parse(dateTime, formatter)
            .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
        return zonedDateTime.format(DateTimeFormatter.ofPattern("MM월 dd일 HH시 mm분"))
    }

    override fun loadWalkingRoot(data: Dto) {
        binding.mapBottomSheetLayout.recyclerView.isVisible = false
        val result =
            data.features?.filter { it.properties.index == 0 }?.map { it.properties }?.first()
                ?: return
        val totalTime = formatTotalTime(result.totalTime)
        binding.mapBottomSheetLayout.resultTextView.apply {
            isVisible = true
            text =
                "총 소요시간 : $totalTime\n총 거리 : ${DecimalFormat("###,###").format(result.totalDistance)}m"
        }
    }

    override fun loadCarRoot(data: com.example.fastcampus.part3.design.model.car.Dto) {
        binding.mapBottomSheetLayout.recyclerView.isVisible = false
        val result = data.features?.map { it.properties }?.firstOrNull() ?: return
        val totalTime = formatTotalTime(result.totalTime)
        val departure = parseDateTime(result.departureTime)
        val arrival = parseDateTime(result.arrivalTime)
        val totalFare =
            if (result.totalFare > 0) DecimalFormat("###,###").format(result.totalFare) else "통행요금 없음"
        val taxiFare = DecimalFormat("###,###").format(result.taxiFare) + "원"
        binding.mapBottomSheetLayout.resultTextView.apply {
            isVisible = true
            text =
                "총 소요시간 : $totalTime\n총 톨게이트비 : $totalFare\n총 택시비 : $taxiFare\n출발시간 : $departure\n도착 예정시간 : $arrival"
        }
    }

    override fun loadRoute(data: PublicTransitRoute?) {
        //제일 최단 길정보를 가져옴
        Thread {
            try {
                val minTimePath = data?.result?.path?.minByOrNull { it.info.totalTime }
                var minSubPathList = mutableListOf<SubPath>()
                minTimePath?.subPath?.forEach { subPath ->
                    if (subPath.sectionTime == 0 && subPath.trafficType == 3) {

                    } else minSubPathList.add(subPath)
                }
                //가져온 정보를 정제 info가 최종 정제 데이터
                val info = mutableListOf<ResultInfo>()
                val countDownLatch = CountDownLatch(minSubPathList.size)
                for (item in minSubPathList) {
                    //데이터를 정제해서 넣어주기
                    val innerCountDownLatch = CountDownLatch(1)
                    trafficTypeCase(item) { data ->
                        info.add(data)
                        innerCountDownLatch.countDown()
                    }
                    innerCountDownLatch.await()
                    countDownLatch.countDown()
                }
                countDownLatch.await()
                info.forEach { Log.e("aa", it.toString()) }
                //맨처음 도착지점 같은 경우 두번째 리스트에 있는 것으로 설정
                for (i in 0 until info.size) {
                    if (i > 0 && i < info.size - 1 && info[i].endName == null) {
                        info[i].endName = info[i + 1].startName
                    }
                    if (i > 0 && info[i].startName == null) {
                        info[i].startName = info[i - 1].endName
                    }
                }

                info[0].startName = "출발지"
                info[info.size - 1].endName = "도착지"
                info[0].endName = info[1].startName
                info[info.size - 1].startName = info[info.size - 2].endName
                runOnUiThread {
                    val routeAdapter = RouteAdapter(info)
                    binding.mapBottomSheetLayout.resultTextView.visibility = View.INVISIBLE
                    binding.mapBottomSheetLayout.recyclerView.apply {
                        isVisible = true
                        adapter = routeAdapter
                        val dividerItemDecoration =
                            DividerItemDecoration(this@CreateActivity, LinearLayoutManager.VERTICAL)
                        addItemDecoration(dividerItemDecoration)
                    }
                }


            } catch (e: Exception) {
                Toast.makeText(this, "거리가 너무 가깝습니다.(700m이내)", Toast.LENGTH_SHORT).show()
            }
        }.start()

    }

    private fun trafficTypeCase(subPath: SubPath, callback: (ResultInfo) -> Unit) {
        val trafficType = subPath.trafficType //1-지하철, 2-버스, 3-도보
        var startName: String? = null
        var endName: String? = null
        var sectionTime: Int?
        var lane: Int? = null
        var busno: String? = null
        var subwayCode: String? = null // 지하철 코드
        var wayCode: Int? = null // 1.상행 2. 하행
        var busId: Int? = null
        when (trafficType) {
            //지하철일때
            1 -> {
                startName = subPath.startName
                endName = subPath.endName
                sectionTime = subPath.sectionTime
                subwayCode = subPath.startID.toString()
                wayCode = subPath.wayCode
                lane = subPath.lane.map { it.subwayCode }.firstOrNull()
                //비동기작업수행
                subwayTimeTableProvider.getSubwayTimeTable(subwayCode, wayCode) { latestTime ->
                    val info = ResultInfo(
                        trafficType,
                        startName,
                        endName,
                        sectionTime,
                        lane,
                        busno,
                        subwayCode,
                        wayCode,
                        latestTime,
                        busId
                    )
                    callback(info)
                }
            }
            //버스일때
            2 -> {
                startName = subPath.startName
                endName = subPath.endName
                busno = subPath.lane.map { it.busNo }.firstOrNull()
                sectionTime = subPath.sectionTime
                subwayCode = subPath.startID.toString()
                wayCode = subPath.wayCode
                busId = subPath.lane.map { it.busID }.firstOrNull()
                // busID를 입력하게 되면 routeID를 얻게 된다.
                busRealTimeLocationProvider.getBusRealTimeLocation(busId!!) { routeId ->
                    busRealTimeProvider.getBusRealTime(subwayCode.toInt(), routeId){
                        val info = ResultInfo(
                            trafficType,
                            startName,
                            endName,
                            sectionTime,
                            lane,
                            busno,
                            subwayCode,
                            wayCode,
                            it,
                            busId
                        )
                        callback(info)
                    }
                } //여기서 routeid를 업데이트해주게됨
            }
            //도보일때
            3 -> {
                sectionTime = subPath.sectionTime
                val info = ResultInfo(
                    trafficType,
                    startName,
                    endName,
                    sectionTime,
                    lane,
                    busno,
                    subwayCode,
                    wayCode,
                    null,
                    busId
                )
                callback(info)
            }
        }
    }

}


