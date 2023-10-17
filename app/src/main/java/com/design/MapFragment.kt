package com.design

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.design.adapter.FavoriteListAdapter
import com.design.databinding.FragmentMapBinding
import com.design.model.importance.Importance
import com.design.model.importance.ImportanceProvider
import com.design.model.location.Location
import com.design.model.location.LocationAdapter
import com.design.model.location.LocationProvider
import com.design.util.FirebaseUtil
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
import java.util.Random

class MapFragment : Fragment(), OnMapReadyCallback, LocationProvider.Callback, ImportanceProvider.Callback {
    private var binding: FragmentMapBinding? = null
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var locationAdapter: LocationAdapter
    private lateinit var favoriteAdapter : FavoriteListAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val locationProvider = LocationProvider(this)
    private val importanceProvider = ImportanceProvider(this)
    private val random = Random()


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

            val favoriteBottomBehavior = BottomSheetBehavior.from(binding.favoriteBottomSheetLayout.root)

            initBottomSheet(binding)
            initAdapter(binding)
            binding.favoriteBottomSheetLayout.favoriteRecyclerView.adapter = favoriteAdapter
            binding.searchBottomSheetyLayout.searchRecyclerView.adapter = locationAdapter
            importanceProvider.getImportanceData()
            binding.searchBottomSheetyLayout.searchEditText.addTextChangedListener {
                val runnable = Runnable {
                    locationProvider.searchLocation(it.toString())
                }
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 300)
            }
            initChip(binding)
        }

    }

    private fun initChip(binding: FragmentMapBinding) {
        val favoriteBottomBehavior = BottomSheetBehavior.from(binding.favoriteBottomSheetLayout.root)
        binding.chip.setOnClickListener {
            FirebaseUtil.importanceDataBase.get()
                .addOnSuccessListener {
                    val data = it.value as Map<String,Any>
                    val dataList = data.values.toList()
                    val importanceList = dataList.map { item ->
                        val itemData = item as Map<String, Any>
                        Importance(
                            endY = itemData["endY"] as Double? ?: 0.0,
                            endX = itemData["endX"] as Double? ?: 0.0,
                            place = itemData["place"] as String,
                            title = itemData["title"] as String,
                            todoId = itemData["todoId"] as String
                        )
                    }
                    importanceList.forEach { data ->
                        if(data.endY != 0.0){
                            val red = random.nextInt(256) // 0부터 255 사이의 랜덤 값
                            val green = random.nextInt(256)
                            val blue = random.nextInt(256)
                            Marker().apply {
                                position = LatLng(data.endY, data.endX)
                                captionText = data.title
                                iconTintColor = Color.rgb(red, green, blue)//random한 색깔
                                map = naverMap
                            }
                        }
                    }
                    val lastData = importanceList.findLast { importance -> importance.endY != 0.0 }
                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(lastData?.endY ?: 0.0, lastData?.endX ?: 0.0))
                    cameraUpdate.animate(CameraAnimation.Fly, 500)
                    naverMap.moveCamera(cameraUpdate)
                    favoriteBottomBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }

        }
    }

    private fun initBottomSheet(binding: FragmentMapBinding) {
        val searchBottomBehavior = BottomSheetBehavior.from(binding.searchBottomSheetyLayout.root)
        val favoriteBottomBehavior = BottomSheetBehavior.from(binding.favoriteBottomSheetLayout.root)
        searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        favoriteBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.searchButton.setOnClickListener {
            //완전 펴져있으면은 접어버림 아니면은 닫아버림
            if (searchBottomBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                searchBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                favoriteBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            } else {
                searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                favoriteBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

    }

    private fun initAdapter(binding: FragmentMapBinding) {
        val favoriteBottomBehavior = BottomSheetBehavior.from(binding.favoriteBottomSheetLayout.root)
        locationAdapter = LocationAdapter {
            val name = it.name
            val lat = it.frontLat.toDouble()
            val lng = it.frontLon.toDouble()
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
            cameraUpdate.animate(CameraAnimation.Fly, 500)
            naverMap.moveCamera(cameraUpdate)
            naverMap.minZoom = 5.0
            naverMap.maxZoom = 18.0
            val bottomBehavior = BottomSheetBehavior.from(binding.searchBottomSheetyLayout.root)
            bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val imm =
                requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(
                binding.searchBottomSheetyLayout.searchEditText.windowToken,
                0
            )
        }

        favoriteAdapter = FavoriteListAdapter { data ->
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(data.endY ?: 0.0, data?.endX ?: 0.0))
            cameraUpdate.animate(CameraAnimation.Fly, 500)
            naverMap.moveCamera(cameraUpdate)
            favoriteBottomBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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

    override fun loadLocation(data: Location) {
        locationAdapter.submitList(data.searchPoiInfo?.pois?.poi)
    }

    override fun loadImportanceList(list: List<Importance>) {
        favoriteAdapter.submitList(list)
    }


}