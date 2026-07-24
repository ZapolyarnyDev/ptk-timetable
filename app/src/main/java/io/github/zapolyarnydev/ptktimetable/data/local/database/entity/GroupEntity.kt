package io.github.zapolyarnydev.ptktimetable.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val groupName: String,
    val collegeName: String,
    val course: Int,
    val courseName: String,
    val sourceUrl: String,
)
