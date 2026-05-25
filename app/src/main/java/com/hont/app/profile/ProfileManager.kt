package com.hont.app.profile

import android.content.Context

object ProfileManager {

    private const val PREFS_NAME = "profile_prefs"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_NAME = "name"
    private const val KEY_GENDER = "gender"
    private const val KEY_HEIGHT = "height"
    private const val KEY_WEIGHT = "weight"
    private const val KEY_GOAL_WEIGHT = "goal_weight"
    private const val KEY_WORKOUT_GOAL = "workout_goal"
    private const val KEY_DISEASES = "diseases"

    fun isOnboardingDone(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun saveProfile(context: Context, data: ProfileData) {
        prefs(context).edit().apply {
            putBoolean(KEY_ONBOARDING_DONE, true)
            putString(KEY_NAME, data.name)
            putString(KEY_GENDER, data.gender)
            putInt(KEY_HEIGHT, data.heightCm)
            putFloat(KEY_WEIGHT, data.weightKg)
            putFloat(KEY_GOAL_WEIGHT, data.goalWeightKg)
            putString(KEY_WORKOUT_GOAL, data.workoutGoal)
            putString(KEY_DISEASES, data.diseases.joinToString(","))
        }.apply()
        // TODO: API 연동 후 PUT /api/profile 호출 추가
    }

    fun getProfile(context: Context): ProfileData {
        val prefs = prefs(context)
        val diseasesStr = prefs.getString(KEY_DISEASES, "") ?: ""
        return ProfileData(
            name = prefs.getString(KEY_NAME, "") ?: "",
            gender = prefs.getString(KEY_GENDER, "") ?: "",
            heightCm = prefs.getInt(KEY_HEIGHT, 0),
            weightKg = prefs.getFloat(KEY_WEIGHT, 0f),
            goalWeightKg = prefs.getFloat(KEY_GOAL_WEIGHT, 0f),
            workoutGoal = prefs.getString(KEY_WORKOUT_GOAL, "") ?: "",
            diseases = if (diseasesStr.isBlank()) emptyList() else diseasesStr.split(",")
        )
    }

    fun updateProfile(context: Context, data: ProfileData) {
        saveProfile(context, data)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
