package com.hont.app.diet

import com.google.gson.annotations.SerializedName

enum class MealType(val label: String) {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    SNACK("간식")
}

// 식단 기록 개별 항목 (앱 내부 모델)
data class DietLogItem(
    val id: Long,
    val foodName: String,
    val amountG: Int,
    val calories: Int,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val mealType: MealType
)

// 공통 API 응답 래퍼
data class ApiResponse<T>(
    @SerializedName("data")    val data: T?,
    @SerializedName("message") val message: String = "",
    @SerializedName("success") val success: Boolean = false
)

// 음식 검색 결과 (API: GET /api/diet/foods/search)
data class FoodSearchResult(
    @SerializedName("foodName")    val foodName: String,
    @SerializedName("calories")    val caloriesPer100g: Double,
    @SerializedName("carbohydrate") val carbsPer100g: Float,
    @SerializedName("protein")     val proteinPer100g: Float,
    @SerializedName("fat")         val fatPer100g: Float,
    @SerializedName("unitName")    val unitName: String? = null,
    @SerializedName("unitWeight")  val unitWeight: Double? = null
) {
    // 실제 섭취 무게(g) 기준으로 영양소 계산
    fun toLogItem(amountG: Int, mealType: MealType): DietLogItem {
        val ratio = amountG / 100.0
        return DietLogItem(
            id = 0,
            foodName = foodName,
            amountG = amountG,
            calories = (caloriesPer100g * ratio).toInt(),
            carbs = (carbsPer100g * ratio).toFloat(),
            protein = (proteinPer100g * ratio).toFloat(),
            fat = (fatPer100g * ratio).toFloat(),
            mealType = mealType
        )
    }

    // 개수 입력 시 실제 섭취 무게(g) 계산
    fun amountGFromCount(count: Int): Int =
        ((unitWeight ?: 100.0) * count).toInt()
}

// 서버 식단 기록 응답 DTO
data class DietLogResponse(
    @SerializedName("id")           val id: Long,
    @SerializedName("mealType")     val mealType: String,
    @SerializedName("foodName")     val foodName: String,
    @SerializedName("amountG")      val amountG: Int,
    @SerializedName("calories")     val calories: Int,
    @SerializedName("carbohydrate") val carbs: Float,
    @SerializedName("protein")      val protein: Float,
    @SerializedName("fat")          val fat: Float
) {
    fun toDietLogItem(): DietLogItem = DietLogItem(
        id = id,
        foodName = foodName,
        amountG = amountG,
        calories = calories,
        carbs = carbs,
        protein = protein,
        fat = fat,
        mealType = MealType.valueOf(mealType)
    )
}

// 식단 기록 추가 요청 DTO
data class AddDietLogRequest(
    @SerializedName("date")         val date: String,
    @SerializedName("mealType")     val mealType: String,
    @SerializedName("foodName")     val foodName: String,
    @SerializedName("amountG")      val amountG: Int,
    @SerializedName("calories")     val calories: Int,
    @SerializedName("carbohydrate") val carbs: Float,
    @SerializedName("protein")      val protein: Float,
    @SerializedName("fat")          val fat: Float
)

// RecyclerView 멀티타입 아이템
sealed class DietListItem {
    data class SectionHeader(
        val mealType: MealType,
        val totalCalories: Int
    ) : DietListItem()

    data class FoodEntry(val item: DietLogItem) : DietListItem()
}

// 하루 영양 목표
data class NutritionGoal(
    val calories: Int = 2000,
    val carbsG: Int = 250,
    val proteinG: Int = 120,
    val fatG: Int = 65
)
