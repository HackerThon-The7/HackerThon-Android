package com.yongjincompany.hackerthonandroid.features.calendar.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class CalendarViewModel : ViewModel() {

    val currentDate = MutableLiveData(LocalDate.now().toString())
    val info = MutableLiveData("입력된 정보가 없습니다.")
    val isWoman = MutableLiveData(true)

}