package com.waterplant.distribution.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.waterplant.distribution.core.database.dao.CustomerDao
import com.waterplant.distribution.core.database.dao.DeliveryDao
import com.waterplant.distribution.core.database.dao.EmployeeDao
import com.waterplant.distribution.core.database.entity.CustomerEntity
import com.waterplant.distribution.core.database.entity.DeliveryEntity
import com.waterplant.distribution.core.database.entity.EmployeeEntity

@Database(
    entities = [
        EmployeeEntity::class,
        CustomerEntity::class,
        DeliveryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun customerDao(): CustomerDao
    abstract fun deliveryDao(): DeliveryDao
}
