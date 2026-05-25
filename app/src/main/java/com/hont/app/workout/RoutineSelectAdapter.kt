package com.hont.app.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hont.app.databinding.ItemRoutineSelectBinding

class RoutineSelectAdapter(
    private val onStartClick: (RoutinePlan) -> Unit,
    private val onEditClick: ((RoutinePlan) -> Unit)? = null
) : RecyclerView.Adapter<RoutineSelectAdapter.ViewHolder>() {

    private val items = mutableListOf<RoutinePlan>()

    fun submitList(list: List<RoutinePlan>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemRoutineSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: RoutinePlan) {
            binding.tvRoutineName.text = plan.name
            binding.tvRoutineDetail.text = plan.exercises.joinToString(" · ") { it.name }

            binding.btnStartRoutine.setOnClickListener { onStartClick(plan) }

            if (onEditClick != null) {
                binding.btnEditRoutine.visibility = View.VISIBLE
                binding.btnEditRoutine.setOnClickListener { onEditClick.invoke(plan) }
            } else {
                binding.btnEditRoutine.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRoutineSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
