package com.unpas.kuliah.ui.matakuliah

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete

@Dao
interface MataKuliahDao {
    @Query("SELECT * FROM MataKuliahData")
    suspend fun getAllMataKuliahs(): List<MataKuliahData>

    @Insert
    suspend fun insertMataKuliah(mataKuliah: MataKuliahData)

    @Update
    suspend fun updateMataKuliah(mataKuliah: MataKuliahData)

    @Delete
    suspend fun deleteMataKuliah(mataKuliah: MataKuliahData)
}