package com.unpas.kuliah.ui.mahasiswa

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MahasiswaData::class],
    version = 1
)
abstract class MahasiswaDatabase : RoomDatabase(){

    abstract fun mahasiswaDao() : MahasiswaDao

    companion object {

        @Volatile private var instance : MahasiswaDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            MahasiswaDatabase::class.java,
            "mahasiswa.db"
        ).build()

    }
}
