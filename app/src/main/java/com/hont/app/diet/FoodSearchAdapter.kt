package com.hont.app.diet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hont.app.databinding.ItemFoodSearchBinding

class FoodSearchAdapter(
    private val onAddClick: (FoodSearchResult) -> Unit
) : RecyclerView.Adapter<FoodSearchAdapter.ViewHolder>() {

    private val items = mutableListOf<FoodSearchResult>()

    fun submitList(list: List<FoodSearchResult>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemFoodSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(food: FoodSearchResult) {
            binding.tvFoodName.text = food.foodName
            binding.tvFoodNutrition.text =
                "${food.caloriesPer100g.toInt()} kcal  ·  탄 ${food.carbsPer100g.toInt()}g  단 ${food.proteinPer100g.toInt()}g  지 ${food.fatPer100g.toInt()}g  (100g 기준)"
            binding.btnAdd.setOnClickListener { onAddClick(food) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemFoodSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
}
