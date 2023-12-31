package com.design

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieDrawable
import com.design.adapter.RouteAdapter
import com.design.databinding.ActivityCreateBinding
import com.design.databinding.SearchDialogBinding
import com.design.model.Todo
import com.design.model.Type
import com.design.model.User
import com.design.model.calendar.TodoDataProvider
import com.design.model.car.CarRouteProvider
import com.design.model.car.CarRouteRequest
import com.design.model.car.DepartureInfo
import com.design.model.car.DestinationInfo
import com.design.model.car.RoutesInfo
import com.design.model.location.Location
import com.design.model.location.LocationAdapter
import com.design.model.route.PublicTransitRoute
import com.design.model.route.ResultInfo
import com.design.model.route.RouteProvider
import com.design.model.route.SubPath
import com.design.model.route.bus.realLocation.BusRealTimeLocationProvider
import com.design.model.route.bus.realtime.BusRealTimeProvider
import com.design.model.route.subway.SubwayTimeTableProvider
import com.design.model.tag.Tag
import com.design.model.walk.Dto
import com.design.model.walk.RouteData
import com.design.model.walk.WalkingRouteProvider
import com.design.util.*
import com.design.util.Key.Companion.DB_CALENDAR
import com.design.util.Key.Companion.DB_TAG
import com.design.view.SearchDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
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
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch

