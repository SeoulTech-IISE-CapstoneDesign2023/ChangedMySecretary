package com.example.fastcampus.part3.design

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.fastcampus.part3.design.databinding.ActivityCreateBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateBinding

    private var amChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
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

        val bottomBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.root)
        bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.dateTextView.setOnClickListener {
            //완전 펴져있으면은 접어버림 아니면은 닫아버림
            if (bottomBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                imm.hideSoftInputFromWindow(binding.memoEditText.windowToken, 0)
            } else {
                bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }


        binding.memoEditText.setOnClickListener {
            bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
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
    }
}