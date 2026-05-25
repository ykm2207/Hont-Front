package com.hont.app.settings

import android.content.Context

object SettingsManager {

    private const val PREFS_NAME = "settings_prefs"

    // 다크모드
    fun isDarkMode(context: Context) = prefs(context).getBoolean("dark_mode", false)
    fun setDarkMode(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean("dark_mode", enabled).apply()

    // 일일 칼로리 목표
    fun getCalorieGoal(context: Context) = prefs(context).getInt("calorie_goal", 2000)
    fun setCalorieGoal(context: Context, calories: Int) =
        prefs(context).edit().putInt("calorie_goal", calories).apply()

    // 주간 운동 목표 (횟수)
    fun getWorkoutFreq(context: Context) = prefs(context).getInt("workout_freq", 3)
    fun setWorkoutFreq(context: Context, times: Int) =
        prefs(context).edit().putInt("workout_freq", times).apply()

    // 운동 알림
    fun isWorkoutNotifEnabled(context: Context) =
        prefs(context).getBoolean("workout_notif", false)
    fun setWorkoutNotifEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean("workout_notif", enabled).apply()
    fun getWorkoutNotifTime(context: Context): Pair<Int, Int> =
        Pair(prefs(context).getInt("workout_notif_h", 19), prefs(context).getInt("workout_notif_m", 0))
    fun setWorkoutNotifTime(context: Context, hour: Int, minute: Int) =
        prefs(context).edit().putInt("workout_notif_h", hour).putInt("workout_notif_m", minute).apply()

    // 식단 알림 (meal: "BREAKFAST" / "LUNCH" / "DINNER")
    fun isDietNotifEnabled(context: Context, meal: String) =
        prefs(context).getBoolean("diet_notif_$meal", false)
    fun setDietNotifEnabled(context: Context, meal: String, enabled: Boolean) =
        prefs(context).edit().putBoolean("diet_notif_$meal", enabled).apply()
    fun getDietNotifTime(context: Context, meal: String): Pair<Int, Int> {
        val defaultHour = when (meal) { "BREAKFAST" -> 8; "LUNCH" -> 12; else -> 18 }
        return Pair(
            prefs(context).getInt("diet_notif_h_$meal", defaultHour),
            prefs(context).getInt("diet_notif_m_$meal", 0)
        )
    }
    fun setDietNotifTime(context: Context, meal: String, hour: Int, minute: Int) =
        prefs(context).edit()
            .putInt("diet_notif_h_$meal", hour)
            .putInt("diet_notif_m_$meal", minute)
            .apply()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
