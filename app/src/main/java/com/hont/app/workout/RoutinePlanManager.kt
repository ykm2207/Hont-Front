package com.hont.app.workout

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.util.UUID

object RoutinePlanManager {

    private const val PREF_NAME = "routine_plans"
    private const val KEY_PLANS = "plans"
    private val gson = Gson()

    fun savePlan(context: Context, plan: RoutinePlan) {
        val plans = getAllPlans(context).toMutableList()
        val idx = plans.indexOfFirst { it.id == plan.id }
        if (idx >= 0) plans[idx] = plan else plans.add(plan)
        write(context, plans)
    }

    fun deletePlan(context: Context, planId: String) {
        write(context, getAllPlans(context).filter { it.id != planId })
    }

    fun getAllPlans(context: Context): List<RoutinePlan> {
        val json = prefs(context).getString(KEY_PLANS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<RoutinePlan>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getPlansForDate(context: Context, date: LocalDate): List<RoutinePlan> =
        getAllPlans(context).filter { plan ->
            if (plan.isRecurring) plan.days.contains(date.dayOfWeek.value)
            else plan.targetDate == date.toString()
        }

    // 특정 날짜 범위 내 루틴이 계획된 날짜 집합 반환
    fun getPlannedDates(context: Context, from: LocalDate, to: LocalDate): Set<LocalDate> {
        val plans = getAllPlans(context)
        val result = mutableSetOf<LocalDate>()
        var date = from
        while (!date.isAfter(to)) {
            val hasplan = plans.any { plan ->
                if (plan.isRecurring) plan.days.contains(date.dayOfWeek.value)
                else plan.targetDate == date.toString()
            }
            if (hasplan) result.add(date)
            date = date.plusDays(1)
        }
        return result
    }

    fun newId(): String = UUID.randomUUID().toString()

    private fun write(context: Context, plans: List<RoutinePlan>) =
        prefs(context).edit().putString(KEY_PLANS, gson.toJson(plans)).apply()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
