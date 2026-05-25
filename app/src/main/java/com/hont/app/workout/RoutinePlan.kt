package com.hont.app.workout

import java.io.Serializable

// 앱에서 사용 가능한 운동 종목 목록
val AVAILABLE_EXERCISES = listOf(
    PlannedExercise("스쿼트", 3, 15),
    PlannedExercise("푸시업", 3, 12),
    PlannedExercise("런지", 3, 12),
    PlannedExercise("버피", 3, 10),
    PlannedExercise("플랭크", 3, 30, isTimeBased = true),
    PlannedExercise("마운틴 클라이머", 3, 20)
)

data class PlannedExercise(
    val name: String,
    val sets: Int,
    val repsOrSeconds: Int,
    val isTimeBased: Boolean = false
) : Serializable {
    val displayDetail: String
        get() = if (isTimeBased) "${sets}세트 × ${repsOrSeconds}초"
                else "${sets}세트 × ${repsOrSeconds}회"
}

data class RoutinePlan(
    val id: String,
    val name: String,
    val days: List<Int>,           // DayOfWeek.value: 1=월 ~ 7=일
    val exercises: List<PlannedExercise>,
    val isRecurring: Boolean = true,
    val targetDate: String? = null // yyyy-MM-dd (즉흥 루틴용)
) : Serializable
