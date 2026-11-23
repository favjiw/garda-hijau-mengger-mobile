package com.codelabs.gardahijau.model.local.antrean

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Database lokal untuk antrean kirim saat offline. */
@Database(entities = [AntreanItem::class], version = 1, exportSchema = false)
abstract class AntreanDatabase : RoomDatabase() {

    abstract fun antreanDao(): AntreanDao

    companion object {
        @Volatile
        private var instance: AntreanDatabase? = null

        fun ambil(context: Context): AntreanDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AntreanDatabase::class.java,
                    "antrean-kirim.db",
                ).build().also { instance = it }
            }
    }
}
