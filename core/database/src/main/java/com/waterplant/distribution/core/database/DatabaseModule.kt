package com.waterplant.distribution.core.database

import android.content.Context
import androidx.room.Room
import com.waterplant.distribution.core.database.dao.CustomerDao
import com.waterplant.distribution.core.database.dao.DeliveryDao
import com.waterplant.distribution.core.database.dao.EmployeeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "water_jar_distribution.db"
        ).build()
    }

    @Provides
    fun provideEmployeeDao(database: AppDatabase): EmployeeDao = database.employeeDao()

    @Provides
    fun provideCustomerDao(database: AppDatabase): CustomerDao = database.customerDao()

    @Provides
    fun provideDeliveryDao(database: AppDatabase): DeliveryDao = database.deliveryDao()
}
