package com.waterplant.distribution.core.database

import androidx.room.TypeConverter
import com.waterplant.distribution.core.models.SyncStatus

class Converters {
    @TypeConverter
    fun toSyncStatus(value: String) = enumValueOf<SyncStatus>(value)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus) = value.name
}
