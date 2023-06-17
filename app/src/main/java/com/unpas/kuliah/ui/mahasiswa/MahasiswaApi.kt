package com.unpas.kuliah.ui.mahasiswa

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MahasiswaApi {
    @POST("mahasiswa")
    suspend fun addMahasiswa(@Body mahasiswaData: MahasiswaData): Response<ResponseBody>
}