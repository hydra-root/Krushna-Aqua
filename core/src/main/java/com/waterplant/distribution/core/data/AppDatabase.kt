package com.waterplant.distribution.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.waterplant.distribution.core.data.dao.CustomerDao
import com.waterplant.distribution.core.data.dao.DeliveryDao
import com.waterplant.distribution.core.data.dao.EmployeeDao
import com.waterplant.distribution.core.data.model.Customer
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.model.Employee

/**
 * Main Room database definition for the Krushna Aqua platform.
 *
 * Provides thread-safe, transactional SQLite storage for:
 * - [Employee]: Staff credentials and role permissions
 * - [Customer]: Customer profiles, group classification (Cash vs Postpaid), and jar balance
 * - [Delivery]: Immutable delivery log records containing jar balance transactions and cash collections
 *
 * @author Pavan Pawar
 */
@Database(
    entities = [Employee::class, Customer::class, Delivery::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun customerDao(): CustomerDao
    abstract fun deliveryDao(): DeliveryDao
}