class CreateActivity : AppCompatActivity(), OnMapReadyCallback, WalkingRouteProvider.Callback,
    CarRouteProvider.Callback, RouteProvider.Callback, TodoDataProvider.Callback {

    private lateinit var binding: ActivityCreateBinding

    private lateinit var searchBottomBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    private lateinit var locationAdapter: LocationAdapter

    private lateinit var backPressedCallback: OnBackPressedCallback

    // 알람 권한 요청
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            //알림권한 없음 -> 설정창으로 한번더 보내서 알림 권한 하라고 요청
            Toast.makeText(this, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
            finish()
        }
    }

    private val startMarker = Marker()
    private val arrivalMarker = Marker()
    private val handler = Handler(Looper.getMainLooper())
    private val walkProvider = WalkingRouteProvider(this)
    private val carProvider = CarRouteProvider(this)
    private val routeProvider = RouteProvider(this)
    private val subwayTimeTableProvider = SubwayTimeTableProvider()
    private val busRealTimeLocationProvider = BusRealTimeLocationProvider()
    private val busRealTimeProvider = BusRealTimeProvider()
    private val todoDataProvider = TodoDataProvider(this)
    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (EEE), HH:mm", Locale.KOREA)
    private val calendar = Calendar.getInstance()
    private val imm by lazy { getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }
    private var isStart = true
    private var startPlace = ""
    private var arrivalPlace = ""
    private var startX = 0.0
    private var startY = 0.0
    private var endX = 0.0
    private var endY = 0.0
    private var type: String? = null
    private var notificationId: String? = "0"
    private var oldNotificationId: String? = null
    private var alarmData = mutableMapOf<String, Any>()
    private lateinit var user: String
    private var todoKeys: ArrayList<String> = arrayListOf()
    private var timeString = ""
    private var dateString = ""
    private var title = ""
    private var date = ""
    private var time = ""
    private var startTime = ""
    private var todoId = ""
    private var tagId = ""
    private var isEditMode = false  // 편집 모드 여부를 나타내는 변수
    private var usingAlarm = false
    private var isTimeChange = false
    private var readyTime = ""
    private var myFriends = mutableListOf<User>()
    private var todoKey = ""
    private var tagKey = ""
    private var usingShare = false
    private var friendUids: ArrayList<String> = arrayListOf()
    private var getDate = ""
    private var selectedDate: Date? = null


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        locationSource = FusedLocationSource(this, MapFragment.LOCATION_PERMISSION_REQUEST_CODE)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        initLocationAdapter(binding)
        initView()
        AlarmUtil.askNotificationPermission(this, requestPermissionLauncher)
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
        binding.locationButton.map = map

        if (startX != 0.0) {
            startMarker.apply {
                position = LatLng(startY, startX)
                captionText = "출발지"
                icon = OverlayImage.fromResource(R.drawable.marker_yellow)
                width = 240
                height = 240
                this.map = naverMap
            }

            arrivalMarker.apply {
                position = LatLng(endY, endX)
                captionText = "도착지"
                icon = OverlayImage.fromResource(R.drawable.marker_red)
                width = 240
                height = 240
                this.map = naverMap
            }

            val cameraUpdate = CameraUpdate.scrollTo(LatLng(startY, startX))
            cameraUpdate.animate(CameraAnimation.Fly, 500)
            naverMap.moveCamera(cameraUpdate)
        }

    }

    override fun loadTodoData(data: Todo) {
        with(data) {
            this@CreateActivity.todoId = todoId
            this@CreateActivity.title = title
            this@CreateActivity.date = date
            this@CreateActivity.time = time
            this@CreateActivity.startPlace = startPlace.toString()
            this@CreateActivity.arrivalPlace = arrivePlace.toString()
            this@CreateActivity.notificationId = notificationId
            this@CreateActivity.readyTime = readyTime ?: ""
            //이부분에서 readyTime도 빼줘야한다.
            oldNotificationId = notificationId
            Log.e("old1", oldNotificationId.toString())
            //이부분에서 notificationId를 가져와 기존의 알람정보를 얻어올수 있음
            startX = startLng!!
            startY = startLat!!
            endX = arrivalLng!!
            endY = arrivalLat!!

            if (data.usingAlarm == true) {
                this@CreateActivity.usingAlarm = true
                binding.alarmImageView.setBackgroundResource(R.drawable.baseline_notifications_24)
            } else {
                this@CreateActivity.usingAlarm = false
                binding.alarmImageView.setBackgroundResource(R.drawable.baseline_notifications_off_24)
            }
        }
        isEditMode = true  // 기존 Todo를 수정하는 편집 모드임을 나타냄


        with(binding) {
            // 화면 초기화
            // Todo의 날짜 및 시간
            dateTextView.text = "$date, $time"
            // Todo의 제목
            titleEditText.setText(title)
            // 출발지와 도착지 이름 설정
            startEditText.setText(startPlace)
            arrivalEditText.setText(arrivalPlace)
        }
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
            binding.apply {
                if (isStart) {
                    startEditText.setText(name)
                    startMarker.apply {
                        position = LatLng(lat, lng)
                        captionText = "출발지"
                        icon = OverlayImage.fromResource(R.drawable.marker_yellow)
                        width = 240
                        height = 240
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
                        icon = OverlayImage.fromResource(R.drawable.marker_red)
                        width = 240
                        height = 240
                        map = naverMap
                    }
                    arrivalPlace = name
                    endX = lng
                    endY = lat
                }
            }
            searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            imm.hideSoftInputFromWindow(
                binding.searchBottomSheetLayout.searchEditText.windowToken,
                0
            )
            binding.searchBottomSheetLayout.searchEditText.setText("")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        // 뒤로가기 콜백 초기화
        backPressedCallback = object : OnBackPressedCallback(true) {
            var waitTime = 0L
            override fun handleOnBackPressed() {
                // 뒤로가기누르면 bottomsheet 내리기
                if (searchBottomBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }

                if (System.currentTimeMillis() - waitTime >= 1500) {
                    waitTime = System.currentTimeMillis()
                    Toast.makeText(
                        this@CreateActivity,
                        "뒤로가기 버튼을 한번 더 누르면 뒤로갑니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    finish()
                }

            }
        }
        // 뒤로가기 콜백 활성화
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        //list에서 일정 하나 선택했을 때 내용 수정
        val startDate = intent.getStringExtra("startDate")
        val todoKey = intent.getStringExtra("todoKey")
        // 기존의 Todo를 수정하는 경우, 일정 시작 날짜와 일정 키 값으로 provider 받아옴
        if (startDate != null && todoKey != null) {
            todoDataProvider.getTodoData(startDate, todoKey)
        }

        //키보드 통제
        searchBottomBehavior = BottomSheetBehavior.from(binding.searchBottomSheetLayout.root)

        searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.layout.setOnTouchListener { _, _ ->
            searchBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            imm.hideSoftInputFromWindow(
                binding.titleEditText.windowToken,
                0
            )
            false
        }

        binding.createButton.setOnClickListener {
            val todoKey = intent.getStringExtra("todoKey")
            val todo = intent.getParcelableExtra<Todo>("todo")
            // 추억 제목을 입력하지 않으면 일정 생성 불가 토스트
            if (binding.titleEditText.text.toString().isEmpty()) {
                binding.createButton.background = AppCompatResources.getDrawable(
                    this@CreateActivity, R.drawable.baseline_check_gray_24
                )
                Toast.makeText(this, "추억의 제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            // 제목이 30자가 넘어갈 경우 일정 생성 불가 토스트
            else if (binding.titleEditText.text.toString().length > 31) {
                binding.createButton.background = AppCompatResources.getDrawable(
                    this@CreateActivity, R.drawable.baseline_check_gray_24
                )
                Toast.makeText(this, "추억의 제목은 30자까지만 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                if (isEditMode) {
                    Log.d("create", "initailize edit mode")

                    if (notificationId == "0") {//애초에 알람을 설정하지 않았을때

                        notificationId = oldNotificationId
                        Log.e("bb", notificationId.toString())
                        updateTodo(todoKey!!, todo!!)
                    } else {//수정하러왔다가 그냥 다시 ok누를때
                        //만약 날짜만 바꿨다면?
                        if (isTimeChange) {
                            if (notificationId == oldNotificationId) {//출발 날짜가 변경되었는데 notificationid가 old와 동일하다면
                                ////근데 여기서 문제 단순히 날짜만 바꿔버리게 되면은 알람 시간도 바뀌게 되기에 한번 업데이트를 해달라고 요청을 하자
                                Toast.makeText(this, "길찾기를 한번 진행해주세요.", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }
                        }

                        if (usingAlarm) {
                            //여기서 길찾기를 경로를 업데이트안하고 okbutton만 누를경우가 있음 이때 alarmdata가 없다면 알람을 생성하지 않는다.
                            if (alarmData["appointmentTime"] == null) {
                                //기존의 todo를 수정
                                notificationId = "0"
                                updateTodo(todoKey!!, todo!!)
                            } else {
                                //기존의 alarm을 삭제
                                Log.e("old", oldNotificationId!!)
                                FirebaseUtil.alarmDataBase.child(oldNotificationId!!).removeValue()
                                AlarmUtil.deleteAlarm(oldNotificationId!!.toInt(), this)
                                //기존의 todo를 수정
                                updateTodo(todoKey!!, todo!!)
                                //새로운 알람 생성
                                setAlarm(calendar, ALARM, todoKey!!)
                                createAlarm()
                            }
                        } else {
                            //기존의 alarm을 삭제하고 새로운 알람은 만들지 않음
                            FirebaseUtil.alarmDataBase.child(oldNotificationId!!).removeValue()
                            AlarmUtil.deleteAlarm(oldNotificationId!!.toInt(), this)
                            //기존의 todo를 수정
                            notificationId = "0"
                            updateTodo(todoKey!!, todo!!)
                        }
                    }
                    finish()
                } else {
                    Log.d("create", "initailize create mode")
                    // 새로운 Todo를 생성하는 경우
                    createTodo()
                    createTag()
                    if (notificationId != "0") {//알람을 설정할때
                        if (usingAlarm) {
                            setAlarm(calendar, ALARM, this@CreateActivity.todoKey)
                            createAlarm()
                        } else {
                            notificationId = "0"
                        }
                    }
                    finish()
                }
            }
            true
        }

        binding.apply {
            titleEditText.setOnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                    // 엔터 키 눌렀을 때 실행할 동작
                    val inputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                    return@setOnKeyListener true
                }
                false
            }
            dateTextView.setOnClickListener {
                setDateAndTime()
            }
            alarmImageView.setOnClickListener {
                if (!usingAlarm) {
                    it.setBackgroundResource(R.drawable.baseline_notifications_24)
                    usingAlarm = true
                    Toast.makeText(this@CreateActivity, "알람설정 on", Toast.LENGTH_SHORT).show()
                    val picker = TimeUtil.openTimePickerForReadyTime(readyTime)
                    picker.addOnPositiveButtonClickListener {
                        readyTime = String.format("%02d:%02d", picker.hour, picker.minute)
                    }
                    picker.show(supportFragmentManager, "준비시간")
                } else {
                    it.setBackgroundResource(R.drawable.baseline_notifications_off_24)
                    usingAlarm = false
                    Toast.makeText(this@CreateActivity, "알람설정 off", Toast.LENGTH_SHORT).show()
                }
            }
            imageFriendView.setOnClickListener {
                val searchDialogBinding = SearchDialogBinding.inflate(layoutInflater)
                val dialog = SearchDialog(searchDialogBinding)
                dialog.isCancelable = false
                dialog.show(supportFragmentManager, "친구 태그")

            }
            startEditText.setOnClickListener {
                isStart = true
                searchBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            arrivalEditText.setOnClickListener {
                isStart = false
                searchBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            walkButton.setOnClickListener {

                walkButton.setImageResource(R.drawable.icons_walk_click)
                carButton.setImageResource(R.drawable.icons_car)
                publicTransportationButton.setImageResource(R.drawable.icons_bus)
                if (checkPlace()) return@setOnClickListener
                val startTime = binding.dateTextView.text.toString()
                if (startTime == "추억의 시간을 지정해주세요") {
                    Toast.makeText(this@CreateActivity, "시간을 먼저 정해주세요.", Toast.LENGTH_SHORT)
                        .show()
                    walkButton.setImageResource(R.drawable.icons_walk)
                    carButton.setImageResource(R.drawable.icons_car)
                    publicTransportationButton.setImageResource(R.drawable.icons_bus)
                }
                type = Type.WALK
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
                walkButton.setImageResource(R.drawable.icons_walk)
                carButton.setImageResource(R.drawable.icons_car_click)
                publicTransportationButton.setImageResource(R.drawable.icons_bus)
                if (checkPlace()) return@setOnClickListener
                type = Type.CAR
                Toast.makeText(this@CreateActivity, "이동수단 : 자동차", Toast.LENGTH_SHORT).show()
                val startTime = binding.dateTextView.text.toString()
                if (startTime == "추억의 시간을 지정해주세요") {
                    walkButton.setImageResource(R.drawable.icons_walk)
                    carButton.setImageResource(R.drawable.icons_car)
                    publicTransportationButton.setImageResource(R.drawable.icons_bus)
                    Toast.makeText(this@CreateActivity, "출발시간을 먼저 정해주세요.", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                val isoDateTime = TimeUtil.convertToISODateTime(startTime)
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

                walkButton.setImageResource(R.drawable.icons_walk)
                carButton.setImageResource(R.drawable.icons_car)
                publicTransportationButton.setImageResource(R.drawable.icons_bus_click)
                type = Type.PUBLIC
                if (checkPlace()) return@setOnClickListener
                Toast.makeText(this@CreateActivity, "이동수단 : 대중교통", Toast.LENGTH_SHORT).show()
                val startTime = binding.dateTextView.text.toString()
                if (startTime == "추억의 시간을 지정해주세요") {
                    walkButton.setImageResource(R.drawable.icons_walk)
                    carButton.setImageResource(R.drawable.icons_car)
                    publicTransportationButton.setImageResource(R.drawable.icons_bus)
                    Toast.makeText(this@CreateActivity, "시간을 먼저 정해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
                routeProvider.getRoute(startX, startY, endX, endY)
            }

            cancelImageView.setOnClickListener {
                showAlertDialog()
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

    fun receiveTagFriends(tagFriends: ArrayList<String>) {
        // tagFriends 목록을 이용해 필요한 작업 수행
        if (tagFriends.isNotEmpty()) {
            binding.imageFriendView.setImageResource(R.drawable.baseline_people_24)
            usingShare = true
            if (tagFriends.size == 1) {
                Log.d("tag", "$tagFriends")

            } else {

                val count = tagFriends.size
                Log.d("tag", "$count")
            }
        }
        for (nick in tagFriends)
            getUidByNickname(nick) { uid ->
                friendUids.add(uid.toString())
                if (friendUids.size == tagFriends.size) {
                    Log.d("tag", "$friendUids")
                }
            }
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

    private fun createTag() {
        val title = binding.titleEditText.text.toString()
        val date = binding.dateTextView.text.toString()
        val todoId = todoId
        val tagId = tagId

        notificationIdPlusReadyTime()

        val tag =
            Tag(
                tagId = tagId,
                todoId = todoId,
                title = title,
                date = date,
                place = arrivalPlace,
                endY = endY,
                endX = endX,
                usingShare = usingShare,
                friendUid = friendUids,
            )

        for (index in friendUids) {
            val myTagRef = FirebaseUtil.tagDataBase.child(index).push()
            tagKey = myTagRef.key!!
            tag.tagId = tagKey
            tag.todoId = todoKey       // 생성된 키 값을 객체에 할당

            val friendTagRef =
                Firebase.database.reference.child(DB_TAG).child(index).child(user).child(tagKey)

            myTagRef.setValue(tag).addOnSuccessListener {
                Log.d("tag", "Success")
            }
            friendTagRef.setValue(tag).addOnSuccessListener {
                Toast.makeText(applicationContext, "친구 태그", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createAlarm() {
        FirebaseUtil.alarmDataBase.child(notificationId!!).updateChildren(alarmData)
        val memo = binding.titleEditText.text.toString()
        val appointmentTime = alarmData["appointmentTime"].toString() //여기서 readyTime을 빼주자
        AlarmUtil.createAlarm(appointmentTime, this@CreateActivity, memo)
    }

    private fun setDateAndTime() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("날짜를 정해주세요")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        val timePicker = MaterialTimePicker.Builder()
            .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(0)
            .setMinute(0)
            .setTitleText("시간을 정해주세요.")
            .build()
        datePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            selectedDate = Date(datePicker.selection!!)
            calendar.time = selectedDate!!
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "일"
                Calendar.MONDAY -> "월"
                Calendar.TUESDAY -> "화"
                Calendar.WEDNESDAY -> "수"
                Calendar.THURSDAY -> "목"
                Calendar.FRIDAY -> "금"
                Calendar.SATURDAY -> "토"
                else -> ""
            }
            val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
            val dateString = dateFormat.format(selectedDate)
            getDate = "$dateString ($dayOfWeek)"
            startTime = "$dateString $timeString"

            timePicker.show(supportFragmentManager, "TimePickerDialog")
        }
        datePicker.show(supportFragmentManager, "datePickerDialog")
        timePicker.addOnPositiveButtonClickListener {
            if(selectedDate != null){
                val formatHour = String.format("%02d", timePicker.hour)
                val formatMinute = String.format("%02d", timePicker.minute)
                val timeStrings = "${formatHour}:${formatMinute}"
                binding.dateTextView.text = "$getDate, $timeStrings"
                startTime = "$dateString ${formatHour}:${formatMinute}"
                selectedDate = null
            }
        }
    }

    private fun createTodo() {
        val title = binding.titleEditText.text.toString()
        val dateAndTime = binding.dateTextView.text.toString()
        val date = dateAndTime.split(",")[0]
        val time = dateAndTime.split(",")[1].trim()
        val todoId = todoId
        val check = splitDate(date)
        val clickedYear = check[0].trim()
        val clickedMonth = check[1].trim()
        val clickedDay = check[2].trim()

        //여기서 notificationId에서 readyTime을 빼줘야함
        notificationIdPlusReadyTime()
        val todo =
            Todo(
                todoId = todoId,
                title = title,
                date = date,
                time = time,
                startPlace = startPlace,
                arrivePlace = arrivalPlace,
                notificationId = notificationId,
                startLat = startY,
                startLng = startX,
                arrivalLat = endY,
                arrivalLng = endX,
                usingAlarm = usingAlarm,
                readyTime = readyTime,
                usingShare = usingShare,
                friendUid = friendUids
            )

        val todoRef = Firebase.database.reference.child(DB_CALENDAR)
            .child(user)
            .child(clickedYear)
            .child(clickedMonth)
            .child(clickedDay)
            .push()

        todoKey = todoRef.key!!   // 자동 생성된 키 값을 가져옴
        todoKeys.add(todoKey!!)     // 가져온 키 값을 todoKeys 목록에 추가
        todo.todoId = todoKey       // 생성된 키 값을 객체에 할당

        todoRef.setValue(todo).addOnSuccessListener {
            Toast.makeText(applicationContext, "새로운 추억", Toast.LENGTH_SHORT).show()
            Log.i("FirebaseData", "데이터 전송에 성공하였습니다.")
        }.addOnCanceledListener {
            Log.i("FirebaseData", "데이터 전송에 실패하였습니다")
        }
    }

    private fun updateTodo(todoKey: String, todo: Todo) {
        val todoKey = intent.getStringExtra("todoKey")
        val todo = intent.getParcelableExtra<Todo>("todo")

        // 기존 일정 시작 날짜와 일정 제목
        val oldStartDate = todo?.date.toString()
        val oldTitle = todo?.title.toString()

        val title = binding.titleEditText.text.toString()
        val dateAndTime = binding.dateTextView.text.toString()
        val date = dateAndTime.split(",")[0]
        val time = dateAndTime.split(",")[1].trim()
        val startPlace = startPlace
        val arrivePlace = arrivalPlace
        val todoId = todo?.todoId

        //여기서 notificationId에서 readyTime을 빼줘야함
        notificationIdPlusReadyTime()

        val todoUpdates: MutableMap<String, Any> = HashMap()
        todoUpdates["title"] = title
        todoUpdates["date"] = date
        todoUpdates["time"] = time
        todoUpdates["startPlace"] = startPlace
        todoUpdates["arrivePlace"] = arrivePlace
        todoUpdates["todoId"] = todoId!!
        todoUpdates["startLng"] = startX
        todoUpdates["startLat"] = startY
        todoUpdates["arrivalLng"] = endX
        todoUpdates["arrivalLat"] = endY
        todoUpdates["usingAlarm"] = usingAlarm
        todoUpdates["notificationId"] = notificationId!!
        todoUpdates["usingAlarm"] = usingAlarm
        todoUpdates["readyTime"] = readyTime
        todoUpdates["usingShare"] = usingShare
        todoUpdates["friendUid"] = friendUids


        // 변경된 일정 시작 날짜
        val newStartDate = todoUpdates["date"].toString()

        // 시작 날짜가 변경될 경우
        if (oldStartDate != newStartDate) {

            val deleteFirebase = splitDate(oldStartDate)
            val oldYear = deleteFirebase[0].trim()
            val oldMonth = deleteFirebase[1].trim()
            val oldDay = deleteFirebase[2].trim()
            // 이전 시작 날짜의 경로 참조
            val oldTodoReference = Firebase.database.reference.child(DB_CALENDAR)
                .child(user)
                .child(oldYear)
                .child(oldMonth)
                .child(oldDay)
                .child(todoKey!!)

            // 이전 날짜의 일정 삭제
            oldTodoReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (data in dataSnapshot.children) {
                        data.ref.removeValue()
                            .addOnSuccessListener {
                                Log.i("FirebaseData", "기존 데이터 삭제")
                            }
                            .addOnCanceledListener {
                                Log.i("FirebaseData", "기존 데이터 삭제 실패.")
                            }
                    }
                }
            })
            // 변경한 날짜의 일정 생성
            val createFirebase = splitDate(newStartDate)
            val newYear = createFirebase[0].trim()
            val newMonth = createFirebase[1].trim()
            val newDay = createFirebase[2].trim()
            // 변경한 시작 날짜의 경로 참조
            val newTodoReference = Firebase.database.reference.child(DB_CALENDAR)
                .child(user)
                .child(newYear)
                .child(newMonth)
                .child(newDay)
            // 변경한 날짜에 일정 업데이트
            if (todoKey != null) {
                newTodoReference
                    .child(todoKey)
                    .setValue(todoUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "추억 수정 완료", Toast.LENGTH_SHORT).show()
                        Log.i("FirebaseData", "데이터 업데이트에 성공하였습니다.")
                    }
                    .addOnCanceledListener {
                        Log.i("FirebaseData", "데이터 업데이트에 실패하였습니다.")
                    }
            }
        }
        // 시작 날짜 변경 없고 다른 내용 수정인 경우
        else {
            val updateFirebase = splitDate(oldStartDate)
            val originalYear = updateFirebase[0].trim()
            val originalMonth = updateFirebase[1].trim()
            val originalDay = updateFirebase[2].trim()

            // 기존 날짜 경로 참조
            val todoReference = Firebase.database.reference.child(DB_CALENDAR)
                .child(user)
                .child(originalYear)
                .child(originalMonth)
                .child(originalDay)
            // 기존 날짜에 일정 업데이트
            if (todoKey != null) {
                todoReference
                    .child(todoKey)
                    .setValue(todoUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "일정 수정 완료", Toast.LENGTH_SHORT).show()
                        Log.i("FirebaseData", "데이터 업데이트에 성공하였습니다.")
                    }
                    .addOnCanceledListener {
                        Log.i("FirebaseData", "데이터 업데이트에 실패하였습니다.")
                    }
            }
        }
    }

    private fun notificationIdPlusReadyTime() {
        if (notificationId == "0") return
        if (readyTime == "") return
        val fullNotificationId = "202$notificationId"
        val dateNotificationId = SimpleDateFormat("yyyyMMddHHmm").parse(fullNotificationId)
        val date = Calendar.getInstance()
        date.time = dateNotificationId

        val readyHour = readyTime.substring(0, 2).toInt()
        val readyMinute = readyTime.substring(3, 5).toInt()

        date.add(Calendar.HOUR_OF_DAY, -readyHour)
        date.add(Calendar.MINUTE, -readyMinute)
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1 // 월은 0부터 시작하므로 1을 더함
        val day = date.get(Calendar.DAY_OF_MONTH)
        val hour = date.get(Calendar.HOUR_OF_DAY)
        val minute = date.get(Calendar.MINUTE)
        notificationId =
            String.format("%04d%02d%02d%02d%02d", year, month, day, hour, minute)
                .substring(3) //202309160940
    }

    private fun splitDate(date: String): Array<String> {
        Log.e("splitDate", date)
        val splitText = date.split(" ")
        val resultDate: Array<String> = Array(3) { "" }
        resultDate[0] = splitText[0]  //year
        resultDate[1] = splitText[1]  //month
        resultDate[2] = splitText[2]  //day
        return resultDate
    }

    // 날짜로 요일 구하는 함수
    private fun getDayOfWeek(year: Int, month: Int, day: Int): String {
        val cal: Calendar = Calendar.getInstance()
        cal.set(year, month - 1, day)

        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "일"
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            else -> ""
        }
    }

    private fun showAlertDialog() {
        if (isEditMode) {
            MaterialAlertDialogBuilder(this)
                .setTitle("추억 수정 취소")
                .setMessage("추억 수정을 그만두시겠습니까?")
                .setNegativeButton("아니요") { _, _ -> }
                .setPositiveButton("네") { _, _ -> finish() }
                .show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("추억 생성 취소")
                .setMessage("추억 생성을 그만두시겠습니까?")
                .setNegativeButton("아니요") { _, _ -> }
                .setPositiveButton("네") { _, _ -> finish() }
                .show()
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

    @RequiresApi(Build.VERSION_CODES.S)
    override fun loadWalkingRoot(data: Dto) {
        val result =
            data.features?.filter { it.properties.index == 0 }?.map { it.properties }?.first()
                ?: return
        val totalTime = TimeUtil.formatTotalTime(result.totalTime)// 5655초로나오게됨
        try {
            val date = dateFormat.parse(binding.dateTextView.text.toString())
            calendar.time = date
            calendar.add(Calendar.SECOND, -result.totalTime)
            setAlarm(calendar, NO_ALARM)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.apply {
            emptyTextView.isVisible = false
            recyclerView.isVisible = false
            totalTimeTextView.isVisible = false
            resultTextView.apply {
                isVisible = true
                text =
                    "총 소요시간 : $totalTime\n총 거리 : ${DecimalFormat("###,###").format(result.totalDistance)}m"
            }
            animationView.apply {
                setAnimation(R.raw.walking_animation)
                isVisible = true
                playAnimation()
                repeatCount = LottieDrawable.INFINITE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun loadCarRoot(data: com.design.model.car.Dto) {
        val result = data.features?.map { it.properties }?.firstOrNull() ?: return
        val totalTime = TimeUtil.formatTotalTime(result.totalTime)
        val departure = TimeUtil.parseDateTime(result.departureTime)
        val arrival = TimeUtil.parseDateTime(result.arrivalTime)
        val totalFare =
            if (result.totalFare > 0) DecimalFormat("###,###").format(result.totalFare) else "통행요금 없음"
        val taxiFare = DecimalFormat("###,###").format(result.taxiFare) + "원"
        val date = dateFormat.parse(binding.dateTextView.text.toString())
        calendar.time = date
        calendar.add(Calendar.SECOND, -result.totalTime)
        setAlarm(calendar, NO_ALARM)
        binding.apply {
            emptyTextView.isVisible = false
            recyclerView.isVisible = false
            totalTimeTextView.isVisible = false
            resultTextView.apply {
                isVisible = true
                text =
                    "총 소요시간 : $totalTime\n출발시간 : $departure\n도착 예정시간 : $arrival"
            }
            animationView.apply {
                setAnimation(R.raw.car_animation)
                isVisible = true
                playAnimation()
                repeatCount = LottieDrawable.INFINITE
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun loadRoute(data: PublicTransitRoute?) {
        //제일 최단 길정보를 가져옴
        Thread {
            try {
                val minTimePath = data?.result?.path?.minByOrNull { it.info.totalTime }
                val totalTime = minTimePath?.info?.totalTime
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
                info.forEach { info ->
                    info.waitTime

                }
                //맨처음 도착지점 같은 경우 두번째 리스트에 있는 것으로 설정
                for (i in 0 until info.size) {
                    if (i > 0 && i < info.size - 1 && info[i].endName == null) {
                        info[i].endName = info[i + 1].startName
                    }
                    if (i > 0 && info[i].startName == null) {
                        info[i].startName = info[i - 1].endName
                    }
                }

                val date = dateFormat.parse(binding.dateTextView.text.toString())
                calendar.time = date
                calendar.add(Calendar.SECOND, -(minTimePath?.info?.totalTime!! * 60))
                setAlarm(calendar, NO_ALARM)
                info[0].startName = "출발지"
                info[info.size - 1].endName = "도착지"
                info[0].endName = info[1].startName
                info[info.size - 1].startName = info[info.size - 2].endName
                runOnUiThread {
                    val routeAdapter = RouteAdapter(info)
                    binding.apply {
                        emptyTextView.isVisible = false
                        totalTimeTextView.text = "총 소요시간 : $totalTime 분"
                        totalTimeTextView.isVisible = true
                        resultTextView.visibility = View.INVISIBLE
                        recyclerView.apply {
                            isVisible = true
                            adapter = routeAdapter
                            val dividerItemDecoration =
                                DividerItemDecoration(
                                    this@CreateActivity,
                                    LinearLayoutManager.VERTICAL
                                )
                            addItemDecoration(dividerItemDecoration)
                        }
                        animationView.apply {
                            isVisible = true
                            setAnimation(R.raw.bus_animation)
                            playAnimation()
                            repeatCount = LottieDrawable.INFINITE
                        }

                    }

                }


            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "거리가 너무 가깝습니다.(700m이내)", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setAlarm(calendar: Calendar, flag: Int, todoKey: String = "") {
        //readyTime에 대해 시간 빼주기
        if (readyTime != "" && flag == ALARM) {
            val readyHour = readyTime.substring(0, 2).toInt()
            val readyMinute = readyTime.substring(3, 5).toInt()
            calendar.add(Calendar.HOUR_OF_DAY, -readyHour)
            calendar.add(Calendar.MINUTE, -readyMinute)
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 월은 0부터 시작하므로 1을 더함
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val appointmentTime =
            String.format("%04d%02d%02d%02d%02d", year, month, day, hour, minute) //202309160940
        notificationId = appointmentTime.substring(3)
        //알람을 설정하는 부분임 먼저 db에 저장을 하는 거 먼저해보자
        alarmData = mutableMapOf()
        //들어가야할 정보 출발 도착지 위경도와 도착을 해야하는 시간 notificationId type
        alarmData["startLng"] = startX
        alarmData["startLat"] = startY
        alarmData["arrivalLng"] = endX
        alarmData["arrivalLat"] = endY
        alarmData["startPlace"] = startPlace
        alarmData["arrivalPlace"] = arrivalPlace
        alarmData["type"] = type.toString()
        alarmData["notificationId"] = notificationId!!
        alarmData["appointmentTime"] = appointmentTime
        alarmData["dateTime"] = binding.dateTextView.text.toString()
        alarmData["message"] = binding.titleEditText.text.toString()
        alarmData["readyTime"] = readyTime
        alarmData["todoId"] = todoKey
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
                    Log.e("aa", latestTime.toString())
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
                    busRealTimeProvider.getBusRealTime(subwayCode.toInt(), routeId) {
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

    companion object {
        const val ALARM = 0
        const val NO_ALARM = 1
    }

}


