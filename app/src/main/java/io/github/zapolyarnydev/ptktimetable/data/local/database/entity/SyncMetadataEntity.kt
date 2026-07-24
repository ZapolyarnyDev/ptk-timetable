package io.github.zapolyarnydev.ptktimetable.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(@PrimaryKey val resourceKey: String, val lastSuccessfulSyncEpochMillis: Long)
