package com.example.universe.domain.models

import java.time.LocalDate

data class DaySchedule(
    val date: LocalDate,
    val meetings: List<Meeting> = emptyList()
)