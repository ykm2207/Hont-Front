package com.hont.app.network

import com.hont.app.diet.AddDietLogRequest
import com.hont.app.diet.ApiResponse
import com.hont.app.diet.DietLogResponse
import com.hont.app.diet.FoodSearchResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ── 식단 ──────────────────────────────────────────

    // 음식 검색
    @GET("api/diet/foods/search")
    suspend fun searchFoods(@Query("query") query: String): ApiResponse<List<FoodSearchResult>>

    // 날짜별 식단 기록 조회
    @GET("api/diet/logs/{date}")
    suspend fun getDietLogs(@Path("date") date: String): ApiResponse<List<DietLogResponse>>

    // 식단 항목 추가
    @POST("api/diet/logs")
    suspend fun addDietLog(@Body request: AddDietLogRequest): ApiResponse<DietLogResponse>

    // 식단 항목 삭제
    @DELETE("api/diet/logs/items/{itemId}")
    suspend fun deleteDietLog(@Path("itemId") itemId: Long): Response<Void>
}
