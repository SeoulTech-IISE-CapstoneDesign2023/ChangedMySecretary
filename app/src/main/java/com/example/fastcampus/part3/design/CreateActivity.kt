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
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.fastcampus.part3.design.databinding.ActivityCreateBinding
import com.example.fastcampus.part3.design.databinding.FragmentMapBinding
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateActivity : AppCompatActivity(), OnMapReadyCallback {

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
    private var isStart = true
    private var startPlace = ""
    private var arrivalPlace = ""

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
                } else {
                    arrivalEditText.setText(name)
                    arrivalMarker.apply {
                        position = LatLng(lat, lng)
                        captionText = "도착지"
                        iconTintColor = Color.BLUE
                        map = naverMap
                    }
                    arrivalPlace = name
                }
            }
            searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mapBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(
                binding.searchBottomSheetLayout.searchEditText.windowToken,
                0
            )
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
            amTextView.setOnClickListener {
                amChecked = true
                if (amChecked) {
                    it.background = AppCompatResources.getDrawable(
                        this@CreateActivity,
                        R.drawable.am_pm_background
                    )
                    binding.dateBottomSheetLayout.pmTextView.background = null
                }

            }
            pmTextView.setOnClickListener {
                amChecked = false
                if (!amChecked) {
                    it.background = AppCompatResources.getDrawable(
                        this@CreateActivity,
                        R.drawable.am_pm_background
                    )
                    binding.dateBottomSheetLayout.amTextView.background = null
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
                    Log.e("result", "$name $address")
                    locationAdapter.submitList(response.body()?.searchPoiInfo?.pois?.poi)
                }

                override fun onFailure(call: Call<Location>, t: Throwable) {
                    t.printStackTrace()
                }

            })
    }
}