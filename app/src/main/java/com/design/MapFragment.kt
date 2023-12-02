package com.design

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.design.adapter.AlarmListAdapter
import com.design.adapter.FriendNickNameListAdapter
import com.design.adapter.MemoryListAdapter
import com.design.alarm.NotificationReceiver
import com.design.databinding.FragmentMapBinding
import com.design.databinding.MemoryDialogBinding
import com.design.model.friend.Friend
import com.design.model.friend.FriendNickNameProvider
import com.design.model.location.Location
import com.design.model.location.LocationAdapter
import com.design.model.location.LocationProvider
import com.design.model.tag.Tag
import com.design.model.tag.TagProvider
import com.design.util.AlarmUtil
import com.design.util.FirebaseUtil
import com.design.util.Key
import com.design.view.MemoryDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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

class MapFragment : Fragment(), OnMapReadyCallback, LocationProvider.Callback,
    FriendNickNameProvider.Callback,
    TagProvider.Callback,
    MemoryDialog.DataTransferListener {
    private var binding: FragmentMapBinding? = null
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var locationAdapter: LocationAdapter
    private lateinit var memoryAdapter: MemoryListAdapter
    private lateinit var friendAdapter: FriendNickNameListAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val locationProvider = LocationProvider(this)
    private val friendNickNameProvider = FriendNickNameProvider(this)
    private val random = Random()
    private val markerList = mutableListOf<Marker>()
    private var friendUid: String =""
    private var friendNick: String =""


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
            initAdapter(binding)
            binding.memoryBottomSheetLayout.memoryRecyclerView.adapter = memoryAdapter
            binding.searchBottomSheetLayout.searchRecyclerView.adapter = locationAdapter
            binding.searchBottomSheetLayout.searchEditText.addTextChangedListener {
                val runnable = Runnable {
                    locationProvider.searchLocation(it.toString())
                }
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 300)
            }
            initChip(binding)
        }

    }

    override fun onDataTransfer(data: String) {
        Log.d("tag", "datatransfer get data is $data")
        getUidByNickname(data) { uid ->
            friendNick = data
            friendUid = uid.toString()
            Log.d("tag", "$friendUid")
            updateMemoryBottomSheetRecyclerView()
        }
    }
    override fun loadFriendNickNameList(list: MutableList<Friend>) {
        //이부분에서 정보가 넘어오게되면 여기서 recyclerview를 업데이트 해줘야한다
        friendAdapter.submitList(list)
        friendAdapter.notifyDataSetChanged()
    }
    override fun loadShareList(list: List<Tag>) {
        memoryAdapter.submitList(list)
    }

    private fun initChip(binding: FragmentMapBinding) {
        binding.chip.setOnClickListener {
            val MemoryDialogBinding = MemoryDialogBinding.inflate(layoutInflater)
            val dialog = MemoryDialog(MemoryDialogBinding, this)
            dialog.isCancelable = false
            dialog.show(requireFragmentManager(),"추억상자")
        }
    }

    private fun initBottomSheet(binding: FragmentMapBinding) {
        val searchBottomBehavior = BottomSheetBehavior.from(binding.searchBottomSheetLayout.root)
        val memoryBottomBehavior = BottomSheetBehavior.from(binding.memoryBottomSheetLayout.root)
        searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        memoryBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.searchButton.setOnClickListener {
            searchBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        binding.searchBottomSheetLayout.root.setOnClickListener {  }
        binding.memoryBottomSheetLayout.friendImageView.setOnClickListener {
            val MemoryDialogBinding = MemoryDialogBinding.inflate(layoutInflater)
            val dialog = MemoryDialog(MemoryDialogBinding, this)
            dialog.isCancelable = false
            dialog.show(requireFragmentManager(),"추억상자")
        }
    }

    private fun initAdapter(binding: FragmentMapBinding) {
        val memoryBottomSheetLayout = BottomSheetBehavior.from(binding.memoryBottomSheetLayout.root)

        locationAdapter = LocationAdapter {
            val name = it.name
            val lat = it.frontLat.toDouble()
            val lng = it.frontLon.toDouble()
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
            cameraUpdate.animate(CameraAnimation.Fly, 500)
            naverMap.moveCamera(cameraUpdate)
            naverMap.minZoom = 5.0
            naverMap.maxZoom = 18.0
            val bottomBehavior = BottomSheetBehavior.from(binding.searchBottomSheetLayout.root)
            bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val imm =
                requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(
                binding.searchBottomSheetLayout.searchEditText.windowToken,
                0
            )
        }
        memoryAdapter = MemoryListAdapter(
            onClick = { data ->
                if (data.endY != 0.0) {
                    val red = random.nextInt(256) // 0부터 255 사이의 랜덤 값
                    val green = random.nextInt(256)
                    val blue = random.nextInt(256)
                    Marker().apply {
                        position = LatLng(data.endY!!.toDouble(), data.endX!!.toDouble())
                        captionText = data.title!!
                        iconTintColor = Color.rgb(red, green, blue)//random한 색깔
                        map = naverMap
                        markerList.add(this)
                    }
                }
                val cameraUpdate = CameraUpdate.scrollTo(
                    LatLng(
                        data.endY?.toDouble() ?: 0.0,
                        data?.endX?.toDouble() ?: 0.0
                    )
                )
                cameraUpdate.animate(CameraAnimation.Fly, 500)
                naverMap.moveCamera(cameraUpdate)
                memoryBottomSheetLayout.state = BottomSheetBehavior.STATE_HIDDEN
            },
            onDeleteClick = { data ->
                //태그 데이터 삭제
                val tagId = data.tagId
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("태그 삭제")
                    .setMessage("태그를 삭제하시겠습니까?")
                    .setPositiveButton("예") { _, _ ->
                        deleteTag(tagId!!)
                    }
                    .setNegativeButton("아니요") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            })
    }

    private fun getUidByNickname(nickname: String, completion: (String?) -> Unit) {
        val usersRef = Firebase.database.reference.child(Key.DB_USERS)
        val query = usersRef.orderByChild("user_info/nickname").equalTo(nickname)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 일치하는 닉네임을 가진 사용자가 여러 명이 아닌 경우
                    for (userSnapshot in dataSnapshot.children) {
                        val userUid = userSnapshot.key // 사용자의 UID
                        completion(userUid)
                        return
                    }
                }
                // 닉네임과 일치하는 사용자를 찾을 수 없음
                completion(null)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // 에러 처리
                completion(null)
            }
        })
    }
    private fun updateMemoryBottomSheetRecyclerView() {
        val memoryBottomBehavior = BottomSheetBehavior.from(binding!!.memoryBottomSheetLayout.root)
        memoryBottomBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        FirebaseUtil.tagDataBase.child(friendUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val sharedTodos = mutableListOf<Tag>()
                    for (todoSnapshot in dataSnapshot.children) {
                        val tag = todoSnapshot.getValue(Tag::class.java)
                        // 해당 일정의 friendUid 리스트와 비교
                        if (tag != null) {
                            // friendUid가 원하는 값인 "0010qwer"와 일치하면 이 부분 실행
                            val shareItem = Tag(
                                tagId = tag.tagId,
                                todoId = tag.todoId,
                                title = tag.title,
                                date = tag.date,
                                place = tag.place,
                                endY = tag.endY,
                                endX = tag.endX,
                                usingShare = tag.usingShare,
                                friendUid = tag.friendUid,
                            )
                            sharedTodos.add(shareItem)
                        }
                    }
                    memoryAdapter.apply {
                        submitList(sharedTodos)
                        notifyDataSetChanged()
                        binding?.memoryBottomSheetLayout?.titleTextView?.text = "${friendNick}님과의 추억"
                        if(sharedTodos.isEmpty()){
                            binding?.memoryBottomSheetLayout?.emptyTextView?.text = "${friendNick}님과의 추억이 없습니다. \n 추억을 쌓아봐요"
                            binding?.memoryBottomSheetLayout?.emptyTextView?.visibility = View.VISIBLE
                        }
                        else{
                            binding?.memoryBottomSheetLayout?.emptyTextView?.visibility = View.GONE
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }
    private fun deleteTag(tagId: String) {
        FirebaseUtil.tagDataBase.child(friendUid).child(tagId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "추억을 삭제했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        updateMemoryBottomSheetRecyclerView()
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


}