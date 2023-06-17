package com.unpas.kuliah.ui.mahasiswa

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MahasiswaData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val npm: String,
    val nama: String,
    val tanggal_lahir: String,
    val jenis_kelamin: String,
) {
    enum class JenisKelamin(val jenis: String) {
        Lakilaki("Laki-laki"),
        Perempuan("Perempuan")
    }
}