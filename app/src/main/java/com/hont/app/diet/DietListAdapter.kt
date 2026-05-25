package com.hont.app.diet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hont.app.databinding.ItemDietFoodBinding
import com.hont.app.databinding.ItemDietSectionBinding

class DietListAdapter(
    private val onAddClick: (MealType) -> Unit,
    private val onDeleteClick: (DietLogItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SECTION = 0
        private const val TYPE_FOOD = 1
    }

    private val items = mutableListOf<DietListItem>()

    fun submitLogs(logs: Map<MealType, List<DietLogItem>>) {
        items.clear()
        MealType.values().forEach { mealType ->
            val mealItems = logs[mealType] ?: emptyList()
            val totalCalories = mealItems.sumOf { it.calories }
            items.add(DietListItem.SectionHeader(mealType, totalCalories))
            mealItems.forEach { items.add(DietListItem.FoodEntry(it)) }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is DietListItem.SectionHeader -> TYPE_SECTION
        is DietListItem.FoodEntry -> TYPE_FOOD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SECTION -> SectionViewHolder(
                ItemDietSectionBinding.inflate(inflater, parent, false)
            )
            else -> FoodViewHolder(
                ItemDietFoodBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DietListItem.SectionHeader -> (holder as SectionViewHolder).bind(item)
            is DietListItem.FoodEntry -> (holder as FoodViewHolder).bind(item.item)
        }
    }

    override fun getItemCount() = items.size

    inner class SectionViewHolder(private val binding: ItemDietSectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(header: DietListItem.SectionHeader) {
            binding.tvMealType.text = header.mealType.label
            binding.tvSectionCalories.text =
                if (header.totalCalories > 0) "${header.totalCalories} kcal" else ""
            binding.btnAddFood.setOnClickListener { onAddClick(header.mealType) }
        }
    }

    inner class FoodViewHolder(private val binding: ItemDietFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DietLogItem) {
            binding.tvFoodName.text = item.foodName
            binding.tvFoodDetail.text = "${item.amountG}g  ·  탄 ${item.carbs.toInt()}g  단 ${item.protein.toInt()}g  지 ${item.fat.toInt()}g"
            binding.tvFoodCalories.text = "${item.calories} kcal"
            binding.btnDeleteFood.setOnClickListener { onDeleteClick(item) }
        }
    }
}
