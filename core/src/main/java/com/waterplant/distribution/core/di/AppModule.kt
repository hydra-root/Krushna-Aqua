package com.waterplant.distribution.core.di

import android.content.Context
import androidx.room.Room
import com.waterplant.distribution.core.data.AppDatabase
import com.waterplant.distribution.core.data.dao.CustomerDao
import com.waterplant.distribution.core.data.dao.DeliveryDao
import com.waterplant.distribution.core.data.dao.EmployeeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "water_jar_distribution.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideEmployeeDao(db: AppDatabase): EmployeeDao = db.employeeDao()

    @Provides
    fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()

    @Provides
    fun provideDeliveryDao(db: AppDatabase): DeliveryDao = db.deliveryDao()
}
