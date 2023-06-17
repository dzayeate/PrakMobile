package com.unpas.kuliah.ui.mahasiswa

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete

@Dao
interface MahasiswaDao {
    @Query("SELECT * FROM MahasiswaData")
    suspend fun getAllMahasiswas(): List<MahasiswaData>

    @Insert
    suspend fun insertMahasiswa(mahasiswa: MahasiswaData)

    @Update
    suspend fun updateMahasiswa(mahasiswa: MahasiswaData)

    @Delete
    suspend fun deleteMahasiswa(mahasiswa: MahasiswaData)
}