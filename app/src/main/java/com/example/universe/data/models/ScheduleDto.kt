package com.example.universe.data.models

import android.app.usage.UsageEvents
import com.example.universe.domain.models.Meeting
import java.time.LocalDate

data class DaySchedule(
    val date: LocalDate,
    val meetings: List<Meeting>,
    val otherEvents: List<UsageEvents.Event>
)