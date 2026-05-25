package com.hont.app.diet

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hont.app.databinding.SheetFoodSearchBinding
import com.hont.app.network.RetrofitClient
import kotlinx.coroutines.launch

class FoodSearchSheet : BottomSheetDialogFragment() {

    private var _binding: SheetFoodSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var mealType: MealType
    var onFoodAdded: ((DietLogItem) -> Unit)? = null

    private val searchAdapter = FoodSearchAdapter { food ->
        showAmountDialog(food)
    }

    companion object {
        private const val ARG_MEAL_TYPE = "arg_meal_type"

        fun newInstance(mealType: MealType): FoodSearchSheet {
            return FoodSearchSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_MEAL_TYPE, mealType.name)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetFoodSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mealType = MealType.valueOf(
            arguments?.getString(ARG_MEAL_TYPE) ?: MealType.BREAKFAST.name
        )
        binding.tvSearchTitle.text = "${mealType.label} 식품 검색"

        setupRecyclerView()
        setupSearch()
    }

    private fun setupRecyclerView() {
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = searchAdapter
    }

    private fun setupSearch() {
        val doSearch = {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) searchFood(query)
        }

        binding.btnSearch.setOnClickListener { doSearch() }
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(); true
            } else false
        }
    }

    private fun searchFood(query: String) {
        binding.llSearchHint.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE
        binding.progressSearch.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val results = RetrofitClient.api.searchFoods(query)
                binding.progressSearch.visibility = View.GONE
                if (results.isEmpty()) {
                    binding.tvSearchEmpty.visibility = View.VISIBLE
                    binding.rvSearchResults.visibility = View.GONE
                } else {
                    binding.tvSearchEmpty.visibility = View.GONE
                    binding.rvSearchResults.visibility = View.VISIBLE
                    searchAdapter.submitList(results)
                }
            } catch (e: Exception) {
                binding.progressSearch.visibility = View.GONE
                binding.tvSearchEmpty.visibility = View.GONE
                binding.rvSearchResults.visibility = View.GONE
                binding.llSearchHint.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "검색에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAmountDialog(food: FoodSearchResult) {
        val hasUnit = food.unitName != null && food.unitWeight != null

        val input = EditText(requireContext()).apply {
            hint = if (hasUnit) "몇 ${food.unitName}?" else "양 입력 (g)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(if (hasUnit) "1" else "100")
            setPadding(48, 32, 48, 32)
        }

        val message = if (hasUnit) "1${food.unitName} = ${food.unitWeight!!.toInt()}g"
                      else "섭취량을 입력하세요"

        AlertDialog.Builder(requireContext())
            .setTitle(food.foodName)
            .setMessage(message)
            .setView(input)
            .setPositiveButton("추가") { _, _ ->
                val inputVal = input.text.toString().toIntOrNull() ?: 1
                val amountG = if (hasUnit) food.amountGFromCount(inputVal) else inputVal
                val logItem = food.toLogItem(amountG, mealType)
                onFoodAdded?.invoke(logItem)
                dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
