package io.github.zapolyarnydev.ptktimetable.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.SyncMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncMetadataDao {

    @Query("SELECT * FROM sync_metadata WHERE resourceKey = :resourceKey LIMIT 1")
    fun observe(resourceKey: String): Flow<SyncMetadataEntity?>

    @Query("SELECT * FROM sync_metadata WHERE resourceKey = :resourceKey LIMIT 1")
    suspend fun get(resourceKey: String): SyncMetadataEntity?

    @Upsert
    suspend fun upsert(metadata: SyncMetadataEntity)
}
