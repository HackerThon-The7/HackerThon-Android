package com.yongjincompany.hackerthonandroid.features.calendar.view

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.yongjincompany.hackerthonandroid.R
import com.yongjincompany.hackerthonandroid.database.RoomDatabase
import com.yongjincompany.hackerthonandroid.databinding.ActivityCalendarBinding
import com.yongjincompany.hackerthonandroid.features.calendar.utils.CycleDecorator
import com.yongjincompany.hackerthonandroid.features.calendar.utils.DayStateDecorator
import com.yongjincompany.hackerthonandroid.features.calendar.utils.TodayDecorator
import com.yongjincompany.hackerthonandroid.features.calendar.vm.CalendarViewModel
import com.yongjincompany.hackerthonandroid.features.diary.view.StateDiaryActivity
import java.time.LocalDate

class CalendarActivity : AppCompatActivity() {

    lateinit var binding: ActivityCalendarBinding
    lateinit var calendarViewModel: CalendarViewModel

    private var isFabOpen = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDataBinding()
        calendarViewModel.database = RoomDatabase.getInstance(this)
        calendarViewModel.getAllStateDate()
        bindingView()
        observeLiveData()
        calendarViewModel.getAllCycles()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindingView() {
        binding.calendarView.leftArrow.setTint(ContextCompat.getColor(this, R.color.white))
        binding.calendarView.rightArrow.setTint(ContextCompat.getColor(this, R.color.white))

        binding.calendarView.selectedDate = CalendarDay.today()

        binding.calendarView.setOnDateChangedListener { widget, date, selected ->
            calendarViewModel.currentDate.value = "${date.year}-${String.format("%02d", date.month)}-${String.format("%02d", date.day)}"
        }

        binding.fabAdd.setOnClickListener {
            toggleFab()
        }

        binding.layoutAddStateDiary.setOnClickListener {
            navigateToStateDiary()
        }

        binding.fabDiary.setOnClickListener { 
            navigateToStateDiary()
        }

        binding.fabPeriodStart.setOnClickListener {
            calendarViewModel.insertStartDate(calendarViewModel.currentDate.value ?: LocalDate.now().toString())
            Toast.makeText(this, "월경 시작 날짜를 등록합니다.", Toast.LENGTH_SHORT).show()
            toggleFab()
        }

        binding.fabPeriodEnd.setOnClickListener {
            calendarViewModel.insertEndDate(calendarViewModel.currentDate.value ?: LocalDate.now().toString())
            Toast.makeText(this, "월경 끝 날짜를 등록합니다.", Toast.LENGTH_SHORT).show()
            toggleFab()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeLiveData() = with(calendarViewModel) {
        currentDate.observe(this@CalendarActivity) {
            getStateDiaryByDate(it)
        }

        dayStateEntity.observe(this@CalendarActivity) {
            if (it == null) {
                binding.layoutDayState.visibility = View.GONE
            } else {
                binding.layoutDayState.visibility = View.VISIBLE
                binding.tvBodyState.text = it.bodyState
                if (it.mood == "좋아요!") {
                    binding.ivMood.setImageResource(R.drawable.ic_happy)
                } else {
                    binding.ivMood.setImageResource(R.drawable.ic_sad)
                }
                binding.tvBehaviorChange.text = it.behaviorChange
            }
        }

        dateList.observe(this@CalendarActivity) {
            it.forEach {
                Log.d("CalendarDateList", it)
            }
            setDeco()
        }

        cycles.observe(this@CalendarActivity) {
            it.last { cycle ->
                cycle.endDate ?: return@observe

                val localStartDate = LocalDate.parse(cycle.startDate)
                val dateList = mutableListOf<String>()
                val term = cycle.getPeriodTerm()

                for (i in 0..term) {
                    dateList.add(localStartDate.plusDays(i.toLong()).toString())
                }
                calendarViewModel.finalCycleList.value = dateList
                dateList.forEach {
                    Log.d("FinalFunc", it)
                }
                setDeco()
                true
            }
        }
    }

    private fun setDeco() {
        calendarViewModel.finalCycleList.value?.forEach {
            Log.d("SetDeco", "$it final")
        }

        calendarViewModel.dateList.value?.forEach {
            Log.d("SetDeco", "$it date")
        }
        val cycleDecorator = CycleDecorator(this@CalendarActivity, calendarViewModel.finalCycleList.value ?: emptyList())
        val dateStateDecorator = DayStateDecorator(this@CalendarActivity, calendarViewModel.dateList.value ?: emptyList())
        val todayDecorator = TodayDecorator()
        binding.calendarView.addDecorators(dateStateDecorator, todayDecorator, cycleDecorator)
    }

    private fun toggleFab() {
        if (isFabOpen) {
            ObjectAnimator.ofFloat(binding.fabDiary, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabPeriodEnd, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabPeriodStart, "translationY", 0f).apply { start() }

            ObjectAnimator.ofFloat(binding.tvDiary, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.tvPeriodEnd, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.tvPeriodStart, "translationY", 0f).apply { start() }

            binding.tvDiary.visibility = View.GONE
            binding.tvPeriodEnd.visibility = View.GONE
            binding.tvPeriodStart.visibility = View.GONE

            binding.fabAdd.setImageResource(R.drawable.ic_add)
        } else {
            ObjectAnimator.ofFloat(binding.fabDiary, "translationY", -150f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabPeriodEnd, "translationY", -300f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabPeriodStart, "translationY", -450f).apply { start() }

            ObjectAnimator.ofFloat(binding.tvDiary, "translationY", -150f).apply { start() }
            ObjectAnimator.ofFloat(binding.tvPeriodEnd, "translationY", -300f).apply { start() }
            ObjectAnimator.ofFloat(binding.tvPeriodStart, "translationY", -450f).apply { start() }

            binding.tvDiary.visibility = View.VISIBLE
            binding.tvPeriodEnd.visibility = View.VISIBLE
            binding.tvPeriodStart.visibility = View.VISIBLE

            binding.fabAdd.setImageResource(R.drawable.ic_clear)
        }

        isFabOpen = !isFabOpen

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun navigateToStateDiary() {
        val intent = Intent(this, StateDiaryActivity::class.java)
        intent.putExtra("date", calendarViewModel.currentDate.value ?: LocalDate.now().toString())
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        calendarViewModel.getAllStateDate()
    }

    private fun performDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_calendar)
        calendarViewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)
        binding.vm = calendarViewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()
    }
}