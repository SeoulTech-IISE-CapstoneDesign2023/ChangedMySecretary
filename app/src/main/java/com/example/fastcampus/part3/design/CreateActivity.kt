package com.example.fastcampus.part3.design

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fastcampus.part3.design.databinding.ActivityCreateBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource

class CreateActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCreateBinding

    private var amChecked = false

    private lateinit var dateBottomBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mapBottomBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationSource = FusedLocationSource(this, MapFragment.LOCATION_PERMISSION_REQUEST_CODE)
        mapView = binding.bottomSheetLayout2.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        //키보드 통제
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        binding.dateTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        binding.layout.setOnTouchListener { _, _ ->
            imm.hideSoftInputFromWindow(binding.memoEditText.windowToken, 0)
            false
        }

        dateBottomBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.root)
        mapBottomBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout2.root)

        dateBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.dateTextView.setOnClickListener {
            //완전 펴져있으면은 접어버림 아니면은 닫아버림
            mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            if (dateBottomBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                dateBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                imm.hideSoftInputFromWindow(binding.memoEditText.windowToken, 0)
            } else {
                dateBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
        binding.locationChip.setOnClickListener {
            dateBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            if (mapBottomBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                mapBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                imm.hideSoftInputFromWindow(binding.memoEditText.windowToken, 0)
            } else {
                mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }


        binding.memoEditText.setOnClickListener {
            dateBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mapBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        binding.memoEditText.addTextChangedListener(object : TextWatcher {
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
        binding.bottomSheetLayout.amTextView.setOnClickListener {
            amChecked = true
            if (amChecked) {
                it.background = AppCompatResources.getDrawable(this, R.drawable.am_pm_background)
                binding.bottomSheetLayout.pmTextView.background = null
            }

        }

        binding.bottomSheetLayout.pmTextView.setOnClickListener {
            amChecked = false
            if (!amChecked) {
                it.background = AppCompatResources.getDrawable(this, R.drawable.am_pm_background)
                binding.bottomSheetLayout.amTextView.background = null
            }
        }
        binding.bottomSheetLayout2.startEditText.isClickable = false
        binding.bottomSheetLayout2.startEditText.isFocusable = false
        binding.bottomSheetLayout2.arrivalEditText.isClickable = false
        binding.bottomSheetLayout2.arrivalEditText.isFocusable = false
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.locationSource = locationSource
        binding.bottomSheetLayout2.locationButton.map = map
    }
}