package com.unpas.kuliah.ui.matakuliah

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MataKuliahApi {
    @POST("matakuliah")
    suspend fun addMataKuliah(@Body mataKuliahData: MataKuliahData): Response<ResponseBody>
}