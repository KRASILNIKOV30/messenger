package com.example.messenger

import android.app.Application
import androidx.room.Room

class StorageApp : Application() {
    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "messenger-storage",
        ).build()
    }

    companion object {
        lateinit var db: AppDatabase
    }
}