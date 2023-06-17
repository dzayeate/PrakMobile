package com.unpas.kuliah.ui.matakuliah

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MataKuliahData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val kode: String,
    val nama: String,
    val sks: Int,
    val praktikum: Boolean,
    val deskripsi: String
)