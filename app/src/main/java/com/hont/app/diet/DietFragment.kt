package com.hont.app.diet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hont.app.databinding.FragmentDietBinding
import com.hont.app.network.RetrofitClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DietFragment : Fragment() {

    private var _binding: FragmentDietBinding? = null
    private val binding get() = _binding!!

    private var currentDate = LocalDate.now()
    private val dateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)")
    private val goal = NutritionGoal()

    // 날짜별 식단 기록 (로컬 캐시)
    private val logs = mutableMapOf<LocalDate, MutableMap<MealType, MutableList<DietLogItem>>>()

    private val dietAdapter = DietListAdapter(
        onAddClick = { mealType -> openFoodSearch(mealType) },
        onDeleteClick = { item -> deleteFood(item) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDietBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupDateNavigation()
        loadDietLogs(currentDate)
    }

    private fun setupRecyclerView() {
        binding.rvDietLog.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDietLog.adapter = dietAdapter
    }

    private fun setupDateNavigation() {
        binding.btnPrevDay.setOnClickListener {
            currentDate = currentDate.minusDays(1)
            loadDietLogs(currentDate)
        }
        binding.btnNextDay.setOnClickListener {
            currentDate = currentDate.plusDays(1)
            loadDietLogs(currentDate)
        }
    }

    private fun loadDietLogs(date: LocalDate) {
        binding.tvDate.text = date.format(dateFormatter)
        lifecycleScope.launch {
            try {
                val responses = RetrofitClient.api.getDietLogs(date.toString()).data ?: emptyList()
                val dayMap = mutableMapOf<MealType, MutableList<DietLogItem>>()
                for (resp in responses) {
                    val item = resp.toDietLogItem()
                    dayMap.getOrPut(item.mealType) { mutableListOf() }.add(item)
                }
                logs[date] = dayMap
            } catch (e: Exception) {
                // 네트워크 오류 시 로컬 캐시 유지
            }
            renderDate(date)
        }
    }

    private fun renderDate(date: LocalDate) {
        binding.tvDate.text = date.format(dateFormatter)
        val dayLogs = logs[date] ?: emptyMap()
        updateNutritionSummary(dayLogs)
        dietAdapter.submitLogs(dayLogs)
    }

    private fun updateNutritionSummary(dayLogs: Map<MealType, List<DietLogItem>>) {
        val allItems = dayLogs.values.flatten()
        val totalCalories = allItems.sumOf { it.calories }
        val totalCarbs = allItems.sumOf { it.carbs.toDouble() }.toFloat()
        val totalProtein = allItems.sumOf { it.protein.toDouble() }.toFloat()
        val totalFat = allItems.sumOf { it.fat.toDouble() }.toFloat()

        binding.tvCaloriesCurrent.text = totalCalories.toString()
        binding.tvCaloriesRemain.text = "잔여 ${(goal.calories - totalCalories).coerceAtLeast(0)} kcal"

        binding.progressCarbs.progress =
            ((totalCarbs / goal.carbsG) * 100).toInt().coerceAtMost(100)
        binding.progressProtein.progress =
            ((totalProtein / goal.proteinG) * 100).toInt().coerceAtMost(100)
        binding.progressFat.progress =
            ((totalFat / goal.fatG) * 100).toInt().coerceAtMost(100)

        binding.tvCarbs.text = "${totalCarbs.toInt()} / ${goal.carbsG}g"
        binding.tvProtein.text = "${totalProtein.toInt()} / ${goal.proteinG}g"
        binding.tvFat.text = "${totalFat.toInt()} / ${goal.fatG}g"
    }

    private fun openFoodSearch(mealType: MealType) {
        val sheet = FoodSearchSheet.newInstance(mealType)
        sheet.onFoodAdded = { item ->
            addFoodToLog(item)
        }
        sheet.show(childFragmentManager, "food_search")
    }

    private fun addFoodToLog(item: DietLogItem) {
        lifecycleScope.launch {
            try {
                val request = AddDietLogRequest(
                    date = currentDate.toString(),
                    mealType = item.mealType.name,
                    foodName = item.foodName,
                    amountG = item.amountG,
                    calories = item.calories,
                    carbs = item.carbs,
                    protein = item.protein,
                    fat = item.fat
                )
                val saved = RetrofitClient.api.addDietLog(request).data
                if (saved != null) {
                    val dayMap = logs.getOrPut(currentDate) { mutableMapOf() }
                    dayMap.getOrPut(item.mealType) { mutableListOf() }.add(saved.toDietLogItem())
                }
                renderDate(currentDate)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "식단 추가에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteFood(item: DietLogItem) {
        lifecycleScope.launch {
            try {
                RetrofitClient.api.deleteDietLog(item.id)
                logs[currentDate]?.get(item.mealType)?.removeAll { it.id == item.id }
                renderDate(currentDate)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "삭제에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
