package com.hont.app.profile

data class ProfileData(
    val name: String = "",
    val gender: String = "",          // "MALE" / "FEMALE"
    val heightCm: Int = 0,
    val weightKg: Float = 0f,
    val goalWeightKg: Float = 0f,
    val workoutGoal: String = "",     // "DIET" / "MUSCLE" / "FITNESS" / "HEALTH"
    val diseases: List<String> = emptyList()
) {
    val bmi: Float
        get() = if (heightCm > 0 && weightKg > 0)
            weightKg / ((heightCm / 100f) * (heightCm / 100f))
        else 0f

    val bmiLabel: String
        get() = when {
            bmi <= 0f -> "-"
            bmi < 18.5f -> "저체중"
            bmi < 25f -> "정상"
            bmi < 30f -> "과체중"
            else -> "비만"
        }

    val workoutGoalLabel: String
        get() = when (workoutGoal) {
            "DIET" -> "다이어트"
            "MUSCLE" -> "근육 증가"
            "FITNESS" -> "체력 향상"
            "HEALTH" -> "건강 유지"
            else -> "-"
        }
}
