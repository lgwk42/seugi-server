package com.seugi.api.domain.timetable.presentation.dto.request

data class CreateTimetableRequest(
    val workspaceId: String,
    val grade: String,
    val classNum: String,
    val time: String,
    val subject: String,
    val date: String,
)