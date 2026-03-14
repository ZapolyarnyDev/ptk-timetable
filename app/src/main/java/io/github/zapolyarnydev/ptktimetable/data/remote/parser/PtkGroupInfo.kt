package io.github.zapolyarnydev.ptktimetable.data.remote.parser

data class PtkGroupInfo(
    val collegeName: String,
    val course: Int,
    val courseName: String,
    val groupName: String,
    val xlsUrl: String
)
