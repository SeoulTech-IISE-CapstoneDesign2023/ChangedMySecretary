package com.example.fastcampus.part3.design

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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
import com.example.fastcampus.part3.design.adapter.RouteAdapter
import com.example.fastcampus.part3.design.model.route.PublicTransitRoute
import com.example.fastcampus.part3.design.model.route.SubPath
import com.example.fastcampus.part3.design.databinding.ActivityCreateBinding
import com.example.fastcampus.part3.design.model.Todo
import com.example.fastcampus.part3.design.model.Type
import com.example.fastcampus.part3.design.model.calendar.TodoDataProvider
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
import com.example.fastcampus.part3.design.util.AlarmUtil
import com.example.fastcampus.part3.design.util.FirebaseUtil
import com.example.fastcampus.part3.design.util.Key.Companion.DB_CALENDAR
import com.example.fastcampus.part3.design.util.LocationRetrofitManager
import com.example.fastcampus.part3.design.util.TimeUtil
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
import com.naver.maps.map.util.FusedLocationSource
import com.prolificinteractive.materialcalendarview.CalendarDay
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

    private lateinit var dateBottomBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mapBottomBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var searchBottomBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    private lateinit var locationAdapter: LocationAdapter

    private val startTimePicker = MaterialTimePicker.Builder()
        .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
        .setTimeFormat(TimeFormat.CLOCK_12H)
        .setHour(0)
        .setMinute(0)
        .setTitleText("시간을 정하세요.")
        .build()

    private val endTimePicker = MaterialTimePicker.Builder()
        .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
        .setTimeFormat(TimeFormat.CLOCK_12H)
        .setHour(0)
        .setMinute(0)
        .setTitleText("시간을 정하세요.")
        .build()

    private val datePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText("종료 날짜를 정하세요")
        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
        .build()

    //알람권한요청
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
    private var isStart = true
    private var startPlace = ""
    private var arrivalPlace = ""
    private var startX = 0.0
    private var startY = 0.0
    private var endX = 0.0
    private var endY = 0.0
    private var type: String? = null
    private var notificationId: String? = null
    private var alarmData = mutableMapOf<String, Any>()
    private lateinit var user: String
    private var todoKeys: ArrayList<String> = arrayListOf()
    private var timeString = ""
    private var dateString = ""
    private var title = ""
    private var stDate = ""
    private var stTime = ""
    private var enDate = ""
    private var enTime = ""
    private var memo = ""
    private var startTime = ""
    private var endDate = ""
    private var endTime = ""
    private var todoId = ""
    private var isEditMode = false  // 편집 모드 여부를 나타내는 변수
    private var usingAlarm = false
    private var startTimeToString = "" //약속시간을 저장하는 변수
    private var arrivalTimeToString = "" //약속시간을 저장하는 변수
    private var editTextLength = 0
    private var previousMemo = ""
    private var changedMemo = ""


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        locationSource = FusedLocationSource(this, MapFragment.LOCATION_PERMISSION_REQUEST_CODE)
        mapView = binding.mapBottomSheetLayout.mapView
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
        binding.mapBottomSheetLayout.locationButton.map = map
    }
    override fun loadTodoData(data: Todo){
        todoId = data.todoId
        title = data.title
        stDate = data.stDate
        stTime = data.stTime
        enDate = data.enDate!!
        enTime = data.enTime!!
        memo = data.memo.toString()

        isEditMode = true  // 기존 Todo를 수정하는 편집 모드임을 나타냄

        // 화면 초기화
        // Todo의 날짜 및 시간
        binding.dateTextView.text = "$stDate, $stTime"
        // Todo의 제목
        binding.titleEditText.setText(title)
        // 시작 날짜 및 시간
        binding.dateBottomSheetLayout.startDateTextView.text = stDate
        binding.dateBottomSheetLayout.startTimeText.text = stTime
        // 도착 날짜 및 시간
        binding.dateBottomSheetLayout.endDateTextView.text = enDate
        binding.dateBottomSheetLayout.endTimeText.text = enTime
        // 메모장
//        binding.memoEditText.text = memo
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

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        //list에서 일정 하나 선택했을 때 내용 수정
        val startDate = intent.getStringExtra("startDate") // "yyyy/MM/dd" 형식으로 받음
        val todoKey = intent.getStringExtra("todoKey")
        // 기존의 Todo를 수정하는 경우, 일정 시작 날짜와 일정 키 값으로 provider 받아옴
        if (startDate != null && todoKey != null) {
            todoDataProvider.getTodoData(startDate, todoKey)
        } else{
            // 새로운 Todo를 생성하는 경우, 화면을 초기화
            initializeCreateMode(startDate)
        }

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

        binding.cancelImageView.setOnClickListener {
            showAlertDialog()
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
            // 일정 시작시간이 종료시간보다 늦을 경우 일정 생성 불가 토스트
            else if (!checkDate()) {
                binding.createButton.background = AppCompatResources.getDrawable(
                    this@CreateActivity, R.drawable.baseline_check_gray_24
                )
                Toast.makeText(this, "시작시간은 종료시간보다 늦을 수 없습니다", Toast.LENGTH_SHORT).show()
            }
            // 메모장 텍스트 100자 넘어가면 일정 생성 불가 토스트
            else if (editTextLength > 100) {
                binding.createButton.background = AppCompatResources.getDrawable(
                    this@CreateActivity, R.drawable.baseline_check_gray_24
                )
                Toast.makeText(this, "메모 글자수가 100을 넘었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                if (isEditMode) {
                    Log.d("create", "initailize edit mode")
                    //기존의 todo를 수정
                    updateTodo(todoKey!!, todo!!)
                    finish()
                } else {
                    Log.d("create", "initailize create mode")
                    // 새로운 Todo를 생성하는 경우
                    if (notificationId != null) {//알람을 설정할때
                        usingAlarm = true
                        FirebaseUtil.alarmDataBase.child(notificationId!!).updateChildren(alarmData)
                        val memo = binding.memoEditText.text.toString()
                        val message = "${memo}할 시간이에요~"
                        val appointmentTime = alarmData["appointmentTime"].toString()
                        AlarmUtil.createAlarm(appointmentTime, this@CreateActivity, message)
                    }
                    createTodo()

                    finish()
                }
            }
            true
        }


        binding.dateBottomSheetLayout.apply {
            endDateTextView.setOnClickListener {
                setDate(1)
            }
            startTimeText.setOnClickListener {
                setTime(0)
            }
            endTimeText.setOnClickListener {
                setTime(1)
            }
            // 날짜 선택시 날짜 view 변경
            calendarView.setOnDateChangedListener { widget, date, selected ->
                val year = date.year
                val month = date.month + 1
                val dayOfMonth = date.day
                val dayOfWeek = getDayOfWeek(year, month, dayOfMonth)
                // 월과 일을 각각 두 자리로 포맷팅
                val formattedMonth = String.format("%02d", month)
                val formattedDayOfMonth = String.format("%02d", dayOfMonth)
                val dateText = "${year}년 ${formattedMonth}월 ${formattedDayOfMonth}일"
                startDateTextView.text = "$dateText ($dayOfWeek)"
                startTime = "$dateText $timeString"
            }
            // 현재 날짜 적용 선택 시
            currentTimeButton.setOnClickListener(View.OnClickListener {
                // 오늘 날짜 받아오기
                val today = Calendar.getInstance()
                val todayYear = today[Calendar.YEAR]
                val todayMonth = today[Calendar.MONTH] + 1
                val todayDay = today[Calendar.DAY_OF_MONTH]
                val todayOfWeek = getDayOfWeek(todayYear, todayMonth, todayDay)
                // 월과 일을 각각 두 자리로 포맷팅
                val formattedMonth = String.format("%02d", todayMonth)
                val formattedDayOfMonth = String.format("%02d", todayDay)
                val todayText = "${todayYear}년 ${formattedMonth}월 ${formattedDayOfMonth}일"
                startDateTextView.text = "$todayText ($todayOfWeek)"
                // CalendarView에서 오늘 날짜로 설정
                calendarView.currentDate = CalendarDay.today()
            })
            // 확인 선택 시
            okButton.setOnClickListener {
                if (!checkDate()) {
                    binding.dateBottomSheetLayout.endDateTextView.text = "추억의 끝을 다시 입력해 주세요"
                    binding.dateBottomSheetLayout.endTimeText.text = "00:00"
                    endTime = ""
//                    Toast.makeText(this, "종료 시간이 시작 시간보다 빠를 수 없습니다", Toast.LENGTH_SHORT).show()
                }
                else{
                    dateBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    binding.dateTextView.text = "${startDateTextView.text}, ${startTimeText.text}"
                }
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
            // 메모장 글자수가 100자가 넘어가면 error 표시
            binding.memoEditText.addTextChangedListener {
                it?.let { text ->
                    binding.memoEditText.error = if (text.length > 100) {
                        "글자수를 초과하였습니다."
                    } else null
                }
            }
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
            walkButton.setOnClickListener {
                if (checkPlace()) return@setOnClickListener
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
                if (checkPlace()) return@setOnClickListener
                type = Type.CAR
                Toast.makeText(this@CreateActivity, "이동수단 : 자동차", Toast.LENGTH_SHORT).show()
                val startTime = binding.dateTextView.text.toString()
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
                type = Type.PUBLIC
                if (checkPlace()) return@setOnClickListener
                Toast.makeText(this@CreateActivity, "이동수단 : 대중교통", Toast.LENGTH_SHORT).show()
                routeProvider.getRoute(startX, startY, endX, endY)
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

//    private fun initializeEditMode(todo: Todo) {
//        // 기존의 Todo를 수정하는 경우, 해당 Todo의 정보를 사용하여 화면을 초기화
//        isEditMode = true  // 기존 Todo를 수정하는 편집 모드임을 나타냄
//        binding.dateTextView.text = "${todo.stDate}, ${todo.stTime}"
//        // Todo의 제목
//        binding.titleEditText.setText(todo.title)
//        // 시작 날짜 및 시간
//        binding.dateBottomSheetLayout.startDateTextView.text = todo.stDate
//        binding.dateBottomSheetLayout.startTimeText.text = todo.stTime
//        // 도착 날짜 및 시간
//        binding.dateBottomSheetLayout.endDateTextView.text = todo.enDate
//        binding.dateBottomSheetLayout.endTimeText.text = todo.enTime
//
//    }

    private fun initializeCreateMode(startDate: String?) {
        // 새로운 일정을 생성하는 경우, 화면을 초기화하는 작업 수행
        binding.titleEditText.setText(if (title != "") title else "")
        binding.dateBottomSheetLayout.startDateTextView.text = startDate
        binding.dateBottomSheetLayout.startTimeText.text = if (startTime != "") startTime else "00:00"
        binding.dateBottomSheetLayout.endDateTextView.text = if (endDate != "") endDate else ""
        binding.dateBottomSheetLayout.endTimeText.text = if (endTime != "") endTime else ""
    }

    private fun convertToNumericInt(inputString: String): String {
        // "년 월 일" 부분을 추출하여 빈 칸으로 구분된 숫자로 분할
        val datePattern = Regex("(\\d+)년 (\\d+)월 (\\d+)일")
        val dateMatchResult = datePattern.find(inputString)

        // "시:분" 부분을 추출하여 ":"으로 구분된 숫자로 분할
        val timePattern = Regex("(\\d+):(\\d+)")
        val timeMatchResult = timePattern.find(inputString)

        var result = ""

        if (dateMatchResult != null && timeMatchResult != null) {
            val (year, month, day) = dateMatchResult.destructured
            val (hour, minute) = timeMatchResult.destructured

            // 추출한 숫자를 연결하여 원하는 형식으로 재구성
            result = "${year}${month}${day}${hour}${minute}"
        }
        // 추출한 숫자를 연결하여 원하는 형식으로 재구성
        return result   // 출력 예시: "202310020000"
    }

    // 일정 생성 할 때 날짜 체크
    private fun checkDate(): Boolean {
        if (binding.dateBottomSheetLayout.endDateTextView.text == "" || binding.dateBottomSheetLayout.endTimeText.text == "") {
            return true
        } else {
            val changeStartTime =
                binding.dateBottomSheetLayout.startTimeText.text.toString()
            val changeArrivalTime =
                binding.dateBottomSheetLayout.endTimeText.text.toString()
            val startTimeText =
                binding.dateBottomSheetLayout.startDateTextView.text.toString() + changeStartTime
            val arrivalTimeText =
                binding.dateBottomSheetLayout.endDateTextView.text.toString() + changeArrivalTime

            startTimeToString = convertToNumericInt(startTimeText)
            arrivalTimeToString = convertToNumericInt(arrivalTimeText)
            Log.e("날짜 확인", startTimeToString)
            Log.e("날짜 확인", arrivalTimeToString)
            return startTimeToString <= arrivalTimeToString
        }
    }

    private fun setDate(separator: Int) {
//        datePicker.show(supportFragmentManager, "datePickerDialog")
        datePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            val selectedDate = Date(datePicker.selection!!)
            calendar.time = selectedDate
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

            if (separator == 1) {
                binding.dateBottomSheetLayout.endDateTextView.text = "$dateString ($dayOfWeek)"
                endTime = "$dateString $timeString"
            } else {

            }
        }
        datePicker.show(supportFragmentManager, "datePickerDialog")
    }

    private fun setTime(separator: Int) {
        if (separator == 0) {
            startTimePicker.addOnPositiveButtonClickListener {
                val formatHour = String.format("%02d", startTimePicker.hour)
                val formatMinute = String.format("%02d", startTimePicker.minute)
                val timeStrings = "${formatHour}:${formatMinute}"
                binding.dateBottomSheetLayout.startTimeText.text = timeStrings
                startTime = "$dateString ${formatHour}:${formatMinute}"
            }
            startTimePicker.show(supportFragmentManager, "TimePickerDialog")
        }
        else {
            endTimePicker.addOnPositiveButtonClickListener {
                val formatHour = String.format("%02d", endTimePicker.hour)
                val formatMinute = String.format("%02d", endTimePicker.minute)
                val timeStrings = "${formatHour}:${formatMinute}"
                binding.dateBottomSheetLayout.endTimeText.text = timeStrings
                endTime = "$dateString ${formatHour}:${formatMinute}"
            }
            endTimePicker.show(supportFragmentManager, "TimePickerDialog")
        }
    }

    private fun createTodo() {
        val title = binding.titleEditText.text.toString()
        val stDate = binding.dateBottomSheetLayout.startDateTextView.text.toString()
        val stTime = binding.dateBottomSheetLayout.startTimeText.text.toString()
        val enDate = binding.dateBottomSheetLayout.endDateTextView.text.toString()
        val enTime = binding.dateBottomSheetLayout.endTimeText.text.toString()
        val memo = binding.memoEditText.toString()
        val todoId = todoId
        val check = splitDate(stDate)
        val clickedYear = check[0].trim()
        val clickedMonth = check[1].trim()
        val clickedDay = check[2].trim()

        val todo =
            Todo(
                todoId = todoId,
                title = title,
                stDate = stDate,
                stTime = stTime,
                enDate = enDate,
                enTime = enTime,
                memo = memo,
                startPlace = startPlace,
                arrivePlace = arrivalPlace,
                notificationId = notificationId,
                startLat = startY,
                startLng = startX,
                arrivalLat = endY,
                arrivalLng = endX,
                usingAlarm = usingAlarm,
            )

        val todoRef = Firebase.database.reference.child(DB_CALENDAR)
            .child(user)
            .child(clickedYear)
            .child(clickedMonth)
            .child(clickedDay)
            .push()

        val todoKey = todoRef.key   // 자동 생성된 키 값을 가져옴
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
        val oldStartDate = todo?.stDate.toString()
        val oldTitle = todo?.title.toString()

        val title = binding.titleEditText.text.toString()
        val stDate = binding.dateBottomSheetLayout.startDateTextView.text.toString()
        val stTime = binding.dateBottomSheetLayout.startTimeText.text.toString()
        val enDate = binding.dateBottomSheetLayout.endDateTextView.text.toString()
        val enTime = binding.dateBottomSheetLayout.endTimeText.text.toString()
        val memo = binding.memoEditText.text.toString()
        val startPlace = startPlace
        val arrivePlace = arrivalPlace
        val todoId = todo?.todoId


        val todoUpdates: MutableMap<String, Any> = HashMap()
        todoUpdates["title"] = title
        todoUpdates["stDate"] = stDate
        todoUpdates["stTime"] = stTime
        todoUpdates["enDate"] = enDate
        todoUpdates["enTime"] = enTime
        todoUpdates["memo"] = memo
        todoUpdates["startPlace"] = startPlace
        todoUpdates["arrivePlace"] = arrivePlace
        todoUpdates["todoId"] = todoId!!
        todoUpdates["startLng"] = startX
        todoUpdates["startLat"] = startY
        todoUpdates["arrivalLng"] = endX
        todoUpdates["arrivalLat"] = endY
        todoUpdates["usingAlarm"] = usingAlarm

        // 변경된 일정 시작 날짜
        val newStartDate = todoUpdates["stDate"].toString()

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
        MaterialAlertDialogBuilder(this)
            .setTitle("추억 생성 취소")
            .setMessage("추억 생성을 그만두시겠습니까?")
            .setNegativeButton("아니요") { _, _ -> }
            .setPositiveButton("네") { _, _ -> finish() }
            .show()
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
        binding.mapBottomSheetLayout.recyclerView.isVisible = false
        val result =
            data.features?.filter { it.properties.index == 0 }?.map { it.properties }?.first()
                ?: return
        val totalTime = TimeUtil.formatTotalTime(result.totalTime)// 5655초로나오게됨
        try {
            val date = dateFormat.parse(binding.dateTextView.text.toString())
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.SECOND, -result.totalTime)
            setAlarm(calendar)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.mapBottomSheetLayout.resultTextView.apply {
            isVisible = true
            text =
                "총 소요시간 : $totalTime\n총 거리 : ${DecimalFormat("###,###").format(result.totalDistance)}m"
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun loadCarRoot(data: com.example.fastcampus.part3.design.model.car.Dto) {
        binding.mapBottomSheetLayout.recyclerView.isVisible = false
        val result = data.features?.map { it.properties }?.firstOrNull() ?: return
        val totalTime = TimeUtil.formatTotalTime(result.totalTime)
        val departure = TimeUtil.parseDateTime(result.departureTime)
        val arrival = TimeUtil.parseDateTime(result.arrivalTime)
        val totalFare =
            if (result.totalFare > 0) DecimalFormat("###,###").format(result.totalFare) else "통행요금 없음"
        val taxiFare = DecimalFormat("###,###").format(result.taxiFare) + "원"
        val date = dateFormat.parse(binding.dateTextView.text.toString())
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.SECOND, -result.totalTime)
        setAlarm(calendar)
        binding.mapBottomSheetLayout.resultTextView.apply {
            isVisible = true
            text =
                "총 소요시간 : $totalTime\n총 톨게이트비 : $totalFare\n총 택시비 : $taxiFare\n출발시간 : $departure\n도착 예정시간 : $arrival"
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
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
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.add(Calendar.SECOND, -(minTimePath?.info?.totalTime!! * 60))
                setAlarm(calendar)
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
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "거리가 너무 가깝습니다.(700m이내)", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setAlarm(calendar: Calendar) {
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
        alarmData["message"] = "${binding.memoEditText.text}할 시간이에요~"
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

}


