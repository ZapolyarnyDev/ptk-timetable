package io.github.zapolyarnydev.ptktimetable.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM groups ORDER BY course, groupName")
    fun observeAll(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups ORDER BY course, groupName")
    suspend fun getAll(): List<GroupEntity>

    @Query("SELECT * FROM groups WHERE groupName = :groupName LIMIT 1")
    suspend fun getByName(groupName: String): GroupEntity?

    @Upsert
    suspend fun upsertAll(groups: List<GroupEntity>)

    @Transaction
    suspend fun saveAll(groups: List<GroupEntity>) {
        upsertAll(groups)
    }
}
