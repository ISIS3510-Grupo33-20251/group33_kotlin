package com.example.universe.data.models

data class GradeEntryDto(
    val name: String,
    val percentage: Double?,
    val grade: Double
)

data class CalculatorSubjectDto(
    val _id: String? = null,
    val subject_name: String,
    val owner_id: String,
    val entries: List<GradeEntryDto> = emptyList(),
    val created_date: String? = null,
    val last_modified: String? = null
)