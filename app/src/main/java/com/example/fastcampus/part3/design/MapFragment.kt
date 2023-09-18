package com.example.fastcampus.part3.design

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.example.fastcampus.part3.design.databinding.FragmentMapBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

class MapFragment : Fragment(), OnMapReadyCallback {
    private var binding: FragmentMapBinding? = null
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var locationAdapter: LocationAdapter
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        return FragmentMapBinding.inflate(inflater, container, false).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { binding ->
            mapView = binding.mapView
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this)

            initBottomSheet(binding)
            initLocationAdapter(binding)
            binding.bottomSheetLayout.searchRecyclerView.adapter = locationAdapter
            binding.bottomSheetLayout.searchEditText.addTextChangedListener {
                val runnable = Runnable {
                    searchLocation(it.toString())
                }
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 300)
            }
            initChip(binding)
        }

    }

    private fun initChip(binding: FragmentMapBinding) {
        binding.chip.setOnClickListener {
            binding.loadingView.isVisible = true
        }
    }

    private fun initBottomSheet(binding: FragmentMapBinding) {
        val bottomBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.root)
        bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.searchButton.setOnClickListener {
            //완전 펴져있으면은 접어버림 아니면은 닫아버림
            if (bottomBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun initLocationAdapter(binding: FragmentMapBinding) {
        locationAdapter = LocationAdapter {
            val name = it.name
            val lat = it.frontLat.toDouble()
            val lng = it.frontLon.toDouble()
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
            cameraUpdate.animate(CameraAnimation.Fly, 500)
            naverMap.moveCamera(cameraUpdate)
            naverMap.minZoom = 5.0
            naverMap.maxZoom = 18.0
            val bottomBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.root)
            bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val imm =
                requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.bottomSheetLayout.searchEditText.windowToken, 0)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
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
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
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
        AlertDialog.Builder(context).apply {
            setMessage("위치 정보를 가져오기 위해서는 위치추적 권한이 필요합니다")
            setNegativeButton("취소", null)
            setPositiveButton("동의") { _, _ ->
                requestLocationTrack()
            }
        }.show()
    }

    private fun requestLocationTrack() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.locationSource = locationSource
        binding!!.locationButton.map = map
        //위치변화할때 내 위경도 데이터 업데이트
        map.addOnLocationChangeListener { location ->
            Log.e("myLocation", "${location.latitude},${location.longitude}")
        }
    }


}